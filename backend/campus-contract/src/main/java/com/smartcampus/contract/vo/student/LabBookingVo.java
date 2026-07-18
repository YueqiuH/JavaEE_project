package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LabBookingVo {
    private Long bookingId;
    private String bookingNo;
    private Long labId;
    private String labName;
    private String location;
    private Long resourceId;
    private String resourceNo;
    private String resourceName;
    private String resourceType;
    private Long studentId;
    private Long studentNo;
    private String studentName;
    private LocalDate bookingDate;
    private Integer startPeriod;
    private Integer endPeriod;
    private String purpose;
    private Integer statusCode;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
