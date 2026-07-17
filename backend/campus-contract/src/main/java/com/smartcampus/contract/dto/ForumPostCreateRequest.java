package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 论坛发帖请求（作者取当前登录用户）。
 */
@Data
public class ForumPostCreateRequest implements Serializable {

    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 128, message = "标题不能超过 128 个字符")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    private String content;
}
