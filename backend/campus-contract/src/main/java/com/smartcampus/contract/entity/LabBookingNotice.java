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
@TableName("lab_booking_notice")
public class LabBookingNotice implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long noticeId;
    private Long bookingId;
    private Long studentId;
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
