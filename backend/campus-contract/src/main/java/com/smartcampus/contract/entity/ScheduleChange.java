package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.util.Date;

@Data @ToString @TableName(value = "schedule_change")
public class ScheduleChange implements Serializable {
    @TableId(type = IdType.AUTO) private Long changeId;
    private Long scheduleId;
    private Long applyUserId;
    private String reason;
    private Integer newWeekDay;
    private Integer newStartPeriod;
    private Integer newEndPeriod;
    private Long newClassroomId;
    private Integer status;
    private Long approveUserId;
    private String approveRemark;
    private Date createTime;
}
