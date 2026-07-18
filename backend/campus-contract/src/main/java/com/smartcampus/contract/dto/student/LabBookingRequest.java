package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LabBookingRequest {

    @NotNull(message = "请选择预约资源")
    private Long resourceId;

    @NotNull(message = "请选择预约日期")
    private LocalDate bookingDate;

    @NotNull(message = "请选择开始节次")
    @Min(value = 1, message = "开始节次不能小于1")
    @Max(value = 12, message = "开始节次不能超过12")
    private Integer startPeriod;

    @NotNull(message = "请选择结束节次")
    @Min(value = 1, message = "结束节次不能小于1")
    @Max(value = 12, message = "结束节次不能超过12")
    private Integer endPeriod;

    @NotBlank(message = "请填写预约用途")
    @Size(max = 256, message = "预约用途不能超过256个字符")
    private String purpose;
}
