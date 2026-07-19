package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StudentFeeOverviewVo implements Serializable {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long billCount;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private String paymentStatus;
}
