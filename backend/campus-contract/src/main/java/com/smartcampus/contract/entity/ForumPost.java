package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "forum_post")
public class ForumPost implements Serializable {
    @TableId(type = IdType.AUTO) private Long postId;
    private String title; private String content; private Long authorId;
    private Integer likeCount; private Integer viewCount;
    private Integer status; private Date createTime;
}
