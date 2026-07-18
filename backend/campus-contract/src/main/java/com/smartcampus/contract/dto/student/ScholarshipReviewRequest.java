package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScholarshipReviewRequest {

    @NotBlank(message = "请选择评审结论")
    @Pattern(regexp = "APPROVE|RETURN|REJECT", message = "评审结论不正确")
    private String decision;

    @Size(max = 256, message = "评审意见不能超过256个字符")
    private String opinion;
}
