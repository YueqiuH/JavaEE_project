package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StatusChangeApplicationRequest {

    @NotBlank(message = "请选择异动类型")
    @Pattern(regexp = "SUSPENSION|RESUMPTION|MAJOR_CHANGE|WITHDRAWAL", message = "异动类型不正确")
    private String changeType;

    @NotBlank(message = "请填写申请原因")
    @Size(max = 512, message = "申请原因不能超过512个字符")
    private String reason;

    private Long newMajorId;

    @NotNull(message = "请选择期望生效日期")
    @FutureOrPresent(message = "期望生效日期不能早于今天")
    private LocalDate desiredEffectiveDate;
}
