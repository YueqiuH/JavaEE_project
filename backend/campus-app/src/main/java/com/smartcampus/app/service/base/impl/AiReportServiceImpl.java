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
import java.util.regex.Pattern;

/**
 * D6 AI 自然语言报表：DeepSeek(OpenAI 兼容接口) NL2SQL，两段式调用。
 * 第一段生成只读 SQL 与图表定义，经安全校验后本地执行；第二段基于查询结果生成分析文字。
 * 未配置 API Key 时降级为演示模式，不影响核心功能启动与验收。
 */
@Service
@SuppressWarnings("null")
public class AiReportServiceImpl implements AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportServiceImpl.class);

    /** 只允许单条 SELECT；禁止写操作、系统库、耗时函数与敏感列 */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|rename|call|handler"
                    + "|load|outfile|dumpfile|information_schema|performance_schema|mysql|sleep|benchmark|password)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SELECT_ONLY = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE);

    private static final int MAX_ROWS = 100;
    private static final int ANALYSIS_SAMPLE_ROWS = 50;

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
        if (apiKey.isEmpty()) {
            return demoReport(question);
        }

        // 第一段：自然语言 → SQL + 图表定义
        JsonNode plan = parseJson(chat(buildSqlSystemPrompt(), question, true));
        String sql = sanitizeSql(plan.path("sql").asText(null));
        String chartType = normalizeChartType(plan.path("chartType").asText("table"));
        String title = plan.path("title").asText(question);

        List<Map<String, Object>> rows = executeQuery(sql);
        List<String> columns = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());

        // 第二段：基于查询结果生成分析文字
        String analysis = generateAnalysis(question, columns, rows);

        AiReportVo vo = new AiReportVo();
        vo.setQuestion(question);
        vo.setTitle(title);
        vo.setChartType(chartType);
        vo.setSql(sql);
        vo.setAnalysis(analysis);
        vo.setColumns(columns);
        vo.setRows(rows);
        vo.setDemoMode(false);
        return vo;
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

    /** 调用 DeepSeek chat/completions，返回首条回复内容 */
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
            log.error("DeepSeek 调用失败", e);
            throw new BusinessException(BaseErrorCodes.AI_SERVICE_ERROR);
        }
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
        String cleaned = sql.replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("--.*", " ")
                .replaceAll("#.*", " ")
                .trim();
        if (cleaned.endsWith(";")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
        }
        if (cleaned.contains(";") || !SELECT_ONLY.matcher(cleaned).find() || FORBIDDEN.matcher(cleaned).find()) {
            log.warn("AI SQL 未通过安全校验: {}", sql);
            throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
        }
        if (!cleaned.toLowerCase().contains("limit")) {
            cleaned = cleaned + " LIMIT " + MAX_ROWS;
        }
        return cleaned;
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
        vo.setSql(sql);
        vo.setAnalysis("当前为演示模式（未配置 DEEPSEEK_API_KEY），展示的是预置的年度招生对比报表。"
                + "配置密钥后即可用自然语言自由查询校园数据。");
        vo.setColumns(rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet()));
        vo.setRows(rows);
        vo.setDemoMode(true);
        return vo;
    }
}
