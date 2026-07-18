package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LabRequest {

    @NotBlank(message = "请填写实验室名称")
    @Size(max = 64, message = "实验室名称不能超过64个字符")
    private String labName;

    @NotBlank(message = "请填写实验室位置")
    @Size(max = 128, message = "实验室位置不能超过128个字符")
    private String location;

    @NotNull(message = "请填写实验室容量")
    @Min(value = 1, message = "实验室容量不能小于1")
    @Max(value = 500, message = "实验室容量不能超过500")
    private Integer capacity;

    @Size(max = 1000, message = "实验室说明不能超过1000个字符")
    private String description;

    @NotNull(message = "请选择实验室状态")
    @Min(value = 0, message = "实验室状态不正确")
    @Max(value = 1, message = "实验室状态不正确")
    private Integer status;
}
