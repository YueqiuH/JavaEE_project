package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EvaluationSubmissionRequest {

    @NotNull(message = "请评价教学态度")
    @Min(value = 1, message = "教学态度评分不能低于1分")
    @Max(value = 5, message = "教学态度评分不能高于5分")
    private Integer scoreTeaching;

    @NotNull(message = "请评价教学内容")
    @Min(value = 1, message = "教学内容评分不能低于1分")
    @Max(value = 5, message = "教学内容评分不能高于5分")
    private Integer scoreContent;

    @NotNull(message = "请评价教学方法")
    @Min(value = 1, message = "教学方法评分不能低于1分")
    @Max(value = 5, message = "教学方法评分不能高于5分")
    private Integer scoreMethod;

    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String comment;
}
