package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompetitionVo {

    private Long competitionId;
    private String competitionNo;
    private String title;
    private String description;
    private String requirements;
    private Long publisherId;
    private String publisherName;
    private LocalDate deadline;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer maxTeamCount;
    private Integer statusCode;
    private String status;
    private Long teamCount;
    private Long approvedTeamCount;
    private Long myTeamId;
    private LocalDateTime createTime;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime closedAt;
}
