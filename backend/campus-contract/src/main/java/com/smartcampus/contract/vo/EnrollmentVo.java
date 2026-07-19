package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 招生计划列表视图（含专业/院系名称）。
 */
@Data
public class EnrollmentVo implements Serializable {

    private Long enrollmentId;

    private Long majorId;

    private String majorName;

    private Long deptId;

    private String deptName;

    /** 年度 */
    private Integer year;

    /** 计划招生人数 */
    private Integer planCount;

    /** 实际报到人数 */
    private Integer actualCount;

    /** 报到率(%) */
    private BigDecimal reportRate;
}
