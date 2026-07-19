package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompetitionReviewRequest {

    @NotBlank(message = "请选择审核结论")
    @Pattern(regexp = "APPROVE|RETURN|REJECT", message = "审核结论不正确")
    private String decision;

    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String opinion;
}
