package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@TableName("lab_booking_period")
public class LabBookingPeriod implements Serializable {
    private Long bookingId;
    private Long resourceId;
    private Long studentId;
    private LocalDate bookingDate;
    private Integer periodNo;
}
