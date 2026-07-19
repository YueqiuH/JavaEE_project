package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛帖子列表视图（含作者姓名与回复数）。
 */
@Data
public class ForumPostVo implements Serializable {

    private Long postId;

    private String title;

    private String content;

    private Long authorId;

    /** 作者姓名（无姓名时为账号） */
    private String authorName;

    private Integer likeCount;

    private Integer viewCount;

    /** 回复数（仅正常状态） */
    private Long commentCount;

    /** 状态：1=正常, 0=已删除, -1=已封禁 */
    private Integer status;

    private LocalDateTime createTime;
}
