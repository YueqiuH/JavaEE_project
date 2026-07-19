package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛回复视图（含回复人姓名）。
 */
@Data
public class ForumCommentVo implements Serializable {

    private Long commentId;

    private Long postId;

    private Long authorId;

    /** 回复人姓名（无姓名时为账号） */
    private String authorName;

    private String content;

    /** 状态：1=正常, 0=已删除 */
    private Integer status;

    private LocalDateTime createTime;
}
