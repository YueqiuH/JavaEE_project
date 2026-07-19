package com.smartcampus.app.dto.teaching;

import lombok.Data;
import java.util.List;

/**
 * 排课数据传输对象 —— 承载前端提交的排课请求。
 */
@Data
public class ScheduleDto {

    // ==================== 基本信息 ====================
    /** 排课ID（更新时必填，新增时为空） */
    private Long scheduleId;

    /** 课程ID */
    private Long courseId;

    /** 课程名称（冗余字段，用于错误提示） */
    private String courseName;

    /** 课程分类: Compulsory=必修, Elective=选修, RestrictedElective=限选 */
    private String courseCategory;

    /** 学分数 1-5 */
    private Integer credits;

    /** 授课教师ID */
    private Long teacherId;

    // ==================== 时间维度 ====================
    /** 学期，如 "2025-2026-1" */
    private String semester;

    /** 星期几：1-7（周一至周日） */
    private Integer weekDay;

    /** 开始节次 1-13 */
    private Integer startPeriod;

    /** 结束节次 1-13 */
    private Integer endPeriod;

    /** 起始教学周 1-15 */
    private Integer startWeek;

    /** 结束教学周 1-15 */
    private Integer endWeek;

    // ==================== 资源维度 ====================
    /** 教室ID */
    private Long classroomId;

    /** 排课类型：正常/调课/补课 */
    private String scheduleType;

    /** F1: 5学分跨日分拆——指向第一段排课的 scheduleId */
    private Long parentId;

    /** 漏洞5: 周模式 every=每周, odd=单周, even=双周 */
    private String weekPattern;

    /** 漏洞4: 每周上课次数（来自course表，用于学时稽核） */
    private Integer weeklyFrequency;

    /** 漏洞10: 选修课面向年级ID，排课时避开该年级必修课时段 */
    private Long targetGradeId;

    /** 4-5学分课程是否可拆分为多次上课 */
    private Boolean separable;

    // ==================== 跨日分段排课（5学分专用） ====================
    /**
     * 5 学分课程如果走"跨日分段"模式，第二段排课信息在此。
     * 为 null 则表示单日上完（1-5节或6-10节）。
     */
    private List<ScheduleDto> secondSegments;
}
