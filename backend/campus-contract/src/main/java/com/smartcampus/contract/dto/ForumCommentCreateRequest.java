package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 论坛回帖请求（回复人取当前登录用户）。
 */
@Data
public class ForumCommentCreateRequest implements Serializable {

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
