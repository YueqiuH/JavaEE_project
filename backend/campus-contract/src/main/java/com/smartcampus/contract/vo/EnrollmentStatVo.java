package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 招生统计分组项（按院系或按年度）。
 */
@Data
public class EnrollmentStatVo implements Serializable {

    /** 分组名：院系名称或年度 */
    private String label;

    private Long planCount;

    private Long actualCount;

    /** 报到率(%) */
    private BigDecimal reportRate;
}
