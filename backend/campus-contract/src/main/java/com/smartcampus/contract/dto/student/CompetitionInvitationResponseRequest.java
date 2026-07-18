package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompetitionInvitationResponseRequest {

    @NotBlank(message = "请选择邀请处理结果")
    @Pattern(regexp = "ACCEPT|DECLINE", message = "邀请处理结果不正确")
    private String decision;
}
