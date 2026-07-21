package com.smartcampus.app.config;

/**
 * 排课系统全局常量定义。
 */
public final class CourseScheduleConfig {

    private CourseScheduleConfig() {}

    // ==================== 时间轴 ====================
    /** 每天总节次 */
    public static final int PERIODS_PER_DAY = 13;

    /** 上午节次区间 [1, 5] */
    public static final int MORNING_START = 1;
    public static final int MORNING_END = 5;

    /** 下午节次区间 [6, 10] */
    public static final int AFTERNOON_START = 6;
    public static final int AFTERNOON_END = 10;

    /** 晚上节次区间 [11, 13] */
    public static final int EVENING_START = 11;
    public static final int EVENING_END = 13;

    /** 默认学期周数（2026年统一改为16周） */
    public static final int DEFAULT_TOTAL_WEEKS = 16;

    /** 短周期课程（不满16周）占比报警阀值 */
    public static final double SHORT_COURSE_WARNING_RATIO = 0.15;

    /** 同一天内教师最大连续授课节数 */
    public static final int MAX_CONSECUTIVE_PERIODS_PER_DAY = 4;

    /** 一节课时长（分钟），预留 */
    public static final int PERIOD_MINUTES = 45;

    // ==================== 学分 → 节次映射 ====================
    // 1-2 学分：2 节课，上午/下午的前两节或三四节
    // 3-4 学分：3 节课，上午后三节(3-5)/下午后三节(8-10)/晚上三节(11-13)
    // 5 学分：  5 节课，单日上午1-5、单日下午6-10，或跨日(2+3)

    /** 1-2 学分允许的节次：每组 [start, end] */
    public static final int[][] TWO_PERIOD_SLOTS = {
        {1, 2},   // 早上前两节
        {3, 4},   // 早上三四节
        {6, 7},   // 下午前两节
        {8, 9},   // 下午三四节
    };

    /** 3-4 学分允许的节次 */
    public static final int[][] THREE_PERIOD_SLOTS = {
        {3, 5},   // 早上后三节
        {8, 10},  // 下午后三节
        {11, 13}, // 晚上三节
    };

    /** 5 学分单日上午 */
    public static final int FULL_MORNING_START = 1;
    public static final int FULL_MORNING_END = 5;

    /** 5 学分单日下午 */
    public static final int FULL_AFTERNOON_START = 6;
    public static final int FULL_AFTERNOON_END = 10;

    // ==================== 课程分类 ====================
    public static final String CATEGORY_COMPULSORY = "必修";
    public static final String CATEGORY_ELECTIVE = "选修";
    public static final String CATEGORY_RESTRICTED_ELECTIVE = "限选";

    // ==================== 角色 ====================
    public static final String ROLE_TEACHER = "TEACHER";

    // ==================== 时间段名称工具 ====================
    public static String getTimeSlotName(int startPeriod, int endPeriod) {
        if (startPeriod >= MORNING_START && endPeriod <= MORNING_END) {
            return "上午";
        } else if (startPeriod >= AFTERNOON_START && endPeriod <= AFTERNOON_END) {
            return "下午";
        } else if (startPeriod >= EVENING_START && endPeriod <= EVENING_END) {
            return "晚上";
        }
        return "跨时段";
    }

    /** 判断两个节次段是否在同一时间段内 */
    public static boolean isSameTimeSlot(int s1, int e1, int s2, int e2) {
        return getTimeSlotName(s1, e1).equals(getTimeSlotName(s2, e2));
    }
}
