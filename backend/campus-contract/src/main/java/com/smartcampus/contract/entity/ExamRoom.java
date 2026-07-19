package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "exam_room")
public class ExamRoom implements Serializable {
    @TableId(type = IdType.AUTO) private Long examRoomId;
    private Long examId;
    private Long classroomId;
    /** 座位号（按学号升序编排，1-based） */
    private String seatNo;
    /** 安排的考生ID */
    private Long studentId;
}
