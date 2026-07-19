package com.smartcampus.app.validator;

import com.smartcampus.app.config.CourseScheduleConfig;
import com.smartcampus.app.dto.teaching.ScheduleDto;
import com.smartcampus.app.exception.ScheduleConflictException;

import java.util.List;

/**
 * 排课规则校验器。
 * <p>新规则：不限学分与节次的刚性绑定，仅校验时段合法性 + 可选拆分模式 + 学时稽核。</p>
 */
public final class ScheduleValidator {

    private ScheduleValidator() {}

    /**
     * 全量校验入口。
     */
    public static void validateSchedule(ScheduleDto dto, long existingCount, long shortCourseCount) {
        // 0. 基础参数
        validateBasicParams(dto);
        // 1. 禁止跨时段
        validateNoCrossTimeSlot(dto);
        // 2. 拆分模式
        if (Boolean.TRUE.equals(dto.getSeparable()) && dto.getCredits() != null && dto.getCredits() >= 4) {
            validateSplitMode(dto);
        }
        // 3. 短周期报警
        checkShortCourseRatio(dto, existingCount, shortCourseCount);
    }

    // ========== 基础参数 ==========

    private static void validateBasicParams(ScheduleDto dto) {
        if (dto.getCourseId() == null) throw new ScheduleConflictException("SCHEDULE_001", "课程ID不能为空");
        if (dto.getTeacherId() == null) throw new ScheduleConflictException("SCHEDULE_002", "授课教师ID不能为空");
        if (dto.getCredits() == null || dto.getCredits() < 1 || dto.getCredits() > 5)
            throw new ScheduleConflictException("SCHEDULE_003", "学分必须在 1-5 之间，当前值: " + dto.getCredits());
        if (dto.getWeekDay() == null || dto.getWeekDay() < 1 || dto.getWeekDay() > 7)
            throw new ScheduleConflictException("SCHEDULE_004", "星期必须在 1-7 之间");
        if (dto.getStartPeriod() == null || dto.getStartPeriod() < 1 || dto.getStartPeriod() > 13)
            throw new ScheduleConflictException("SCHEDULE_005", "开始节次必须在 1-13 之间");
        if (dto.getEndPeriod() == null || dto.getEndPeriod() < 1 || dto.getEndPeriod() > 13)
            throw new ScheduleConflictException("SCHEDULE_006", "结束节次必须在 1-13 之间");
        if (dto.getStartPeriod() >= dto.getEndPeriod())
            throw new ScheduleConflictException("SCHEDULE_007", "开始节次必须小于结束节次");
        if (dto.getStartWeek() == null || dto.getStartWeek() < 1 || dto.getStartWeek() > 16)
            throw new ScheduleConflictException("SCHEDULE_008", "起始周必须在 1-16 之间");
        if (dto.getEndWeek() == null || dto.getEndWeek() < 1 || dto.getEndWeek() > 16)
            throw new ScheduleConflictException("SCHEDULE_009", "结束周必须在 1-16 之间");
        if (dto.getStartWeek() > dto.getEndWeek())
            throw new ScheduleConflictException("SCHEDULE_010", "起始周不能大于结束周");
    }

    // ========== 禁止跨时段 ==========

    private static void validateNoCrossTimeSlot(ScheduleDto dto) {
        String slot = CourseScheduleConfig.getTimeSlotName(dto.getStartPeriod(), dto.getEndPeriod());
        if ("跨时段".equals(slot)) {
            throw new ScheduleConflictException("SCHEDULE_014",
                String.format("课程《%s》不允许跨时间段排课（上午/下午/晚上不可混排）。当前：第%d-%d节",
                    dto.getCourseName(), dto.getStartPeriod(), dto.getEndPeriod()));
        }
    }

    // ========== 4-5学分拆分模式 ==========

    private static void validateSplitMode(ScheduleDto dto) {
        List<ScheduleDto> segments = dto.getSecondSegments();
        if (segments == null || segments.isEmpty()) return; // 不拆分，只校验不跨时段

        if (segments.size() > 1)
            throw new ScheduleConflictException("SCHEDULE_018",
                "拆分模式最多允许 2 段（1段主排 + 1段附加），当前" + (segments.size() + 1) + "段");

        ScheduleDto seg = segments.get(0);
        if (dto.getWeekDay().equals(seg.getWeekDay()))
            throw new ScheduleConflictException("SCHEDULE_019",
                "拆分模式两段必须在不同日期，当前都排在星期" + dto.getWeekDay());

        validateNoCrossTimeSlot(dto);
        validateNoCrossTimeSlot(seg);
    }

    // ========== 短周期报警 ==========

    private static void checkShortCourseRatio(ScheduleDto dto, long total, long shortCount) {
        boolean isShort = (dto.getEndWeek() - dto.getStartWeek() + 1) < CourseScheduleConfig.DEFAULT_TOTAL_WEEKS;
        long ns = isShort ? shortCount + 1 : shortCount;
        long nt = total + 1;
        if (nt > 0) {
            double r = (double) ns / nt;
            if (r > CourseScheduleConfig.SHORT_COURSE_WARNING_RATIO) {
                System.err.printf("[排课报警] 短周期课程占比 %.1f%% 已超 %.0f%% 阀值%n",
                    r * 100, CourseScheduleConfig.SHORT_COURSE_WARNING_RATIO * 100);
            }
        }
    }
}
