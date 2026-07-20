package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "schedule")
public class Schedule implements Serializable {
    @TableId(type = IdType.AUTO) private Long scheduleId;
    private Long courseId;
    private Long classroomId;
    private Long teacherId;
    private String semester;
    private Integer weekDay;
    private Integer startPeriod;
    private Integer endPeriod;
    private Integer startWeek;
    private Integer endWeek;
    private String scheduleType;
    /** F1: 自关联——指向同课程第一段排课的 scheduleId。5学分跨日分拆时用于关联两段排课 */
    private Long parentId;
    /** 漏洞5: 周模式 every=每周, odd=单周, even=双周 */
    private String weekPattern;
    /** 漏洞10: 选修课面向年级，排课避开该年级必修时段 */
    private Long targetGradeId;

    /** 课程名称（JOIN 填充） */
    @TableField(exist = false)
    private String courseName;

    /** 教师姓名（JOIN 填充） */
    @TableField(exist = false)
    private String teacherName;

    /** 教室名称（JOIN 填充） */
    @TableField(exist = false)
    private String classroomName;
}
