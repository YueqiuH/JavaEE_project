package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 新闻公告列表视图（含发布人姓名）。
 */
@Data
public class NewsVo implements Serializable {

    private Long newsId;

    private String title;

    private String content;

    /** 类型：公告/新闻 */
    private String newsType;

    private Long publisherId;

    /** 发布人姓名（无姓名时为账号） */
    private String publisherName;

    /** 是否置顶: 1=置顶, 0=普通 */
    private Integer isPinned;

    private LocalDateTime createTime;
}
