package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LabResourceVo {
    private Long resourceId;
    private Long labId;
    private String labName;
    private String location;
    private String resourceNo;
    private String resourceName;
    private String resourceType;
    private String resourceTypeLabel;
    private String description;
    private Integer statusCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
