package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "meeting_attendee")
public class MeetingAttendee implements Serializable {
    @TableId(type = IdType.AUTO) private Long attendeeId;
    private Long meetingId; private Long userId;
    private String status; private Date replyTime;
}
