package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "schedule")
public class Schedule implements Serializable {
    @TableId(type = IdType.AUTO) private Long scheduleId;
    private Long courseId;
    private Long classroomId;
    private Long teacherId;
    private String semester;
    private Integer weekDay;
    private Integer startPeriod;
    private Integer endPeriod;
    private Integer startWeek;
    private Integer endWeek;
    private String scheduleType;
}
