package com.smartcampus.app.service.base.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.app.service.base.AiReportService;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.vo.AiReportVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * D6 AI 自然语言报表：DeepSeek(OpenAI 兼容接口) NL2SQL，两段式调用。
 * 第一段生成只读 SQL 与图表定义，经安全校验后本地执行；第二段基于查询结果生成分析文字。
 * 未配置 API Key 时降级为演示模式，不影响核心功能启动与验收。
 */
@Service
public class AiReportServiceImpl implements AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportServiceImpl.class);

    /** 只允许单条 SELECT；禁止写操作、系统库、耗时函数、注入关键字与敏感列（含 PII） */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|rename|call|handler"
                    + "|load|outfile|dumpfile|into|lock|union|get_lock|release_lock|benchmark|sleep"
                    + "|information_schema|performance_schema|mysql|password|pwd|pass|secret|token"
                    + "|phone|email|id_card|address"
                    + "|procedure|execute|prepare|deallocate|shutdown)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SELECT_ONLY = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE);
    /** 提取 SQL 中 FROM/JOIN 后的表名（含逗号隐式 JOIN：FROM a, b） */
    private static final Pattern TABLE_EXTRACTOR = Pattern.compile(
            "\\b(?:from|join)\\s+([`\\w]+(?:\\s+(?:as\\s+)?\\w+)?(?:\\s*,\\s*[`\\w]+(?:\\s+(?:as\\s+)?\\w+)?)*)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME = Pattern.compile("`?(\\w+)`?");

    /** 允许查询的表白名单 */
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "department", "major", "student", "enrollment", "user",
            "course", "course_selection");

    private static final int MAX_ROWS = 100;
    private static final int ANALYSIS_SAMPLE_ROWS = 50;
    /** 每用户每分钟最多请求次数 */
    private static final int RATE_LIMIT_MAX_REQUESTS = 10;

    /** 简易内存限流：key=userId, value=最近一次请求窗口的起始时间戳+计数 */
    private final ConcurrentHashMap<Long, long[]> rateLimitMap = new ConcurrentHashMap<>();

    /** 提供给模型的数据库结构说明（仅统计相关表与列，不含密码等敏感字段） */
    private static final String SCHEMA_PROMPT = """
            数据库为 MySQL 8，库名 school_spring，相关表结构如下：
            1. department(dept_id 主键, dept_name 院系名称, dept_code 编号)
            2. major(major_id 主键, dept_id 所属院系, major_name 专业名称, major_code 编号)
            3. student(student_id 主键, student_no 学号, student_name 姓名, gender 性别 1=男 2=女,
               dept_id 院系, major_id 专业, class_name 班级, origin_place 生源地省份,
               enroll_year 入学年份, status 学籍状态 1=在读 2=休学 3=毕业 0=退学, student_age 年龄)
            4. enrollment(enrollment_id 主键, major_id 专业, year 年度, plan_count 计划招生人数,
               actual_count 实际报到人数, report_rate 报到率百分比)
            5. user(user_id 主键, username 工号, real_name 姓名, user_type 1=学生 2=教师 3=教职工 4=管理员,
               title 职称, position 职务, dept_id 院系, status 1=启用 0=停用)
            6. course(course_id 主键, course_name 课程名称)
            7. course_selection(selection_id 主键, student_id 学生, course_id 课程, semester 学期, status 1=已选 2=退选)
            关联关系：student.dept_id/major_id → department/major；enrollment.major_id → major；major.dept_id → department。
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public AiReportServiceImpl(JdbcTemplate jdbcTemplate,
                               ObjectMapper objectMapper,
                               @Value("${DEEPSEEK_API_KEY:}") String apiKey,
                               @Value("${DEEPSEEK_BASE_URL:https://api.deepseek.com}") String baseUrl,
                               @Value("${DEEPSEEK_MODEL:deepseek-chat}") String model) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public AiReportVo generate(String question) {
        // 限流检查
        checkRateLimit();

        // 清洗用户输入，防 prompt 注入
        String safeQuestion = sanitizeQuestion(question);

        if (apiKey.isEmpty()) {
            return demoReport(safeQuestion);
        }

        // 第一段：自然语言 → SQL + 图表定义
        JsonNode plan = parseJson(chat(buildSqlSystemPrompt(), safeQuestion, true));
        String sql = sanitizeSql(plan.path("sql").asText(null));
        String chartType = normalizeChartType(plan.path("chartType").asText("table"));
        String fallbackTitle = safeQuestion.length() > 128 ? safeQuestion.substring(0, 128) + "…" : safeQuestion;
        String title = plan.path("title").asText(fallbackTitle);

        List<Map<String, Object>> rows = executeQuery(sql);
        List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());

        // 审计日志
        Long userId = getCurrentUserId();
        log.info("AI_REPORT userId={} question={} sql={} rows={}", userId, safeQuestion, sql, rows.size());

        // 第二段：基于查询结果生成分析文字
        String analysis = generateAnalysis(safeQuestion, columns, rows);

        AiReportVo vo = new AiReportVo();
        vo.setQuestion(safeQuestion);
        vo.setTitle(title);
        vo.setChartType(chartType);
        vo.setAnalysis(analysis);
        vo.setColumns(columns);
        vo.setRows(rows);
        vo.setDemoMode(false);
        return vo;
    }

    /** 限流：每用户每分钟最多 RATE_LIMIT_MAX_REQUESTS 次 */
    private void checkRateLimit() {
        Long userId = getCurrentUserId();
        long now = System.currentTimeMillis();
        long[] window = rateLimitMap.computeIfAbsent(userId, k -> new long[]{now / 60000, 0});
        synchronized (window) {
            long currentMinute = now / 60000;
            if (window[0] != currentMinute) {
                window[0] = currentMinute;
                window[1] = 1;
            } else if (window[1] >= RATE_LIMIT_MAX_REQUESTS) {
                throw new BusinessException(BaseErrorCodes.AI_RATE_LIMITED);
            } else {
                window[1]++;
            }
        }
    }

    /** 清洗用户问题中的注入关键词 */
    private String sanitizeQuestion(String question) {
        if (question == null || question.isBlank()) {
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
        // 截断超长输入
        String cleaned = question.length() > 500 ? question.substring(0, 500) : question;
        // 移除常见的注入指令模式
        cleaned = cleaned.replaceAll("(?i)\\b(ignore|override|disregard|bypass)\\s+(all\\s+)?(previous|above|prior|earlier)\\s+(instructions?|rules?|constraints?|prompts?)", "[filtered]");
        return cleaned;
    }

    private Long getCurrentUserId() {
        try {
            return com.smartcampus.auth.context.CurrentUserContext.require().userId();
        } catch (Exception e) {
            return 0L;
        }
    }

    // ===== LLM 调用 =====

    private String buildSqlSystemPrompt() {
        return """
                你是校园数据报表助手。根据用户的中文问题生成一条 MySQL 查询，并以 JSON 返回，不要输出任何其他内容。
                返回格式：{"title": "报表标题", "chartType": "bar|line|pie|table", "sql": "SELECT ..."}
                规则：
                1. 只能生成单条 SELECT 语句，禁止任何写操作、子查询删除、注释和分号；
                2. 结果用于画图时，第一列请用别名 name（维度），数值列用有意义的中文别名；
                3. 行数不超过 100，必要时使用 ORDER BY 和 LIMIT；
                4. 涉及"今年/去年"时，今年按 %d 计算；
                5. 判断学生数量时默认只统计在读（status=1），除非用户明确要求其他口径。
                %s
                """.formatted(Year.now().getValue(), SCHEMA_PROMPT);
    }

    private String generateAnalysis(String question, List<String> columns, List<Map<String, Object>> rows) {
        try {
            List<Map<String, Object>> sample = rows.size() > ANALYSIS_SAMPLE_ROWS
                    ? rows.subList(0, ANALYSIS_SAMPLE_ROWS) : rows;
            String data = objectMapper.writeValueAsString(Map.of("columns", columns, "rows", sample));
            String system = "你是校园数据分析师。请根据用户问题和查询结果，用中文写一段 120 字以内的简洁分析结论，"
                    + "点出关键数字、最高/最低项和值得关注的趋势。直接输出正文，不要标题、不要列表。";
            return chat(system, "问题：" + question + "\n查询结果：" + data, false);
        } catch (Exception e) {
            log.warn("生成分析文字失败，返回默认文案", e);
            return "已为你查询到 " + rows.size() + " 条数据，详见下方图表。";
        }
    }

    /** 调用 DeepSeek chat/completions，返回首条回复内容（含重试） */
    private String chat(String systemPrompt, String userPrompt, boolean jsonMode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }
        int maxRetries = 2;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                JsonNode response = restClient.post()
                        .uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey)
                        .body(body)
                        .retrieve()
                        .body(JsonNode.class);
                String content = response == null ? null
                        : response.path("choices").path(0).path("message").path("content").asText(null);
                if (content == null || content.isBlank()) {
                    throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
                }
                return content.trim();
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    log.warn("DeepSeek 调用失败，第 {} 次重试: {}", attempt + 1, e.getMessage());
                    try { Thread.sleep(1000L * (attempt + 1)); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                } else {
                    log.error("DeepSeek 调用失败，已达最大重试次数", e);
                    throw new BusinessException(BaseErrorCodes.AI_SERVICE_ERROR);
                }
            }
        }
        throw new BusinessException(BaseErrorCodes.AI_SERVICE_ERROR);
    }

    private JsonNode parseJson(String content) {
        String cleaned = content
                .replaceAll("(?s)^```(?:json)?\\s*", "")
                .replaceAll("(?s)\\s*```\\s*$", "")
                .trim();
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            log.warn("AI 返回内容无法解析为 JSON: {}", content);
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
    }

    private String normalizeChartType(String chartType) {
        String normalized = chartType == null ? "" : chartType.toLowerCase();
        return switch (normalized) {
            case "bar", "line", "pie" -> normalized;
            default -> "table";
        };
    }

    // ===== SQL 安全校验与执行 =====

    private String sanitizeSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
        // 注释清洗（仅移除行首/行尾注释，避免破坏字符串字面量中的 # -- /*）
        String cleaned = sql.strip()
                .replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("(?m)^\\s*--.*$", " ")
                .replaceAll("(?m)^\\s*#.*$", " ")
                .trim();
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        // 分号检测：仅拒绝 SQL 中间出现的分号（不在字符串字面量中）
        int semicolonIdx = cleaned.indexOf(';');
        if (semicolonIdx >= 0 && semicolonIdx < cleaned.length() - 1) {
            log.warn("AI SQL 含有多余分号: {}", sql);
            throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
        }
        if (!SELECT_ONLY.matcher(cleaned).find() || FORBIDDEN.matcher(cleaned).find()) {
            log.warn("AI SQL 未通过安全校验: {}", sql);
            throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
        }
        // 表白名单校验（覆盖 FROM a, b 逗号隐式 JOIN）
        Matcher tableMatcher = TABLE_EXTRACTOR.matcher(cleaned.toLowerCase());
        while (tableMatcher.find()) {
            // group(1) 可能是 "student s, fee f" 这样的逗号列表，逐段取第一个词为表名
            for (String ref : tableMatcher.group(1).split(",")) {
                Matcher nameMatcher = TABLE_NAME.matcher(ref.trim());
                if (nameMatcher.find()) {
                    String tableName = nameMatcher.group(1);
                    if (!ALLOWED_TABLES.contains(tableName)) {
                        log.warn("AI SQL 引用了不允许的表: {}", tableName);
                        throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
                    }
                }
            }
        }
        // LIMIT 强制：检查并替换过大的 LIMIT 值
        cleaned = enforceMaxLimit(cleaned);
        return cleaned;
    }

    /** 强制 LIMIT 不超过 MAX_ROWS（兼容 LIMIT n / LIMIT off, n / LIMIT n OFFSET m） */
    private String enforceMaxLimit(String sql) {
        Pattern limitPattern = Pattern.compile(
                "\\blimit\\s+(\\d+)(\\s*,\\s*(\\d+)|\\s+offset\\s+(\\d+))?\\s*$", Pattern.CASE_INSENSITIVE);
        Matcher m = limitPattern.matcher(sql);
        if (m.find()) {
            // LIMIT off, count 形式时 count 是 group(3)，否则是 group(1)
            String countStr = m.group(3) != null ? m.group(3) : m.group(1);
            try {
                int count = Integer.parseInt(countStr.trim());
                if (count > MAX_ROWS) {
                    if (m.group(3) != null) {
                        return sql.substring(0, m.start()) + "LIMIT " + m.group(1).trim() + ", " + MAX_ROWS;
                    } else if (m.group(4) != null) {
                        return sql.substring(0, m.start()) + "LIMIT " + MAX_ROWS + " OFFSET " + m.group(4).trim();
                    } else {
                        return sql.substring(0, m.start()) + "LIMIT " + MAX_ROWS;
                    }
                }
            } catch (NumberFormatException ignored) { /* fall through */ }
        } else {
            return sql + " LIMIT " + MAX_ROWS;
        }
        return sql;
    }

    private List<Map<String, Object>> executeQuery(String sql) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            return rows.size() > MAX_ROWS ? rows.subList(0, MAX_ROWS) : rows;
        } catch (DataAccessException e) {
            log.warn("AI SQL 执行失败: {}", sql, e);
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
    }

    // ===== 演示模式（未配置 API Key） =====

    private AiReportVo demoReport(String question) {
        int year = Year.now().getValue();
        String sql = """
                SELECT d.dept_name AS name,
                       SUM(e.plan_count) AS 计划招生,
                       SUM(e.actual_count) AS 实际报到
                FROM enrollment e
                JOIN major m ON m.major_id = e.major_id
                JOIN department d ON d.dept_id = m.dept_id
                WHERE e.year = %d
                GROUP BY d.dept_id, d.dept_name
                ORDER BY 计划招生 DESC
                LIMIT 100""".formatted(year);
        List<Map<String, Object>> rows = executeQuery(sql);
        AiReportVo vo = new AiReportVo();
        vo.setQuestion(question);
        vo.setTitle(year + " 年各院系招生报到对比（演示报表）");
        vo.setChartType("bar");
        vo.setAnalysis("当前为演示模式（未配置 DEEPSEEK_API_KEY），展示的是预置的年度招生对比报表。"
                + "配置密钥后即可用自然语言自由查询校园数据。");
        vo.setColumns(rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet()));
        vo.setRows(rows);
        vo.setDemoMode(true);
        return vo;
    }
}
