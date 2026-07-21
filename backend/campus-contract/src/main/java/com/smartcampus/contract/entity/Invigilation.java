package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "invigilation")
public class Invigilation implements Serializable {
    @TableId(type = IdType.AUTO) private Long invigilationId;
    private Long examId;
    private Long teacherId;
    private Long classroomId;
    /** 主监考 / 副监考 */
    private String duty;
}
