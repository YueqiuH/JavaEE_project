package com.smartcampus.app.service.teaching.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.app.dto.teaching.AutoScheduleConfigDto;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.teaching.IAiChatService;
import com.smartcampus.app.service.teaching.IAutoScheduleService;
import com.smartcampus.app.service.teaching.IExamService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.vo.AiChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiChatServiceImpl implements IAiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);
    private static final int MAX_ROWS = 100;

    private static final Pattern FORBIDDEN_SQL = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|grant|revoke|rename|call|handler"
                    + "|load|outfile|dumpfile|into|lock|union|get_lock|release_lock|benchmark|sleep"
                    + "|information_schema|performance_schema|mysql|password|pwd|pass|secret|token"
                    + "|procedure|execute|prepare|deallocate|shutdown)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SELECT_ONLY = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_EXTRACTOR = Pattern.compile(
            "\\b(?:from|join)\\s+([`\\w]+(?:\\s+(?:as\\s+)?\\w+)?(?:\\s*,\\s*[`\\w]+(?:\\s+(?:as\\s+)?\\w+)?)*)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_NAME = Pattern.compile("`?(\\w+)`?");

    private static final Set<String> STUDENT_TABLES = Set.of(
            "department", "major", "student", "course", "course_selection",
            "score", "schedule", "exam", "exam_room", "resit_apply");
    private static final Set<String> COUNSELOR_TABLES = Set.of(
            "department", "major", "student", "course", "course_selection",
            "score", "schedule", "enrollment", "user", "exam",
            "evaluation", "student_status_change");
    private static final Set<String> STAFF_TABLES = Set.of(
            "department", "major", "enrollment", "user", "fee", "payment",
            "asset", "document", "meeting", "work_plan", "notification");
    private static final Set<String> ADMIN_TABLES = Set.of(
            "department", "major", "student", "enrollment", "user", "course",
            "course_selection", "score", "schedule", "exam", "exam_room",
            "invigilation", "resit_apply", "fee", "payment", "asset",
            "document", "meeting", "work_plan", "notification", "classroom",
            "evaluation", "student_status_change", "scholarship", "competition",
            "news", "forum_post", "forum_comment");

    private static final String SCHEMA_PROMPT_BASE = """
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
            6. course(course_id 主键, course_name 课程名称, course_code 课程编号, credit 学分)
            7. course_selection(selection_id 主键, student_id 学生, course_id 课程, semester 学期, status 1=已选 2=退选)
            8. score(score_id 主键, student_id 学生, course_id 课程, score_score 总评成绩,
               regular_score 平时成绩, exam_score 期末成绩, semester 学期, gpa 绩点, publish_status)
            9. schedule(schedule_id 主键, course_id, classroom_id, teacher_id,
               semester 学期, week_day 周几, start_period, end_period)
            关联：student.dept_id/major_id → department/major；enrollment.major_id → major；
            score.student_id → student; score.course_id → course; schedule.course_id → course。
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final IAutoScheduleService autoScheduleService;
    private final IExamService examService;
    private final StringRedisTemplate redisTemplate;

    public AiChatServiceImpl(JdbcTemplate jdbcTemplate,
                             ObjectMapper objectMapper,
                             IAutoScheduleService autoScheduleService,
                             IExamService examService,
                             StringRedisTemplate redisTemplate,
                             @Value("${DEEPSEEK_API_KEY:}") String apiKey,
                             @Value("${DEEPSEEK_BASE_URL:https://api.deepseek.com}") String baseUrl,
                             @Value("${DEEPSEEK_MODEL:deepseek-chat}") String model) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.autoScheduleService = autoScheduleService;
        this.examService = examService;
        this.redisTemplate = redisTemplate;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(60));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public SseEmitter chat(String message, String conversationId, Long userId) {
        SseEmitter emitter = new SseEmitter(180000L);
        String convId = (conversationId == null || conversationId.isBlank())
                ? UUID.randomUUID().toString().replace("-", "") : conversationId;
        saveMessage(convId, userId, "user", message);
        final Integer userType = getCurrentUserType();
        new Thread(() -> processChat(emitter, convId, message, userId, userType)).start();
        return emitter;
    }

    private Integer getCurrentUserType() {
        try { return CurrentUserContext.require().userType(); }
        catch (Exception e) { return 0; }
    }

    private void processChat(SseEmitter emitter, String convId, String message, Long userId, Integer userType) {
        try {
            if (apiKey.isEmpty()) {
                demoChat(emitter, convId, message, userId, userType);
            } else {
                String intent = classifyIntent(message);
                log.info("AI_CHAT userId={} intent={}", userId, intent);
                switch (intent) {
                    case "schedule" -> handleSchedule(emitter, convId, message, userId, userType);
                    case "exam" -> handleExam(emitter, convId, message, userId, userType);
                    case "query" -> handleQuery(emitter, convId, message, userId, userType);
                    case "learn" -> handleLearn(emitter, convId, message, userId, userType);
                    default -> handleGeneralChat(emitter, convId, message, userId, userType);
                }
            }
        } catch (Exception e) {
            log.error("AI chat error for userId={}", userId, e);
            try { emitter.send(SseEmitter.event().name("error").data(errorResponse(e.getMessage()))); }
            catch (IOException ignored) { }
        } finally {
            try { emitter.complete(); } catch (Exception ignored) { }
        }
    }

    // ============ Intent Classification ============

    private String classifyIntent(String message) {
        // Local keyword matching (always works, even without API)
        String msg = message.toLowerCase();
        if (msg.contains("排课") || msg.contains("课表") || msg.contains("排班")) return "schedule";
        if (msg.contains("考试") || msg.contains("排考") || msg.contains("监考") || msg.contains("补考")) return "exam";
        if (msg.contains("查询") || msg.contains("统计") || msg.contains("成绩") || msg.contains("多少")
                || msg.contains("报表") || msg.contains("分析") || msg.contains("对比") || msg.contains("汇总")) return "query";
        if (msg.contains("学习") || msg.contains("复习") || msg.contains("练习") || msg.contains("计划")
                || msg.contains("总结") || msg.contains("建议") || msg.contains("知识点")) return "learn";

        // Try DeepSeek for ambiguous cases
        if (!apiKey.isEmpty()) {
            try {
                String result = llmCall("你是意图分类器。判断用户意图，只返回一个单词：schedule/exam/query/learn/chat。"
                        + "排课→schedule, 考试→exam, 查询/统计→query, 学习/复习/练习→learn, 其他→chat。",
                        message, false);
                if (result != null) {
                    String r = result.trim().toLowerCase();
                    if (r.contains("schedule")) return "schedule";
                    if (r.contains("exam")) return "exam";
                    if (r.contains("query")) return "query";
                    if (r.contains("learn")) return "learn";
                    if (r.contains("chat")) return "chat";
                }
            } catch (Exception e) { /* fall through to chat */ }
        }
        return "chat";
    }

    // ============ Handle: Schedule ============

    private void handleSchedule(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        if (userType == null || userType != 4) {
            sendText(emitter, convId, "一键排课仅限教务处管理员使用。你可以在「排课与课表」页面手动排课。");
            return;
        }
        sendText(emitter, convId, "收到，正在分析排课需求...");
        String semester = extractSemester(message);
        if (semester == null) semester = "2025-2026-1";
        try {
            AutoScheduleConfigDto config = new AutoScheduleConfigDto();
            config.setSemester(semester);
            Long realTeacherId = null;
            try {
                var rows = jdbcTemplate.queryForList(
                    "SELECT user_id FROM user WHERE user_type=2 AND status=1 ORDER BY user_id LIMIT 1");
                if (!rows.isEmpty()) realTeacherId = ((Number) rows.get(0).get("user_id")).longValue();
            } catch (Exception ignored) { }
            if (realTeacherId != null) config.setDefaultTeacherId(realTeacherId);
            config.setLockExistingSchedules(true);
            config.setEnableBacktracking(true);
            var result = autoScheduleService.startAutoSchedule(config);
            String taskId = null;
            if (result.getData() instanceof Map) {
                taskId = (String) ((Map<?, ?>) result.getData()).get("taskId");
            }

            // Poll progress for up to 60 seconds, sending updates via SSE
            sendText(emitter, convId, "自动排课引擎已启动！学期：" + semester + "，正在编排课表...");
            int pollCount = 0;
            while (pollCount < 30) {
                Thread.sleep(2000);
                pollCount++;
                var progress = autoScheduleService.getProgress(taskId);
                if (progress.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> p = (Map<String, Object>) progress.getData();
                    String status = (String) p.get("status");
                    int percent = p.get("percent") instanceof Number ? ((Number) p.get("percent")).intValue() : 0;
                    int completed = p.get("completedTasks") instanceof Number ? ((Number) p.get("completedTasks")).intValue() : 0;
                    int total = p.get("totalTasks") instanceof Number ? ((Number) p.get("totalTasks")).intValue() : 0;
                    String message2 = (String) p.get("message");
                    sendProgress(emitter, convId, "schedule", percent,
                        message2 != null ? message2 : ("排课中... " + completed + "/" + total));
                    if (!"running".equals(status)) break;
                }
            }

            // Final status
            var finalProgress = autoScheduleService.getProgress(taskId);
            if (finalProgress.getData() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> p = (Map<String, Object>) finalProgress.getData();
                int completed = p.get("completedTasks") instanceof Number ? ((Number) p.get("completedTasks")).intValue() : 0;
                int failed = p.get("failedTasks") instanceof Number ? ((Number) p.get("failedTasks")).intValue() : 0;
                String msg = "自动排课完成！成功编排 " + completed + " 门课程";
                if (failed > 0) msg += "，" + failed + " 门未能排入";
                msg += "。请在「排课与课表」页面查看详细课表。";
                sendText(emitter, convId, msg);
            }
            saveMessage(convId, userId, "assistant",
                    "[schedule] 学期:" + semester + " 排课完成");
        } catch (Exception e) {
            log.error("Auto schedule failed", e);
            sendText(emitter, convId, "排课失败：" + e.getMessage() + "。请尝试在「排课与课表」页面手动操作。");
        }
    }

    // ============ Handle: Exam ============

    private void handleExam(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        if (userType == null || userType != 4) {
            sendText(emitter, convId, "考试编排仅限教务处管理员使用。你可以在「考试与补考」页面手动操作。");
            return;
        }
        String semester = extractSemester(message);
        if (semester == null) semester = detectCurrentSemester();
        sendText(emitter, convId, "开始全自动考试编排，学期：" + semester + "...");
        sendProgress(emitter, convId, "exam", 20, "编排考试时间…");
        try {
            examService.scheduleExams(semester);
            sendProgress(emitter, convId, "exam", 50, "分配考场座位…");
            examService.assignAllRooms(semester);
            sendProgress(emitter, convId, "exam", 80, "指派监考教师…");
            examService.assignAllInvigilators(semester);
            sendText(emitter, convId, "全自动考试编排已完成！考试时间、考场座位、监考教师均已安排完毕。"
                    + "可在「考试与补考」页面查看详情。");
            saveMessage(convId, userId, "assistant", "[exam] 学期:" + semester + " 编排完成");
        } catch (Exception e) {
            sendText(emitter, convId, "考试编排失败：" + e.getMessage());
        }
    }

    // ============ Handle: Query ============

    private void handleQuery(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        Set<String> allowed = switch (userType == null || userType == 0 ? 1 : userType) {
            case 1 -> STUDENT_TABLES;
            case 2 -> COUNSELOR_TABLES;
            case 3 -> STAFF_TABLES;
            default -> ADMIN_TABLES;
        };
        try {
            String prompt = buildQuerySystemPrompt(userType, userId);
            JsonNode plan = parseJson(llmCall(prompt, message, true));
            String sql = sanitizeSql(plan.path("sql").asText(null), allowed);
            String ct = normalizeChart(plan.path("chartType").asText("table"));
            String title = plan.path("title").asText(
                    message.length() > 128 ? message.substring(0, 128) + "…" : message);

            List<Map<String, Object>> rows = execQuery(sql);
            List<String> cols = rows.isEmpty() ? List.of() : new ArrayList<>(rows.get(0).keySet());
            String analysis = buildAnalysis(message, cols, rows);

            sendEvent(emitter, "result", AiChatResponse.builder()
                    .type("query_result").conversationId(convId).intent("query")
                    .content(analysis).title(title).chartType(ct)
                    .columns(cols).rows(rows).demoMode(false).build());
            saveMessage(convId, userId, "assistant", "[query] " + title);
        } catch (BusinessException e) {
            sendText(emitter, convId, e.getMessage());
        }
    }

    // ============ Handle: Learn ============

    private void handleLearn(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        if (userType != null && userType == 1) {
            sendText(emitter, convId, "正在分析你的学习数据，生成个性化建议…");
        }
        String sys = "你是智慧校园AI学习辅导员。请根据学生的课程和成绩数据，提供个性化的学习建议、"
                + "复习计划（按天）、练习题（含答案）或课程总结。使用Markdown格式，语气亲切鼓励。";
        String reply = apiKey.isEmpty()
                ? buildDemoLearnResponse(message, userId)
                : llmCall(sys, message, false);
        if (reply == null) reply = buildDemoLearnResponse(message, userId);
        sendText(emitter, convId, reply);
        saveMessage(convId, userId, "assistant", reply);
    }

    // ============ Handle: General Chat ============

    private void handleGeneralChat(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        Integer ut = userType;
        String label = roleLabel(ut);
        String sys = "你是智慧校园AI智能助手。当前用户角色：" + label + "。"
                + "可用功能：排课(教务处)、考试编排(教务处)、数据查询、学习辅导、校园问答。"
                + "回复简洁专业、亲切友好。不了解的事情如实告知。";
        String reply = apiKey.isEmpty()
                ? buildDemoGeneral(message, label)
                : llmCall(sys, message, false);
        if (reply == null) reply = buildDemoGeneral(message, label);
        sendText(emitter, convId, reply);
        saveMessage(convId, userId, "assistant", reply);
        List<String> sug = switch (ut == null ? 0 : ut) {
            case 1 -> List.of("查询我的成绩", "生成复习计划", "我的课表是什么");
            case 2 -> List.of("班级成绩统计", "学业预警名单", "生源地分布分析");
            case 3 -> List.of("统计院系报到率", "查看会议安排", "查询公文状态");
            case 4 -> List.of("一键排课", "一键编排考试", "全校招生数据统计");
            default -> List.of("查成绩", "看课表", "学习建议");
        };
        sendEvent(emitter, "suggestions", AiChatResponse.builder().type("suggestions")
                .conversationId(convId).suggestions(sug).build());
    }

    // ============ Demo Mode ============

    private void demoChat(SseEmitter emitter, String convId, String message, Long userId, Integer userType) throws IOException {
        String label = roleLabel(userType);
        String reply = buildDemoGeneral(message, label);
        sendText(emitter, convId, reply);
        saveMessage(convId, userId, "assistant", reply);
        sendEvent(emitter, "suggestions", AiChatResponse.builder().type("suggestions")
                .conversationId(convId).suggestions(List.of("查成绩", "复习计划", "看课表", "统计数据"))
                .demoMode(true).build());
    }

    private String buildDemoGeneral(String msg, String label) {
        return String.format("你好，%s！我是智慧校园AI助手（演示模式）。\n\n"
                + "你问的是：%s\n\n"
                + "当前未配置 DEEPSEEK_API_KEY，所有 AI 功能在演示模式下运行。"
                + "配置密钥后即可使用完整功能：智能排课、考试编排、数据查询、学习辅导等。",
                label, msg.length() > 200 ? msg.substring(0, 200) + "…" : msg);
    }

    private String buildDemoLearnResponse(String message, Long userId) {
        String name = "同学";
        try {
            var r = jdbcTemplate.queryForList(
                    "SELECT student_name FROM student WHERE student_id = ? LIMIT 1", userId);
            if (!r.isEmpty()) name = (String) r.get(0).get("student_name");
        } catch (Exception ignored) { }
        return "你好，" + name + "！以下是演示模式的复习计划示例：\n\n"
                + "| 星期 | 上午(9-12点) | 下午(2-5点) | 晚上(7-9点) |\n"
                + "|------|------------|------------|------------|\n"
                + "| 周一 | 核心专业课复习 | 公共基础课 | 习题巩固 |\n"
                + "| 周二 | 数学/逻辑训练 | 英语读写 | 知识点整理 |\n"
                + "| 周三 | 专业实验/实践 | 思政/通识 | 综合练习 |\n"
                + "| 周四 | 核心专业课复习 | 选修课复习 | 错题回顾 |\n"
                + "| 周五 | 薄弱科目攻坚 | 自由复习 | 总结复盘 |\n\n"
                + "配置 DeepSeek API Key 后，可根据你的真实成绩数据生成个性化学习方案。";
    }

    // ============ DeepSeek API ============

    private String llmCall(String system, String user, boolean jsonMode) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.2);
        body.put("messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)));
        if (jsonMode) body.put("response_format", Map.of("type", "json_object"));
        for (int i = 0; i <= 2; i++) {
            try {
                JsonNode resp = restClient.post().uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey).body(body)
                        .retrieve().body(JsonNode.class);
                String c = resp == null ? null
                        : resp.path("choices").path(0).path("message").path("content").asText(null);
                if (c == null || c.isBlank()) throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
                return c.trim();
            } catch (BusinessException e) { throw e; }
            catch (Exception e) {
                if (i < 2) {
                    log.warn("DeepSeek retry {}: {}", i + 1, e.getMessage());
                    try { Thread.sleep(1000L * (i + 1)); }
                    catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
                } else throw new BusinessException(BaseErrorCodes.AI_SERVICE_ERROR);
            }
        }
        throw new BusinessException(BaseErrorCodes.AI_SERVICE_ERROR);
    }

    // ============ NL2SQL ============

    private String buildQuerySystemPrompt(Integer userType, Long userId) {
        String constraint = "";
        if (userType != null && userType == 1) {
            constraint = "重要：当前是学生(userId=" + userId
                    + ")。查询学生相关表时必须自动添加 WHERE student_id = " + userId + " 限制。";
        } else if (userType != null && userType == 2) {
            constraint = "当前是辅导员，可查管理班级数据，不要暴露学生个人隐私信息。";
        }
        return """
                你是校园数据查询助手。根据用户问题生成一条MySQL SELECT，以JSON返回：
                {"title":"标题","chartType":"bar|line|pie|table","sql":"SELECT ..."}
                规则：
                1. 仅生成一条SELECT，禁止写操作、分号、注释；
                2. 第一列别名name作维度，数值列用中文别名；
                3. LIMIT <= 100；
                4. 今年=%d，判断学生数默认统计在读(status=1)；
                5. 禁止查询password/phone/email/id_card等敏感列。
                %s
                %s
                """.formatted(Year.now().getValue(), SCHEMA_PROMPT_BASE, constraint);
    }

    private String sanitizeSql(String sql, Set<String> allowed) {
        if (sql == null || sql.isBlank()) throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        String c = sql.strip()
                .replaceAll("(?s)/\\*.*?\\*/", " ")
                .replaceAll("(?m)^\\s*--.*$", " ")
                .replaceAll("(?m)^\\s*#.*$", " ").trim();
        if (c.endsWith(";")) c = c.substring(0, c.length() - 1).trim();
        if (c.indexOf(';') >= 0 && c.indexOf(';') < c.length() - 1)
            throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
        if (!SELECT_ONLY.matcher(c).find() || FORBIDDEN_SQL.matcher(c).find())
            throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
        Matcher tm = TABLE_EXTRACTOR.matcher(c.toLowerCase());
        while (tm.find()) {
            for (String ref : tm.group(1).split(",")) {
                Matcher nm = TABLE_NAME.matcher(ref.trim());
                if (nm.find() && !allowed.contains(nm.group(1)))
                    throw new BusinessException(BaseErrorCodes.AI_SQL_REJECTED);
            }
        }
        return enforceLimit(c);
    }

    private String enforceLimit(String sql) {
        Pattern lp = Pattern.compile("\\blimit\\s+(\\d+)(\\s*,\\s*(\\d+)|\\s+offset\\s+(\\d+))?\\s*$",
                Pattern.CASE_INSENSITIVE);
        Matcher m = lp.matcher(sql);
        if (m.find()) {
            String cs = m.group(3) != null ? m.group(3) : m.group(1);
            try {
                if (Integer.parseInt(cs.trim()) > MAX_ROWS) {
                    return sql.substring(0, m.start()) + "LIMIT " + MAX_ROWS;
                }
            } catch (NumberFormatException ignored) { }
        } else return sql + " LIMIT " + MAX_ROWS;
        return sql;
    }

    private List<Map<String, Object>> execQuery(String sql) {
        try {
            var rows = jdbcTemplate.queryForList(sql);
            return rows.size() > MAX_ROWS ? rows.subList(0, MAX_ROWS) : rows;
        } catch (DataAccessException e) {
            log.warn("SQL exec failed: {}", sql, e);
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
    }

    private JsonNode parseJson(String content) {
        try {
            String c = content.replaceAll("(?s)^```(?:json)?\\s*", "")
                    .replaceAll("(?s)\\s*```\\s*$", "").trim();
            return objectMapper.readTree(c);
        } catch (Exception e) {
            throw new BusinessException(BaseErrorCodes.AI_RESPONSE_INVALID);
        }
    }

    private String normalizeChart(String t) {
        return switch (t == null ? "" : t.toLowerCase()) {
            case "bar", "line", "pie" -> t.toLowerCase();
            default -> "table";
        };
    }

    private String buildAnalysis(String q, List<String> cols, List<Map<String, Object>> rows) {
        if (apiKey.isEmpty()) return "已查询到 " + rows.size() + " 条数据。";
        try {
            var sample = rows.size() > 50 ? rows.subList(0, 50) : rows;
            String data = objectMapper.writeValueAsString(Map.of("columns", cols, "rows", sample));
            return llmCall("你是校园数据分析师。用中文写120字以内简洁分析，点出关键数字和趋势。直接输出正文。",
                    "问题：" + q + "\n数据：" + data, false);
        } catch (Exception e) {
            return "已查询到 " + rows.size() + " 条数据。";
        }
    }

    // ============ Utilities ============

    private String roleLabel(Integer ut) {
        return switch (ut == null ? 0 : ut) {
            case 1 -> "学生"; case 2 -> "辅导员";
            case 3 -> "教职工"; case 4 -> "教务处管理员";
            default -> "用户";
        };
    }

    private String extractSemester(String msg) {
        Matcher m = Pattern.compile("(\\d{4}-\\d{4})").matcher(msg);
        return m.find() ? m.group(1) : null;
    }

    private String detectCurrentSemester() {
        int y = Year.now().getValue();
        int m = java.time.LocalDate.now().getMonthValue();
        String term = (m >= 2 && m <= 8) ? "2" : "1";
        return (m >= 2 && m <= 8 ? (y - 1) : y) + "-" + (m >= 2 && m <= 8 ? y : y + 1) + "-" + term;
    }

    private void sendText(SseEmitter e, String cid, String text) throws IOException {
        e.send(SseEmitter.event().name("text").data(
                AiChatResponse.builder().type("text").conversationId(cid).content(text)
                        .demoMode(apiKey.isEmpty()).build()));
    }

    private void sendEvent(SseEmitter e, String event, Object data) throws IOException {
        e.send(SseEmitter.event().name(event).data(data));
    }

    private void sendProgress(SseEmitter e, String cid, String intent, int pct, String status) throws IOException {
        e.send(SseEmitter.event().name("progress").data(
                AiChatResponse.builder().type("progress").conversationId(cid)
                        .intent(intent).progress(Map.of("percent", pct, "status", status))
                        .demoMode(apiKey.isEmpty()).build()));
    }

    private AiChatResponse errorResponse(String msg) {
        return AiChatResponse.builder().type("error").content(msg).build();
    }

    private void saveMessage(String convId, Long userId, String role, String content) {
        try {
            String key = "ai:chat:" + convId;
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("role", role);
            entry.put("content", content);
            entry.put("time", String.valueOf(System.currentTimeMillis()));
            redisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(entry));
            redisTemplate.expire(key, 7, TimeUnit.DAYS);
            redisTemplate.opsForZSet().add("ai:chat:user:" + userId, convId, System.currentTimeMillis());
            redisTemplate.expire("ai:chat:user:" + userId, 7, TimeUnit.DAYS);
        } catch (Exception e) { log.warn("Save chat msg failed", e); }
    }

    @Override
    public List<Map<String, Object>> getHistory(Long userId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            var convIds = redisTemplate.opsForZSet()
                    .reverseRange("ai:chat:user:" + userId, 0, 19);
            if (convIds != null) {
                for (String cid : convIds) {
                    String key = "ai:chat:" + cid;
                    String lastMsg = redisTemplate.opsForList().index(key, -1);
                    String title = "新会话";
                    if (lastMsg != null) {
                        try {
                            JsonNode node = objectMapper.readTree(lastMsg);
                            String text = node.path("content").asText("");
                            title = text.length() > 30 ? text.substring(0, 30) : text;
                            if (title.isEmpty()) title = "新会话";
                        } catch (Exception ignored) { }
                    }
                    result.add(Map.of("id", cid, "title", title));
                }
            }
        } catch (Exception e) { log.warn("Get history failed", e); }
        return result;
    }

    @Override
    public List<Map<String, Object>> getConversation(String convId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            var list = redisTemplate.opsForList().range("ai:chat:" + convId, 0, -1);
            if (list != null) {
                for (String json : list) {
                    try {
                        JsonNode node = objectMapper.readTree(json);
                        result.add(Map.of(
                                "role", node.path("role").asText("user"),
                                "text", node.path("content").asText(""),
                                "time", node.path("time").asText("")));
                    } catch (Exception ignored) { }
                }
            }
        } catch (Exception e) { log.warn("Get conversation failed", e); }
        return result;
    }

    @Override
    public void deleteConversation(String convId) {
        try { redisTemplate.delete("ai:chat:" + convId); }
        catch (Exception e) { log.warn("Delete conversation failed", e); }
    }
}
