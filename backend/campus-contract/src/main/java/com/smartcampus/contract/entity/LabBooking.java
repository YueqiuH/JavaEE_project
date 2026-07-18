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
@TableName(value = "lab_booking")
public class LabBooking implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long bookingId;
    private String bookingNo;
    private Long labId;
    private Long resourceId;
    private Long studentId;
    private LocalDate bookingDate;
    private Integer startPeriod;
    private Integer endPeriod;
    private String purpose;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private LocalDateTime expiresAt;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;
}
