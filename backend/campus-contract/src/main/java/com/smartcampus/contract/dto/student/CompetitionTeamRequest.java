package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompetitionTeamRequest {

    @NotBlank(message = "请填写队伍名称")
    @Size(max = 64, message = "队伍名称不能超过64个字符")
    private String teamName;

    @Size(max = 1000, message = "材料说明不能超过1000个字符")
    private String materialDescription;
}
