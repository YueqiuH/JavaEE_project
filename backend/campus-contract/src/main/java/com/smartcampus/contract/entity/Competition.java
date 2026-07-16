package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "competition")
public class Competition implements Serializable {
    @TableId(type = IdType.AUTO) private Long competitionId;
    private String title; private String description; private Long publisherId;
    private Date deadline; private Integer maxTeamCount; private Integer status; private Date createTime;
}
