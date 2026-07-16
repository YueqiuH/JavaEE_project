package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "classroom")
public class Classroom implements Serializable {
    @TableId(type = IdType.AUTO) private Long classroomId;
    private String classroomName;
    private String building;
    private Integer capacity;
    private String type;
    private Integer status;
}
