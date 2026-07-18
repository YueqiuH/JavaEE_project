package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "competition_member")
public class CompetitionMember implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long memberId;
    private Long teamId;
    private Long studentId;
    private String role;
    private Integer invitationStatus;
    private Long invitedBy;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}
