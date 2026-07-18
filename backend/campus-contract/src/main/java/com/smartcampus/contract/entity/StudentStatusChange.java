package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "student_status_change")
public class StudentStatusChange implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long changeId;
    private String applicationNo;
    private Long studentId;
    private String changeType;
    private String reason;
    private Long newMajorId;
    private LocalDate desiredEffectiveDate;
    private Integer status;
    private Long counselorId;
    private String counselorOpinion;
    private LocalDateTime counselorReviewedAt;
    private Long academicReviewerId;
    private String academicOpinion;
    private LocalDateTime academicReviewedAt;
    private LocalDateTime applyTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
