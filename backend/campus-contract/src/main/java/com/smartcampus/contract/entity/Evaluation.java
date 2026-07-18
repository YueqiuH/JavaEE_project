package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "evaluation")
public class Evaluation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long evaluationId;
    private Long studentId;
    private Long scheduleId;
    private Long teacherId;
    private Long courseId;
    private String semester;
    private Integer scoreTeaching;
    private Integer scoreContent;
    private Integer scoreMethod;
    private String comment;
    private LocalDateTime createTime;
}
