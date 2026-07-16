package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.sql.Date; import java.sql.Time;

@Data @ToString @TableName(value = "meeting")
public class Meeting implements Serializable {
    @TableId(type = IdType.AUTO) private Long meetingId;
    private String title; private String content; private Long initiatorId;
    private Date meetingDate; private Time startTime; private Time endTime;
    private String location; private Date createTime;
}
