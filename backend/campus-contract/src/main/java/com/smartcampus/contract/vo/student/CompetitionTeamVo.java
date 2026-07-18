package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CompetitionTeamVo {

    private Long teamId;
    private String registrationNo;
    private Long competitionId;
    private String competitionTitle;
    private LocalDate competitionDeadline;
    private Integer minMembers;
    private Integer maxMembers;
    private Long publisherId;
    private String teamName;
    private Long leaderId;
    private Long leaderNo;
    private String leaderName;
    private String materialOriginalName;
    private String materialContentType;
    private Long materialSize;
    private String materialDescription;
    private Integer statusCode;
    private String status;
    private Long reviewerId;
    private String reviewerName;
    private String reviewOpinion;
    private Integer acceptedMemberCount;
    private Integer invitedMemberCount;
    private LocalDateTime applyTime;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
    private List<CompetitionMemberVo> members;
}
