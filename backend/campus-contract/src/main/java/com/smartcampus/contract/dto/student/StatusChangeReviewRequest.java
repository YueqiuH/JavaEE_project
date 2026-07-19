package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StatusChangeReviewRequest {

    @NotBlank(message = "请选择审核阶段")
    @Pattern(regexp = "COUNSELOR|ACADEMIC", message = "审核阶段不正确")
    private String stage;

    @NotBlank(message = "请选择审核结论")
    @Pattern(regexp = "APPROVE|REJECT", message = "审核结论不正确")
    private String decision;

    @Size(max = 256, message = "审核意见不能超过256个字符")
    private String opinion;
}
