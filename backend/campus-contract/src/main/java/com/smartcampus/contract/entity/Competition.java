package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "competition")
public class Competition implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long competitionId;
    private String competitionNo;
    private String title;
    private String description;
    private String requirements;
    private Long publisherId;
    private LocalDate deadline;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer maxTeamCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
}
