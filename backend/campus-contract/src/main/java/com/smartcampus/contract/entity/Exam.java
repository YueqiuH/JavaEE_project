package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.sql.Date; import java.sql.Time;

@Data @ToString @TableName(value = "exam")
public class Exam implements Serializable {
    @TableId(type = IdType.AUTO) private Long examId;
    private Long courseId;
    private String examName;
    private String examType;
    private Date examDate;
    private Time startTime;
    private Time endTime;
    private String semester;
}
