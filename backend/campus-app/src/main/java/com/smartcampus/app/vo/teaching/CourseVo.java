package com.smartcampus.app.vo.teaching;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程搜索返回视图对象 —— 聚合课程基本信息 + 授课教师 + 容量。
 */
@Data
public class CourseVo {

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 课程代码 */
    private String courseCode;

    /** 课程分类 */
    private String classification;

    /** 学分数 */
    private Integer credit;

    /** 授课教师ID */
    private Long teacherId;

    /** 授课教师姓名 */
    private String teacherName;

    /** 最大选课人数 */
    private Integer maxCapacity;

    /** 当前已选人数 */
    private Integer currentCount;

    /** 上课星期几 */
    private Integer weekDay;

    /** 开始节次 */
    private Integer startPeriod;

    /** 结束节次 */
    private Integer endPeriod;

    /** 教室名称 */
    private String classroomName;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
