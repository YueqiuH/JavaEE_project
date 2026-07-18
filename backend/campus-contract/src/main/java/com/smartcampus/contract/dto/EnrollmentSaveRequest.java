package com.smartcampus.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 招生计划录入/修改请求。
 */
@Data
public class EnrollmentSaveRequest implements Serializable {

    @NotNull(message = "专业不能为空")
    private Long majorId;

    @NotNull(message = "年度不能为空")
    @Min(value = 2000, message = "年度不合法")
    @Max(value = 2100, message = "年度不合法")
    private Integer year;

    @NotNull(message = "计划招生人数不能为空")
    @Min(value = 1, message = "计划招生人数不能为 0")
    private Integer planCount;

    /** 实际报到人数，迎新期间可随时更新 */
    @Min(value = 0, message = "实际报到人数不能为负数")
    private Integer actualCount;
}
