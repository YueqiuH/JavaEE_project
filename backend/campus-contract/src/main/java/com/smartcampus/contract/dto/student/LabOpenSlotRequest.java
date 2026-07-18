package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LabOpenSlotRequest {

    @NotNull(message = "请选择开放日期")
    private LocalDate openDate;

    @NotNull(message = "请选择开始节次")
    @Min(value = 1, message = "开始节次不能小于1")
    @Max(value = 12, message = "开始节次不能超过12")
    private Integer startPeriod;

    @NotNull(message = "请选择结束节次")
    @Min(value = 1, message = "结束节次不能小于1")
    @Max(value = 12, message = "结束节次不能超过12")
    private Integer endPeriod;
}
