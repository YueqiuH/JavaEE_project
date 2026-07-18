package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LabResourceRequest {

    @NotBlank(message = "请填写资源编号")
    @Size(max = 32, message = "资源编号不能超过32个字符")
    private String resourceNo;

    @NotBlank(message = "请填写资源名称")
    @Size(max = 64, message = "资源名称不能超过64个字符")
    private String resourceName;

    @NotBlank(message = "请选择资源类型")
    @Pattern(regexp = "EQUIPMENT|WORKSTATION", message = "资源类型不正确")
    private String resourceType;

    @Size(max = 500, message = "资源说明不能超过500个字符")
    private String description;

    @NotNull(message = "请选择资源状态")
    @Min(value = 0, message = "资源状态不正确")
    @Max(value = 1, message = "资源状态不正确")
    private Integer status;
}
