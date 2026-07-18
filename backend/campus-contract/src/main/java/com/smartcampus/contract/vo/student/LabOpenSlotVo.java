package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LabOpenSlotVo {
    private Long slotId;
    private Long labId;
    private String labName;
    private String location;
    private LocalDate openDate;
    private Integer startPeriod;
    private Integer endPeriod;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
