package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * D2 招生与迎新统计聚合视图。
 */
@Data
public class EnrollmentStatsVo implements Serializable {

    /** 统计年度 */
    private Integer year;

    /** 全校计划招生数 */
    private Long planTotal;

    /** 全校实际报到数 */
    private Long actualTotal;

    /** 全校报到率(%) */
    private BigDecimal reportRate;

    /** 各院系计划/报到对比 */
    private List<EnrollmentStatVo> byDept;

    /** 历年招生趋势 */
    private List<EnrollmentStatVo> trend;

    /** 该年度新生生源地分布 */
    private List<NameValueVo> originDistribution;
}
