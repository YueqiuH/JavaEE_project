package com.smartcampus.contract.dto;

import com.smartcampus.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教职工信息库多条件分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffQuery extends PageParam {

    /** 工号/姓名/电话关键字 */
    private String keyword;

    private Long deptId;

    /** 人员类别: 2=教师, 3=教职工 */
    private Integer userType;

    /** 状态: 1=在职, 0=停用, 2=退休/离职 */
    private Integer status;
}
