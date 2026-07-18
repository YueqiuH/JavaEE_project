package com.smartcampus.contract.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 新闻公告发布/修改请求。
 */
@Data
public class NewsSaveRequest implements Serializable {

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 20000, message = "内容不能超过 20000 个字符")
    private String content;

    /** 类型：公告/新闻 */
    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "公告|新闻", message = "类型只能是公告或新闻")
    private String newsType;

    /** 是否置顶: 1=置顶, 0=普通 */
    @Min(value = 0, message = "置顶标志只能为 0 或 1")
    @Max(value = 1, message = "置顶标志只能为 0 或 1")
    private Integer isPinned;
}
