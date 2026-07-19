package com.smartcampus.app.service.student;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.student.CompetitionMapper;
import com.smartcampus.app.dao.student.CompetitionMemberMapper;
import com.smartcampus.app.dao.student.CompetitionTeamMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.CompetitionInvitationRequest;
import com.smartcampus.contract.dto.student.CompetitionInvitationResponseRequest;
import com.smartcampus.contract.dto.student.CompetitionRequest;
import com.smartcampus.contract.dto.student.CompetitionReviewRequest;
import com.smartcampus.contract.dto.student.CompetitionTeamRequest;
import com.smartcampus.contract.entity.Competition;
import com.smartcampus.contract.entity.CompetitionMember;
import com.smartcampus.contract.entity.CompetitionTeam;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.CompetitionInvitationVo;
import com.smartcampus.contract.vo.student.CompetitionMemberVo;
import com.smartcampus.contract.vo.student.CompetitionTeamVo;
import com.smartcampus.contract.vo.student.CompetitionVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class CompetitionService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final String COUNSELOR_ROLE = "COUNSELOR";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CompetitionMapper competitionMapper;
    private final CompetitionTeamMapper teamMapper;
    private final CompetitionMemberMapper memberMapper;
    private final CompetitionMaterialStorage materialStorage;

    public CompetitionService(
            CompetitionMapper competitionMapper,
            CompetitionTeamMapper teamMapper,
            CompetitionMemberMapper memberMapper,
            CompetitionMaterialStorage materialStorage) {
        this.competitionMapper = competitionMapper;
        this.teamMapper = teamMapper;
        this.memberMapper = memberMapper;
        this.materialStorage = materialStorage;
    }

    public PageResult<CompetitionVo> listCompetitions(long page, long size, String statusName) {
        AuthSession session = requirePermission("competition:read");
        Long publisherId = null;
        Long studentId = null;
        boolean openOnly = false;
        if (session.roles().contains(STUDENT_ROLE)) {
            studentId = requireStudent(session).getStudentId();
            openOnly = true;
        } else if (session.roles().contains(TEACHER_ROLE)) {
            publisherId = session.userId();
        } else if (!isOversight(session)) {
            throw forbidden();
        }
        Page<CompetitionVo> query = new Page<>(page, size);
        IPage<CompetitionVo> result = competitionMapper.selectCompetitionPage(
                query, publisherId, openOnly, studentId, parseCompetitionStatus(statusName));
        result.getRecords().forEach(this::translateCompetition);
        return PageResult.from(result);
    }

    public CompetitionVo getCompetition(Long id) {
        AuthSession session = requirePermission("competition:read");
        Long studentId = session.roles().contains(STUDENT_ROLE) ? requireStudent(session).getStudentId() : null;
        CompetitionVo competition = requireCompetitionView(id, studentId);
        if (session.roles().contains(TEACHER_ROLE) && !session.userId().equals(competition.getPublisherId())) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_OWNED);
        }
        if (session.roles().contains(STUDENT_ROLE)
                && competition.getStatusCode() != CompetitionStatus.OPEN.code()
                && competition.getMyTeamId() == null) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_FOUND);
        }
        return competition;
    }

    @Transactional
    public CompetitionVo createCompetition(CompetitionRequest request) {
        AuthSession teacher = requireTeacher("competition:publish");
        validateMemberRange(request);
        Competition competition = new Competition();
        competition.setCompetitionNo(createNumber("JS"));
        competition.setPublisherId(teacher.userId());
        copyCompetition(request, competition);
        competition.setStatus(CompetitionStatus.DRAFT.code());
        LocalDateTime now = LocalDateTime.now();
        competition.setCreateTime(now);
        competition.setUpdatedAt(now);
        if (competitionMapper.insert(competition) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        return requireCompetitionView(competition.getCompetitionId(), null);
    }

    @Transactional
    public CompetitionVo updateCompetition(Long id, CompetitionRequest request) {
        requireTeacher("competition:manage-self");
        validateMemberRange(request);
        Competition current = requireOwnedCompetition(id);
        requireCompetitionStatus(current, CompetitionStatus.DRAFT);
        Competition update = new Competition();
        update.setCompetitionId(id);
        copyCompetition(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        if (competitionMapper.updateById(update) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return requireCompetitionView(id, null);
    }

    @Transactional
    public CompetitionVo publishCompetition(Long id) {
        requireTeacher("competition:manage-self");
        Competition current = requireOwnedCompetition(id);
        requireCompetitionStatus(current, CompetitionStatus.DRAFT);
        if (current.getDeadline() == null || current.getDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(CompetitionErrorCodes.REGISTRATION_CLOSED);
        }
        LocalDateTime now = LocalDateTime.now();
        updateCompetitionStatus(id, CompetitionStatus.DRAFT, CompetitionStatus.OPEN, now, "published_at");
        return requireCompetitionView(id, null);
    }

    @Transactional
    public CompetitionVo closeCompetition(Long id) {
        requireTeacher("competition:manage-self");
        Competition current = requireOwnedCompetition(id);
        requireCompetitionStatus(current, CompetitionStatus.OPEN);
        LocalDateTime now = LocalDateTime.now();
        updateCompetitionStatus(id, CompetitionStatus.OPEN, CompetitionStatus.CLOSED, now, "closed_at");
        return requireCompetitionView(id, null);
    }

    public PageResult<CompetitionTeamVo> listMyTeams(long page, long size, String statusName) {
        AuthSession session = requireStudent("competition:team:read-self");
        StudentEntity student = requireStudent(session);
        return listTeams(page, size, student.getStudentId(), null, null, parseTeamStatus(statusName));
    }

    public PageResult<CompetitionTeamVo> listReviews(long page, long size, String statusName) {
        AuthSession session = requirePermission("competition:read");
        if (!session.roles().contains(TEACHER_ROLE) && !isOversight(session)) {
            throw forbidden();
        }
        Integer status = statusName == null || statusName.isBlank()
                ? (session.roles().contains(TEACHER_ROLE) ? CompetitionTeamStatus.SUBMITTED.code() : null)
                : parseTeamStatus(statusName);
        Long publisherId = session.roles().contains(TEACHER_ROLE) ? session.userId() : null;
        return listTeams(page, size, null, publisherId, null, status);
    }

    public PageResult<CompetitionTeamVo> listCompetitionTeams(
            Long competitionId, long page, long size, String statusName) {
        AuthSession session = requirePermission("competition:read");
        if (session.roles().contains(TEACHER_ROLE)) {
            requireOwnedCompetition(competitionId);
        } else if (!isOversight(session)) {
            throw forbidden();
        }
        Long publisherId = session.roles().contains(TEACHER_ROLE) ? session.userId() : null;
        return listTeams(page, size, null, publisherId,
                competitionId, parseTeamStatus(statusName));
    }

    public PageResult<CompetitionInvitationVo> listMyInvitations(long page, long size, String statusName) {
        AuthSession session = requireStudent("competition:team:read-self");
        StudentEntity student = requireStudent(session);
        Page<CompetitionInvitationVo> query = new Page<>(page, size);
        IPage<CompetitionInvitationVo> result = memberMapper.selectInvitationPage(
                query, student.getStudentId(), parseInvitationStatus(statusName));
        result.getRecords().forEach(this::translateInvitation);
        return PageResult.from(result);
    }

    public CompetitionTeamVo getTeam(Long id) {
        AuthSession session = CurrentUserContext.require();
        CompetitionTeamVo team = requireTeamView(id);
        if (session.roles().contains(STUDENT_ROLE)) {
            if (!session.hasPermission("competition:team:read-self")) {
                throw forbidden();
            }
            StudentEntity student = requireStudent(session);
            CompetitionMember member = memberMapper.selectTeamMember(id, student.getStudentId());
            if (member == null || member.getInvitationStatus() == CompetitionInvitationStatus.REMOVED.code()) {
                throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_VISIBLE);
            }
        } else if (session.roles().contains(TEACHER_ROLE)) {
            if (!session.hasPermission("competition:review:read-self")) {
                throw forbidden();
            }
            if (!session.userId().equals(team.getPublisherId())) {
                throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_VISIBLE);
            }
        } else if (isOversight(session) && session.hasPermission("competition:oversight:read")) {
            // Oversight roles may inspect every competition and team, but cannot review them.
        } else {
            throw forbidden();
        }
        return team;
    }

    @Transactional
    public CompetitionTeamVo createTeam(Long competitionId, CompetitionTeamRequest request) {
        AuthSession session = requireStudent("competition:team:create");
        StudentEntity student = requireStudent(session);
        Competition competition = requireCompetition(competitionId);
        requireRegistrationOpen(competition);
        if (teamMapper.countStudentActiveTeams(competitionId, student.getStudentId(), null) > 0) {
            throw new BusinessException(CompetitionErrorCodes.ALREADY_IN_TEAM);
        }

        CompetitionTeam team = new CompetitionTeam();
        team.setRegistrationNo(createNumber("TD"));
        team.setCompetitionId(competitionId);
        team.setLeaderId(student.getStudentId());
        copyTeam(request, team);
        team.setStatus(CompetitionTeamStatus.FORMING.code());
        LocalDateTime now = LocalDateTime.now();
        team.setApplyTime(now);
        team.setUpdatedAt(now);
        try {
            if (teamMapper.insert(team) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.REPEATED_REQUESTS);
        }

        CompetitionMember leader = new CompetitionMember();
        leader.setTeamId(team.getTeamId());
        leader.setStudentId(student.getStudentId());
        leader.setRole("队长");
        leader.setInvitationStatus(CompetitionInvitationStatus.ACCEPTED.code());
        leader.setInvitedBy(student.getStudentId());
        leader.setInvitedAt(now);
        leader.setRespondedAt(now);
        if (memberMapper.insert(leader) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        return requireTeamView(team.getTeamId());
    }

    @Transactional
    public CompetitionTeamVo updateTeam(Long id, CompetitionTeamRequest request) {
        AuthSession session = requireStudent("competition:team:manage-self");
        StudentEntity student = requireStudent(session);
        CompetitionTeam current = requireLeaderTeam(id, student.getStudentId());
        requireManageableTeam(current);
        CompetitionTeam update = new CompetitionTeam();
        update.setTeamId(id);
        copyTeam(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        if (teamMapper.updateById(update) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return requireTeamView(id);
    }

    @Transactional
    public CompetitionTeamVo uploadMaterial(Long id, MultipartFile file) {
        AuthSession session = requireStudent("competition:team:manage-self");
        StudentEntity student = requireStudent(session);
        CompetitionTeam current = requireLeaderTeam(id, student.getStudentId());
        requireManageableTeam(current);

        CompetitionMaterialStorage.StoredMaterial stored = materialStorage.store(file);
        CompetitionTeam update = new CompetitionTeam();
        update.setTeamId(id);
        update.setMaterialStorageName(stored.storageName());
        update.setMaterialOriginalName(stored.originalName());
        update.setMaterialContentType(stored.contentType());
        update.setMaterialSize(stored.size());
        update.setMaterialUrl(null);
        update.setUpdatedAt(LocalDateTime.now());
        try {
            if (teamMapper.updateById(update) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
            }
        } catch (RuntimeException exception) {
            materialStorage.deleteQuietly(stored.storageName());
            throw exception;
        }
        materialStorage.deleteQuietly(current.getMaterialStorageName());
        return requireTeamView(id);
    }

    public DownloadMaterial downloadMaterial(Long id) {
        getTeam(id);
        CompetitionTeam team = requireTeam(id);
        if (team.getMaterialStorageName() == null || team.getMaterialStorageName().isBlank()) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_FILE_NOT_FOUND);
        }
        return new DownloadMaterial(
                materialStorage.load(team.getMaterialStorageName()),
                team.getMaterialOriginalName(),
                team.getMaterialContentType(),
                team.getMaterialSize());
    }

    @Transactional
    public CompetitionTeamVo inviteMember(Long id, CompetitionInvitationRequest request) {
        AuthSession session = requireStudent("competition:team:manage-self");
        StudentEntity leader = requireStudent(session);
        CompetitionTeam team = requireLeaderTeam(id, leader.getStudentId());
        requireManageableTeam(team);
        Competition competition = requireCompetition(team.getCompetitionId());
        requireRegistrationOpen(competition);

        StudentEntity invitee = memberMapper.selectStudentByNo(request.getStudentNo());
        if (invitee == null) {
            throw new BusinessException(CompetitionErrorCodes.INVITEE_NOT_FOUND);
        }
        if (invitee.getStudentId().equals(leader.getStudentId())) {
            throw new BusinessException(CompetitionErrorCodes.DUPLICATE_INVITATION);
        }
        if (memberMapper.countActiveMembers(id) >= competition.getMaxMembers()) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_SIZE_INVALID);
        }
        if (teamMapper.countStudentActiveTeams(team.getCompetitionId(), invitee.getStudentId(), id) > 0) {
            throw new BusinessException(CompetitionErrorCodes.ALREADY_IN_TEAM);
        }

        CompetitionMember existing = memberMapper.selectTeamMember(id, invitee.getStudentId());
        LocalDateTime now = LocalDateTime.now();
        if (existing != null && (existing.getInvitationStatus() == CompetitionInvitationStatus.INVITED.code()
                || existing.getInvitationStatus() == CompetitionInvitationStatus.ACCEPTED.code())) {
            throw new BusinessException(CompetitionErrorCodes.DUPLICATE_INVITATION);
        }
        if (existing == null) {
            CompetitionMember invitation = new CompetitionMember();
            invitation.setTeamId(id);
            invitation.setStudentId(invitee.getStudentId());
            invitation.setRole("队员");
            invitation.setInvitationStatus(CompetitionInvitationStatus.INVITED.code());
            invitation.setInvitedBy(leader.getStudentId());
            invitation.setInvitedAt(now);
            if (memberMapper.insert(invitation) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
            }
        } else {
            existing.setRole("队员");
            existing.setInvitationStatus(CompetitionInvitationStatus.INVITED.code());
            existing.setInvitedBy(leader.getStudentId());
            existing.setInvitedAt(now);
            existing.setRespondedAt(null);
            if (memberMapper.updateById(existing) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
            }
        }
        touchTeam(id);
        return requireTeamView(id);
    }

    @Transactional
    public CompetitionInvitationVo respondInvitation(Long memberId, CompetitionInvitationResponseRequest request) {
        AuthSession session = requireStudent("competition:invitation:respond-self");
        StudentEntity student = requireStudent(session);
        CompetitionMember invitation = memberMapper.selectById(memberId);
        if (invitation == null) {
            throw new BusinessException(CompetitionErrorCodes.INVITATION_NOT_FOUND);
        }
        if (!student.getStudentId().equals(invitation.getStudentId())) {
            throw new BusinessException(CompetitionErrorCodes.INVITATION_NOT_OWNED);
        }
        if (invitation.getInvitationStatus() != CompetitionInvitationStatus.INVITED.code()) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
        CompetitionTeam team = requireTeam(invitation.getTeamId());
        requireManageableTeam(team);
        Competition competition = requireCompetition(team.getCompetitionId());
        requireRegistrationOpen(competition);

        CompetitionInvitationStatus target = "ACCEPT".equals(request.getDecision())
                ? CompetitionInvitationStatus.ACCEPTED : CompetitionInvitationStatus.DECLINED;
        if (target == CompetitionInvitationStatus.ACCEPTED) {
            if (teamMapper.countStudentActiveTeams(team.getCompetitionId(), student.getStudentId(), team.getTeamId()) > 0) {
                throw new BusinessException(CompetitionErrorCodes.ALREADY_IN_TEAM);
            }
            if (memberMapper.countAcceptedMembers(team.getTeamId()) >= competition.getMaxMembers()) {
                throw new BusinessException(CompetitionErrorCodes.TEAM_SIZE_INVALID);
            }
        }
        invitation.setInvitationStatus(target.code());
        invitation.setRespondedAt(LocalDateTime.now());
        if (memberMapper.updateById(invitation) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        touchTeam(team.getTeamId());
        return findInvitation(student.getStudentId(), memberId);
    }

    @Transactional
    public CompetitionTeamVo removeMember(Long teamId, Long memberId) {
        AuthSession session = requireStudent("competition:team:manage-self");
        StudentEntity leader = requireStudent(session);
        CompetitionTeam team = requireLeaderTeam(teamId, leader.getStudentId());
        requireManageableTeam(team);
        CompetitionMember member = memberMapper.selectById(memberId);
        if (member == null || !teamId.equals(member.getTeamId())) {
            throw new BusinessException(CompetitionErrorCodes.INVITATION_NOT_FOUND);
        }
        if (member.getStudentId().equals(team.getLeaderId())) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_OWNED);
        }
        member.setInvitationStatus(CompetitionInvitationStatus.REMOVED.code());
        member.setRespondedAt(LocalDateTime.now());
        if (memberMapper.updateById(member) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        touchTeam(teamId);
        return requireTeamView(teamId);
    }

    @Transactional
    public CompetitionTeamVo submitTeam(Long id) {
        AuthSession session = requireStudent("competition:team:submit-self");
        StudentEntity leader = requireStudent(session);
        CompetitionTeam team = requireLeaderTeam(id, leader.getStudentId());
        requireManageableTeam(team);
        Competition competition = requireCompetition(team.getCompetitionId());
        requireRegistrationOpen(competition);
        int acceptedMembers = memberMapper.countAcceptedMembers(id);
        if (acceptedMembers < competition.getMinMembers() || acceptedMembers > competition.getMaxMembers()) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_SIZE_INVALID);
        }
        if (team.getMaterialStorageName() == null || team.getMaterialStorageName().isBlank()) {
            throw new BusinessException(CompetitionErrorCodes.MATERIAL_REQUIRED);
        }
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<CompetitionTeam> update = new UpdateWrapper<CompetitionTeam>()
                .eq("team_id", id)
                .in("status", CompetitionTeamStatus.FORMING.code(), CompetitionTeamStatus.RETURNED.code())
                .set("status", CompetitionTeamStatus.SUBMITTED.code())
                .set("submitted_at", now)
                .set("reviewer_id", null)
                .set("review_opinion", null)
                .set("reviewed_at", null)
                .set("updated_at", now);
        updateTeamStatus(update);
        return requireTeamView(id);
    }

    @Transactional
    public CompetitionTeamVo reviewTeam(Long id, CompetitionReviewRequest request) {
        AuthSession teacher = requireTeacher("competition:review:submit-self");
        CompetitionTeam team = requireTeam(id);
        Competition competition = requireCompetition(team.getCompetitionId());
        if (!teacher.userId().equals(competition.getPublisherId())) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_OWNED);
        }
        if (team.getStatus() != CompetitionTeamStatus.SUBMITTED.code()) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
        CompetitionTeamStatus target = switch (request.getDecision()) {
            case "APPROVE" -> CompetitionTeamStatus.APPROVED;
            case "RETURN" -> CompetitionTeamStatus.RETURNED;
            case "REJECT" -> CompetitionTeamStatus.REJECTED;
            default -> throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        };
        if ((target == CompetitionTeamStatus.RETURNED || target == CompetitionTeamStatus.REJECTED)
                && (request.getOpinion() == null || request.getOpinion().isBlank())) {
            throw new BusinessException(CompetitionErrorCodes.REVIEW_OPINION_REQUIRED);
        }
        if (target == CompetitionTeamStatus.APPROVED
                && competitionMapper.countApprovedTeams(competition.getCompetitionId()) >= competition.getMaxTeamCount()) {
            throw new BusinessException(CompetitionErrorCodes.APPROVAL_CAPACITY_FULL);
        }
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<CompetitionTeam> update = new UpdateWrapper<CompetitionTeam>()
                .eq("team_id", id)
                .eq("status", CompetitionTeamStatus.SUBMITTED.code())
                .set("status", target.code())
                .set("reviewer_id", teacher.userId())
                .set("review_opinion", normalize(request.getOpinion()))
                .set("reviewed_at", now)
                .set("updated_at", now);
        updateTeamStatus(update);
        return requireTeamView(id);
    }

    private PageResult<CompetitionTeamVo> listTeams(
            long page, long size, Long studentId, Long publisherId, Long competitionId, Integer status) {
        Page<CompetitionTeamVo> query = new Page<>(page, size);
        IPage<CompetitionTeamVo> result = teamMapper.selectTeamPage(
                query, studentId, publisherId, competitionId, status);
        result.getRecords().forEach(this::enrichTeam);
        return PageResult.from(result);
    }

    private Competition requireCompetition(Long id) {
        Competition competition = competitionMapper.selectById(id);
        if (competition == null) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_FOUND);
        }
        return competition;
    }

    private Competition requireOwnedCompetition(Long id) {
        Competition competition = requireCompetition(id);
        if (!CurrentUserContext.require().userId().equals(competition.getPublisherId())) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_OWNED);
        }
        return competition;
    }

    private CompetitionVo requireCompetitionView(Long id, Long studentId) {
        CompetitionVo competition = competitionMapper.selectCompetitionView(id, studentId);
        if (competition == null) {
            throw new BusinessException(CompetitionErrorCodes.COMPETITION_NOT_FOUND);
        }
        translateCompetition(competition);
        return competition;
    }

    private CompetitionTeam requireTeam(Long id) {
        CompetitionTeam team = teamMapper.selectById(id);
        if (team == null) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_FOUND);
        }
        return team;
    }

    private CompetitionTeam requireLeaderTeam(Long id, Long studentId) {
        CompetitionTeam team = requireTeam(id);
        if (!studentId.equals(team.getLeaderId())) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_OWNED);
        }
        return team;
    }

    private CompetitionTeamVo requireTeamView(Long id) {
        CompetitionTeamVo team = teamMapper.selectTeamView(id);
        if (team == null) {
            throw new BusinessException(CompetitionErrorCodes.TEAM_NOT_FOUND);
        }
        enrichTeam(team);
        return team;
    }

    private void enrichTeam(CompetitionTeamVo team) {
        team.setStatus(CompetitionTeamStatus.fromCode(team.getStatusCode()).name());
        List<CompetitionMemberVo> members = memberMapper.selectMembers(team.getTeamId());
        members.forEach(member -> member.setInvitationStatus(
                CompetitionInvitationStatus.fromCode(member.getInvitationStatusCode()).name()));
        team.setMembers(members);
    }

    private CompetitionInvitationVo findInvitation(Long studentId, Long memberId) {
        CompetitionInvitationVo invitation = memberMapper.selectInvitation(studentId, memberId);
        if (invitation == null) {
            throw new BusinessException(CompetitionErrorCodes.INVITATION_NOT_FOUND);
        }
        translateInvitation(invitation);
        return invitation;
    }

    private void requireRegistrationOpen(Competition competition) {
        if (competition.getStatus() != CompetitionStatus.OPEN.code()
                || competition.getDeadline() == null
                || competition.getDeadline().isBefore(LocalDate.now())) {
            throw new BusinessException(CompetitionErrorCodes.REGISTRATION_CLOSED);
        }
    }

    private void requireManageableTeam(CompetitionTeam team) {
        if (team.getStatus() != CompetitionTeamStatus.FORMING.code()
                && team.getStatus() != CompetitionTeamStatus.RETURNED.code()) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
    }

    private void requireCompetitionStatus(Competition competition, CompetitionStatus status) {
        if (competition.getStatus() != status.code()) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
    }

    private AuthSession requirePermission(String permission) {
        AuthSession session = CurrentUserContext.require();
        if (!session.hasPermission(permission)) {
            throw forbidden();
        }
        return session;
    }

    private AuthSession requireStudent(String permission) {
        AuthSession session = requirePermission(permission);
        if (!session.roles().contains(STUDENT_ROLE)) {
            throw forbidden();
        }
        return session;
    }

    private AuthSession requireTeacher(String permission) {
        AuthSession session = requirePermission(permission);
        if (!session.roles().contains(TEACHER_ROLE)) {
            throw forbidden();
        }
        return session;
    }

    private boolean isOversight(AuthSession session) {
        return (session.roles().contains(COUNSELOR_ROLE) || session.roles().contains(ADMIN_ROLE))
                && session.hasPermission("competition:oversight:read");
    }

    private StudentEntity requireStudent(AuthSession session) {
        try {
            StudentEntity student = memberMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) {
                return student;
            }
        } catch (NumberFormatException ignored) {
            // Student login names in this project are student numbers.
        }
        throw new BusinessException(CompetitionErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private void updateCompetitionStatus(
            Long id, CompetitionStatus source, CompetitionStatus target, LocalDateTime now, String timestampColumn) {
        UpdateWrapper<Competition> update = new UpdateWrapper<Competition>()
                .eq("competition_id", id)
                .eq("status", source.code())
                .set("status", target.code())
                .set(timestampColumn, now)
                .set("updated_at", now);
        if (competitionMapper.update(null, update) != 1) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
    }

    private void updateTeamStatus(UpdateWrapper<CompetitionTeam> update) {
        if (teamMapper.update(null, update) != 1) {
            throw new BusinessException(CompetitionErrorCodes.INVALID_STATUS);
        }
    }

    private void touchTeam(Long teamId) {
        CompetitionTeam update = new CompetitionTeam();
        update.setTeamId(teamId);
        update.setUpdatedAt(LocalDateTime.now());
        teamMapper.updateById(update);
    }

    private void validateMemberRange(CompetitionRequest request) {
        if (request.getMinMembers() > request.getMaxMembers()) {
            throw new BusinessException(CompetitionErrorCodes.MEMBER_RANGE_INVALID);
        }
    }

    private void copyCompetition(CompetitionRequest request, Competition competition) {
        competition.setTitle(request.getTitle().trim());
        competition.setDescription(request.getDescription().trim());
        competition.setRequirements(request.getRequirements().trim());
        competition.setDeadline(request.getDeadline());
        competition.setMinMembers(request.getMinMembers());
        competition.setMaxMembers(request.getMaxMembers());
        competition.setMaxTeamCount(request.getMaxTeamCount());
    }

    private void copyTeam(CompetitionTeamRequest request, CompetitionTeam team) {
        team.setTeamName(request.getTeamName().trim());
        team.setMaterialDescription(normalize(request.getMaterialDescription()));
    }

    public record DownloadMaterial(Resource resource, String fileName, String contentType, Long size) {
    }

    private Integer parseCompetitionStatus(String name) {
        try {
            CompetitionStatus status = CompetitionStatus.fromName(name);
            return status == null ? null : status.code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private Integer parseTeamStatus(String name) {
        try {
            CompetitionTeamStatus status = CompetitionTeamStatus.fromName(name);
            return status == null ? null : status.code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private Integer parseInvitationStatus(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        try {
            return CompetitionInvitationStatus.valueOf(name.toUpperCase()).code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private void translateCompetition(CompetitionVo competition) {
        competition.setStatus(CompetitionStatus.fromCode(competition.getStatusCode()).name());
    }

    private void translateInvitation(CompetitionInvitationVo invitation) {
        invitation.setInvitationStatus(
                CompetitionInvitationStatus.fromCode(invitation.getInvitationStatusCode()).name());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String createNumber(String prefix) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + LocalDate.now().format(NUMBER_DATE) + suffix;
    }

    private BusinessException forbidden() {
        return new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
    }
}
