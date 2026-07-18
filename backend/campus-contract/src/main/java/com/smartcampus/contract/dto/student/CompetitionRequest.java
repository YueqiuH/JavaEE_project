package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CompetitionRequest {

    @NotBlank(message = "请填写竞赛名称")
    @Size(max = 128, message = "竞赛名称不能超过128个字符")
    private String title;

    @NotBlank(message = "请填写竞赛介绍")
    @Size(max = 4000, message = "竞赛介绍不能超过4000个字符")
    private String description;

    @NotBlank(message = "请填写参赛要求")
    @Size(max = 4000, message = "参赛要求不能超过4000个字符")
    private String requirements;

    @NotNull(message = "请选择报名截止日期")
    private LocalDate deadline;

    @NotNull(message = "请填写最少队伍人数")
    @Min(value = 1, message = "最少队伍人数不能小于1")
    @Max(value = 10, message = "最少队伍人数不能超过10")
    private Integer minMembers;

    @NotNull(message = "请填写最多队伍人数")
    @Min(value = 1, message = "最多队伍人数不能小于1")
    @Max(value = 10, message = "最多队伍人数不能超过10")
    private Integer maxMembers;

    @NotNull(message = "请填写最大入选队伍数")
    @Min(value = 1, message = "最大入选队伍数不能小于1")
    @Max(value = 200, message = "最大入选队伍数不能超过200")
    private Integer maxTeamCount;
}
