package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 专业新增/修改请求。
 */
@Data
public class MajorSaveRequest implements Serializable {

    @NotNull(message = "所属院系不能为空")
    private Long deptId;

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 64, message = "专业名称不能超过 64 个字符")
    private String majorName;

    @NotBlank(message = "专业编号不能为空")
    @Size(max = 16, message = "专业编号不能超过 16 个字符")
    private String majorCode;

    /** 培养方案，可为空 */
    private String cultivationPlan;
}
