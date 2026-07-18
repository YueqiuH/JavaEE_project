package com.smartcampus.app.service.student;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.smartcampus.app.dao.student.CompetitionMapper;
import com.smartcampus.app.dao.student.CompetitionMemberMapper;
import com.smartcampus.app.dao.student.CompetitionTeamMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.CompetitionInvitationResponseRequest;
import com.smartcampus.contract.dto.student.CompetitionRequest;
import com.smartcampus.contract.dto.student.CompetitionReviewRequest;
import com.smartcampus.contract.entity.Competition;
import com.smartcampus.contract.entity.CompetitionMember;
import com.smartcampus.contract.entity.CompetitionTeam;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.CompetitionTeamVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionServiceTest {

    @Mock
    private CompetitionMapper competitionMapper;
    @Mock
    private CompetitionTeamMapper teamMapper;
    @Mock
    private CompetitionMemberMapper memberMapper;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void teacherCannotUpdateAnotherTeachersCompetition() {
        CurrentUserContext.set(teacherSession());
        Competition competition = competition(10L, CompetitionStatus.DRAFT);
        competition.setPublisherId(99L);
        when(competitionMapper.selectById(10L)).thenReturn(competition);

        assertCode(() -> service().updateCompetition(10L, competitionRequest()),
                CompetitionErrorCodes.COMPETITION_NOT_OWNED.getCode());
        verify(competitionMapper, never()).updateById(any(Competition.class));
    }

    @Test
    void studentCannotManageAnotherLeadersTeam() {
        CurrentUserContext.set(studentSession());
        when(memberMapper.selectStudentByNo(600001L)).thenReturn(student(1L, 600001L));
        CompetitionTeam team = team(20L, CompetitionTeamStatus.FORMING);
        team.setLeaderId(99L);
        when(teamMapper.selectById(20L)).thenReturn(team);

        assertCode(() -> service().submitTeam(20L), CompetitionErrorCodes.TEAM_NOT_OWNED.getCode());
        verify(teamMapper, never()).update(isNull(), any(UpdateWrapper.class));
    }

    @Test
    void submittedTeamCannotBeSubmittedAgain() {
        CurrentUserContext.set(studentSession());
        when(memberMapper.selectStudentByNo(600001L)).thenReturn(student(1L, 600001L));
        when(teamMapper.selectById(20L)).thenReturn(team(20L, CompetitionTeamStatus.SUBMITTED));

        assertCode(() -> service().submitTeam(20L), CompetitionErrorCodes.INVALID_STATUS.getCode());
    }

    @Test
    void submissionRequiresMaterialAndValidMemberCount() {
        CurrentUserContext.set(studentSession());
        when(memberMapper.selectStudentByNo(600001L)).thenReturn(student(1L, 600001L));
        CompetitionTeam team = team(20L, CompetitionTeamStatus.FORMING);
        team.setMaterialUrl(null);
        when(teamMapper.selectById(20L)).thenReturn(team);
        when(competitionMapper.selectById(10L)).thenReturn(competition(10L, CompetitionStatus.OPEN));
        when(memberMapper.countAcceptedMembers(20L)).thenReturn(1);

        assertCode(() -> service().submitTeam(20L), CompetitionErrorCodes.MATERIAL_REQUIRED.getCode());
    }

    @Test
    void formingTeamTransitionsToSubmitted() {
        CurrentUserContext.set(studentSession());
        when(memberMapper.selectStudentByNo(600001L)).thenReturn(student(1L, 600001L));
        when(teamMapper.selectById(20L)).thenReturn(team(20L, CompetitionTeamStatus.FORMING));
        when(competitionMapper.selectById(10L)).thenReturn(competition(10L, CompetitionStatus.OPEN));
        when(memberMapper.countAcceptedMembers(20L)).thenReturn(1);
        when(teamMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(teamMapper.selectTeamView(20L)).thenReturn(teamView(20L, CompetitionTeamStatus.SUBMITTED));
        when(memberMapper.selectMembers(20L)).thenReturn(List.of());

        CompetitionTeamVo result = service().submitTeam(20L);

        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        verify(teamMapper).update(isNull(), any(UpdateWrapper.class));
    }

    @Test
    void teacherCannotReviewTeamFromAnotherTeachersCompetition() {
        CurrentUserContext.set(teacherSession());
        when(teamMapper.selectById(20L)).thenReturn(team(20L, CompetitionTeamStatus.SUBMITTED));
        Competition competition = competition(10L, CompetitionStatus.OPEN);
        competition.setPublisherId(99L);
        when(competitionMapper.selectById(10L)).thenReturn(competition);

        assertCode(() -> service().reviewTeam(20L, review("APPROVE", null)),
                CompetitionErrorCodes.COMPETITION_NOT_OWNED.getCode());
    }

    @Test
    void returnedOrRejectedReviewRequiresOpinion() {
        CurrentUserContext.set(teacherSession());
        when(teamMapper.selectById(20L)).thenReturn(team(20L, CompetitionTeamStatus.SUBMITTED));
        when(competitionMapper.selectById(10L)).thenReturn(competition(10L, CompetitionStatus.OPEN));

        assertCode(() -> service().reviewTeam(20L, review("RETURN", " ")),
                CompetitionErrorCodes.REVIEW_OPINION_REQUIRED.getCode());
    }

    @Test
    void teacherApprovalTransitionsSubmittedTeamToApproved() {
        CurrentUserContext.set(teacherSession());
        when(teamMapper.selectById(20L)).thenReturn(team(20L, CompetitionTeamStatus.SUBMITTED));
        when(competitionMapper.selectById(10L)).thenReturn(competition(10L, CompetitionStatus.OPEN));
        when(competitionMapper.countApprovedTeams(10L)).thenReturn(0);
        when(teamMapper.update(isNull(), any(UpdateWrapper.class))).thenReturn(1);
        when(teamMapper.selectTeamView(20L)).thenReturn(teamView(20L, CompetitionTeamStatus.APPROVED));
        when(memberMapper.selectMembers(20L)).thenReturn(List.of());

        CompetitionTeamVo result = service().reviewTeam(20L, review("APPROVE", "材料完整"));

        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void studentCannotRespondToAnotherStudentsInvitation() {
        CurrentUserContext.set(studentSession());
        when(memberMapper.selectStudentByNo(600001L)).thenReturn(student(1L, 600001L));
        CompetitionMember invitation = new CompetitionMember();
        invitation.setMemberId(30L);
        invitation.setStudentId(99L);
        invitation.setInvitationStatus(CompetitionInvitationStatus.INVITED.code());
        when(memberMapper.selectById(30L)).thenReturn(invitation);

        CompetitionInvitationResponseRequest request = new CompetitionInvitationResponseRequest();
        request.setDecision("ACCEPT");
        assertCode(() -> service().respondInvitation(30L, request),
                CompetitionErrorCodes.INVITATION_NOT_OWNED.getCode());
        verify(memberMapper, never()).updateById(any(CompetitionMember.class));
    }

    private CompetitionService service() {
        return new CompetitionService(competitionMapper, teamMapper, memberMapper);
    }

    private void assertCode(Runnable action, int code) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(code);
    }

    private Competition competition(Long id, CompetitionStatus status) {
        Competition competition = new Competition();
        competition.setCompetitionId(id);
        competition.setPublisherId(2L);
        competition.setDeadline(LocalDate.now().plusDays(10));
        competition.setMinMembers(1);
        competition.setMaxMembers(5);
        competition.setMaxTeamCount(10);
        competition.setStatus(status.code());
        return competition;
    }

    private CompetitionTeam team(Long id, CompetitionTeamStatus status) {
        CompetitionTeam team = new CompetitionTeam();
        team.setTeamId(id);
        team.setCompetitionId(10L);
        team.setLeaderId(1L);
        team.setMaterialUrl("https://example.com/material.pdf");
        team.setStatus(status.code());
        return team;
    }

    private CompetitionTeamVo teamView(Long id, CompetitionTeamStatus status) {
        CompetitionTeamVo team = new CompetitionTeamVo();
        team.setTeamId(id);
        team.setStatusCode(status.code());
        return team;
    }

    private CompetitionRequest competitionRequest() {
        CompetitionRequest request = new CompetitionRequest();
        request.setTitle("程序设计竞赛");
        request.setDescription("竞赛介绍");
        request.setRequirements("参赛要求");
        request.setDeadline(LocalDate.now().plusDays(10));
        request.setMinMembers(1);
        request.setMaxMembers(5);
        request.setMaxTeamCount(10);
        return request;
    }

    private CompetitionReviewRequest review(String decision, String opinion) {
        CompetitionReviewRequest request = new CompetitionReviewRequest();
        request.setDecision(decision);
        request.setOpinion(opinion);
        return request;
    }

    private StudentEntity student(Long id, Long number) {
        StudentEntity student = new StudentEntity();
        student.setStudentId(id);
        student.setStudentNo(number);
        return student;
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of(
                "competition:read", "competition:team:read-self", "competition:team:create",
                "competition:team:manage-self", "competition:team:submit-self",
                "competition:invitation:respond-self"), 0);
    }

    private AuthSession teacherSession() {
        return new AuthSession(2L, "700001", 2, Set.of("TEACHER"), Set.of(
                "competition:read", "competition:publish", "competition:manage-self",
                "competition:review:read-self", "competition:review:submit-self"), 0);
    }
}
