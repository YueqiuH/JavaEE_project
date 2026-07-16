package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "notification")
public class Notification implements Serializable {
    @TableId(type = IdType.AUTO) private Long notifyId;
    private Long userId; private String title; private String content;
    private String notifyType; private Integer isRead; private Date createTime;
}
