package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 论坛回帖请求（回复人取当前登录用户）。
 */
@Data
public class ForumCommentCreateRequest implements Serializable {

    @NotBlank(message = "回复内容不能为空")
    @Size(max = 2000, message = "回复内容不能超过 2000 个字符")
    private String content;
}
