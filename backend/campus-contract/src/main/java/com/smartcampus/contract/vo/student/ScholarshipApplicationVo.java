package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScholarshipApplicationVo {

    private Long scholarshipId;
    private String applicationNo;
    private Long studentId;
    private Long studentNo;
    private String studentName;
    private String scholarshipType;
    private String title;
    private String reason;
    private String attachmentUrl;
    private Integer statusCode;
    private String status;
    private Long reviewerId;
    private String reviewerName;
    private String reviewOpinion;
    private LocalDateTime applyTime;
    private LocalDateTime reviewedAt;
    private LocalDateTime selectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
