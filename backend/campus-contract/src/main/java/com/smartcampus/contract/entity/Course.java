package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "course")
public class Course implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long courseId;
    private String courseName;
    private String courseCode;
    /** 必修 / 选修 / 限选 */
    private String classification;
    /** 学分 1-5，支持小数如 2.5 */
    private BigDecimal credit;
    /** 每周上课次数：1 或 2，绝不允许 3 */
    private Integer weeklyFrequency;
    /** F3: 先修课程ID，自关联course表。选课时需校验该先修课已及格 */
    private Long prerequisiteId;
    /** 漏洞7: 是否开课 1=正常, 0=停开。停开的课程在课表和学分计算中自动过滤 */
    private Integer isActive;
    private LocalDateTime createdAt;
}
