package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LabBookingNoticeVo {
    private Long noticeId;
    private Long bookingId;
    private String bookingNo;
    private Long studentId;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
