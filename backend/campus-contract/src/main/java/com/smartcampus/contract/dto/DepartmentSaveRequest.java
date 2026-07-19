package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 院系新增/修改请求。
 */
@Data
public class DepartmentSaveRequest implements Serializable {

    @NotBlank(message = "院系名称不能为空")
    @Size(max = 64, message = "院系名称不能超过 64 个字符")
    private String deptName;

    @NotBlank(message = "院系编号不能为空")
    @Size(max = 16, message = "院系编号不能超过 16 个字符")
    private String deptCode;

    /** 院系简介，可为空 */
    private String description;
}
