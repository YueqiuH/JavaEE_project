package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "news")
public class News implements Serializable {
    @TableId(type = IdType.AUTO) private Long newsId;
    private String title; private String content; private String newsType;
    private Long publisherId; private Integer isPinned; private Date createTime;
}
