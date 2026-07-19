package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 专业列表视图（含所属院系名称与在读学生数）。
 */
@Data
public class MajorVo implements Serializable {

    private Long majorId;

    private Long deptId;

    private String deptName;

    private String majorName;

    private String majorCode;

    private String cultivationPlan;

    /** 在读学生数 */
    private Long studentCount;
}
