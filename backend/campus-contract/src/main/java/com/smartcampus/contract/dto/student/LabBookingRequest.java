package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LabBookingRequest {

    @NotNull(message = "请选择实验室")
    private Long labId;

    @NotBlank(message = "请填写预约用途")
    @Size(max = 256, message = "预约用途不能超过256个字符")
    private String purpose;
}
