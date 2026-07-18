package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CompetitionInvitationVo {

    private Long memberId;
    private Long teamId;
    private String teamName;
    private Long competitionId;
    private String competitionTitle;
    private LocalDate deadline;
    private Long leaderNo;
    private String leaderName;
    private Integer invitationStatusCode;
    private String invitationStatus;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}
