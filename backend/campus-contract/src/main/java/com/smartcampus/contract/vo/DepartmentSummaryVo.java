package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 院系列表视图（含专业数与在读学生数）。
 */
@Data
public class DepartmentSummaryVo implements Serializable {

    private Long deptId;

    private String deptName;

    private String deptCode;

    private String description;

    /** 专业数量 */
    private Long majorCount;

    /** 在读学生数 */
    private Long studentCount;
}
