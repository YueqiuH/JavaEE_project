package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 自然语言报表请求。
 */
@Data
public class AiReportRequest implements Serializable {

    /** 自然语言问题，如：统计今年各院系新生报到率并对比去年 */
    @NotBlank(message = "问题不能为空")
    @Size(max = 500, message = "问题不能超过 500 个字符")
    private String question;
}
