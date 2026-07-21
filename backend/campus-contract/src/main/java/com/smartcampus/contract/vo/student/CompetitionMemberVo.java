package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompetitionMemberVo {

    private Long memberId;
    private Long studentId;
    private Long studentNo;
    private String studentName;
    private String role;
    private Integer invitationStatusCode;
    private String invitationStatus;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}
