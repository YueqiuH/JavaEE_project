package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LabVo {
    private Long labId;
    private String labNo;
    private String labName;
    private String location;
    private Integer capacity;
    private String description;
    private Long managerId;
    private String managerName;
    private Integer statusCode;
    private String status;
    private Long resourceCount;
    private Long upcomingSlotCount;
    private Long activeBookingCount;
    private LocalDate nextOpenDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
