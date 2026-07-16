package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "forum_comment")
public class ForumComment implements Serializable {
    @TableId(type = IdType.AUTO) private Long commentId;
    private Long postId; private Long authorId; private String content;
    private Integer status; private Date createTime;
}
