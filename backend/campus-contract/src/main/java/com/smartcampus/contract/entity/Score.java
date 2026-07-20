package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;

@Data @ToString @TableName(value = "score")
public class Score implements Serializable {
    @TableId(type = IdType.AUTO) private Long scoreId;
    private Long studentId;
    private Long courseId;
    private BigDecimal regularScore;
    private BigDecimal examScore;
    private Integer scoreScore;
    private String semester;
    private BigDecimal gpa;
    private BigDecimal regularRatio;
    private BigDecimal examRatio;
    private Long scheduleId;
    private Long teacherId;
    private Integer publishStatus;
    private Integer status;

    /** 学生姓名（JOIN 填充） */
    @TableField(exist = false)
    private String studentName;

    /** 学号（JOIN 填充） */
    @TableField(exist = false)
    private String studentNo;

    /** 班级（JOIN 填充） */
    @TableField(exist = false)
    private String className;

    /** 专业名称（JOIN 填充） */
    @TableField(exist = false)
    private String majorName;
}
