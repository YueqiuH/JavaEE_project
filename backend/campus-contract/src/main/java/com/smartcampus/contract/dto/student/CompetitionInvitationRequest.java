package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CompetitionInvitationRequest {

    @NotNull(message = "请填写受邀学生学号")
    @Positive(message = "学生学号不正确")
    private Long studentNo;
}
