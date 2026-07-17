package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StatusChangeApplicationVo {
    private Long changeId;
    private String applicationNo;
    private Long studentId;
    private Long studentNo;
    private String studentName;
    private String changeType;
    private String reason;
    private Long newMajorId;
    private String newMajorName;
    private LocalDate desiredEffectiveDate;
    private Integer statusCode;
    private String status;
    private Long counselorId;
    private String counselorName;
    private String counselorOpinion;
    private LocalDateTime counselorReviewedAt;
    private Long academicReviewerId;
    private String academicReviewerName;
    private String academicOpinion;
    private LocalDateTime academicReviewedAt;
    private LocalDateTime applyTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
