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
@TableName(value = "competition_team")
public class CompetitionTeam implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long teamId;
    private String registrationNo;
    private Long competitionId;
    private String teamName;
    private Long leaderId;
    private String materialUrl;
    private String materialStorageName;
    private String materialOriginalName;
    private String materialContentType;
    private Long materialSize;
    private String materialDescription;
    private Integer status;
    private Long reviewerId;
    private String reviewOpinion;
    private LocalDateTime applyTime;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
}
