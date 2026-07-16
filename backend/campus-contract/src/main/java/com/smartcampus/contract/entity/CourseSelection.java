package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.util.Date;

@Data @ToString @TableName(value = "course_selection")
public class CourseSelection implements Serializable {
    @TableId(type = IdType.AUTO) private Long selectionId;
    private Long studentId;
    private Long courseId;
    private Long scheduleId;
    private String semester;
    private Integer status;
    private Date selectTime;
}
