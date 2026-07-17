package com.smartcampus.app.service.student;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.student.StatusChangeMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.StatusChangeApplicationRequest;
import com.smartcampus.contract.dto.student.StatusChangeReviewRequest;
import com.smartcampus.contract.dto.student.StudentProfileUpdateRequest;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.entity.StudentStatusChange;
import com.smartcampus.contract.vo.student.MajorOptionVo;
import com.smartcampus.contract.vo.student.StatusChangeApplicationVo;
import com.smartcampus.contract.vo.student.StudentProfileVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class StatusChangeService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final String REVIEW_PERMISSION = "status:review:read";
    private static final DateTimeFormatter APPLICATION_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StatusChangeMapper statusChangeMapper;

    public StatusChangeService(StatusChangeMapper statusChangeMapper) {
        this.statusChangeMapper = statusChangeMapper;
    }

    public StudentProfileVo getProfile() {
        StudentProfileVo profile = statusChangeMapper.selectProfile(requireStudent().getStudentId());
        if (profile == null) {
            throw new BusinessException(StatusChangeErrorCodes.STUDENT_PROFILE_NOT_FOUND);
        }
        return profile;
    }

    @Transactional
    public StudentProfileVo updateProfile(StudentProfileUpdateRequest request) {
        Long studentId = requireStudent().getStudentId();
        normalizeProfile(request);
        if (statusChangeMapper.upsertProfile(studentId, request) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return statusChangeMapper.selectProfile(studentId);
    }

    public List<MajorOptionVo> listMajors() {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(STUDENT_ROLE) && !session.roles().contains(TEACHER_ROLE)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return statusChangeMapper.selectMajorOptions();
    }

    public PageResult<StatusChangeApplicationVo> listMine(long page, long size, String statusName) {
        return listApplications(page, size, requireStudent().getStudentId(), parseStatus(statusName));
    }

    public PageResult<StatusChangeApplicationVo> listForReview(long page, long size, String stage) {
        Integer status = switch (stage == null ? "" : stage.toUpperCase()) {
            case "COUNSELOR" -> StatusChangeStatus.COUNSELOR_REVIEW.code();
            case "ACADEMIC" -> StatusChangeStatus.ACADEMIC_REVIEW.code();
            default -> throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        };
        return listApplications(page, size, null, status);
    }

    public StatusChangeApplicationVo getApplication(Long id) {
        StatusChangeApplicationVo application = requireApplicationView(id);
        AuthSession session = CurrentUserContext.require();
        if (session.roles().contains(STUDENT_ROLE)) {
            requireOwned(application.getStudentId());
        } else if (!session.hasPermission(REVIEW_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return application;
    }

    @Transactional
    public StatusChangeApplicationVo create(StatusChangeApplicationRequest request) {
        validateApplicationRequest(request);
        LocalDateTime now = LocalDateTime.now();
        StudentStatusChange application = new StudentStatusChange();
        application.setApplicationNo(createApplicationNo());
        application.setStudentId(requireStudent().getStudentId());
        copyEditableFields(request, application);
        application.setStatus(StatusChangeStatus.DRAFT.code());
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        if (statusChangeMapper.insert(application) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        return requireApplicationView(application.getChangeId());
    }

    @Transactional
    public StatusChangeApplicationVo update(Long id, StatusChangeApplicationRequest request) {
        validateApplicationRequest(request);
        StudentStatusChange current = requireOwnedApplication(id);
        requireStatus(current, StatusChangeStatus.DRAFT);
        StudentStatusChange update = new StudentStatusChange();
        update.setChangeId(id);
        copyEditableFields(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        if (statusChangeMapper.updateById(update) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return requireApplicationView(id);
    }

    @Transactional
    public StatusChangeApplicationVo submit(Long id) {
        StudentStatusChange current = requireOwnedApplication(id);
        requireStatus(current, StatusChangeStatus.DRAFT);
        if (statusChangeMapper.countOtherActiveApplications(current.getStudentId(), id) > 0) {
            throw new BusinessException(StatusChangeErrorCodes.ACTIVE_APPLICATION_EXISTS);
        }
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<StudentStatusChange> update = new UpdateWrapper<StudentStatusChange>()
                .eq("change_id", id)
                .eq("status", StatusChangeStatus.DRAFT.code())
                .set("status", StatusChangeStatus.COUNSELOR_REVIEW.code())
                .set("apply_time", now)
                .set("updated_at", now);
        updateStatus(update);
        return requireApplicationView(id);
    }

    @Transactional
    public StatusChangeApplicationVo withdraw(Long id) {
        StudentStatusChange current = requireOwnedApplication(id);
        requireStatus(current, StatusChangeStatus.DRAFT, StatusChangeStatus.COUNSELOR_REVIEW);
        UpdateWrapper<StudentStatusChange> update = new UpdateWrapper<StudentStatusChange>()
                .eq("change_id", id)
                .eq("status", current.getStatus())
                .set("status", StatusChangeStatus.WITHDRAWN.code())
                .set("updated_at", LocalDateTime.now());
        updateStatus(update);
        return requireApplicationView(id);
    }

    @Transactional
    public StatusChangeApplicationVo review(Long id, StatusChangeReviewRequest request) {
        StudentStatusChange current = requireApplication(id);
        boolean counselorStage = "COUNSELOR".equals(request.getStage());
        StatusChangeStatus expected = counselorStage
                ? StatusChangeStatus.COUNSELOR_REVIEW : StatusChangeStatus.ACADEMIC_REVIEW;
        requireStatus(current, expected);
        if ("REJECT".equals(request.getDecision()) && isBlank(request.getOpinion())) {
            throw new BusinessException(StatusChangeErrorCodes.REVIEW_OPINION_REQUIRED);
        }

        StatusChangeStatus target;
        if (counselorStage) {
            target = "APPROVE".equals(request.getDecision())
                    ? StatusChangeStatus.ACADEMIC_REVIEW : StatusChangeStatus.COUNSELOR_REJECTED;
        } else {
            target = "APPROVE".equals(request.getDecision())
                    ? StatusChangeStatus.APPROVED : StatusChangeStatus.ACADEMIC_REJECTED;
        }

        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<StudentStatusChange> update = new UpdateWrapper<StudentStatusChange>()
                .eq("change_id", id)
                .eq("status", expected.code())
                .set("status", target.code())
                .set("updated_at", now);
        if (counselorStage) {
            update.set("counselor_id", CurrentUserContext.require().userId())
                    .set("counselor_opinion", normalize(request.getOpinion()))
                    .set("counselor_reviewed_at", now);
        } else {
            update.set("academic_reviewer_id", CurrentUserContext.require().userId())
                    .set("academic_opinion", normalize(request.getOpinion()))
                    .set("academic_reviewed_at", now);
        }
        updateStatus(update);
        return requireApplicationView(id);
    }

    private PageResult<StatusChangeApplicationVo> listApplications(long page, long size, Long studentId, Integer status) {
        Page<StatusChangeApplicationVo> queryPage = new Page<>(page, size);
        IPage<StatusChangeApplicationVo> result = statusChangeMapper.selectApplicationPage(queryPage, studentId, status);
        result.getRecords().forEach(this::translateStatus);
        return PageResult.from(result);
    }

    private StudentStatusChange requireOwnedApplication(Long id) {
        StudentStatusChange application = requireApplication(id);
        requireOwned(application.getStudentId());
        return application;
    }

    private void requireOwned(Long studentId) {
        if (!requireStudent().getStudentId().equals(studentId)) {
            throw new BusinessException(StatusChangeErrorCodes.APPLICATION_NOT_OWNED);
        }
    }

    private StudentStatusChange requireApplication(Long id) {
        StudentStatusChange application = statusChangeMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(StatusChangeErrorCodes.APPLICATION_NOT_FOUND);
        }
        return application;
    }

    private StatusChangeApplicationVo requireApplicationView(Long id) {
        StatusChangeApplicationVo application = statusChangeMapper.selectApplicationById(id);
        if (application == null) {
            throw new BusinessException(StatusChangeErrorCodes.APPLICATION_NOT_FOUND);
        }
        translateStatus(application);
        return application;
    }

    private StudentEntity requireStudent() {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(STUDENT_ROLE)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        try {
            StudentEntity student = statusChangeMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) {
                return student;
            }
        } catch (NumberFormatException ignored) {
            // Student login names in this project are student numbers.
        }
        throw new BusinessException(StatusChangeErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private void validateApplicationRequest(StatusChangeApplicationRequest request) {
        if ("MAJOR_CHANGE".equals(request.getChangeType())) {
            if (request.getNewMajorId() == null) {
                throw new BusinessException(StatusChangeErrorCodes.MAJOR_REQUIRED);
            }
            if (statusChangeMapper.countMajor(request.getNewMajorId()) == 0) {
                throw new BusinessException(StatusChangeErrorCodes.MAJOR_NOT_FOUND);
            }
        } else {
            request.setNewMajorId(null);
        }
    }

    private void requireStatus(StudentStatusChange application, StatusChangeStatus... statuses) {
        for (StatusChangeStatus status : statuses) {
            if (application.getStatus() != null && application.getStatus() == status.code()) {
                return;
            }
        }
        throw new BusinessException(StatusChangeErrorCodes.INVALID_STATUS);
    }

    private void updateStatus(UpdateWrapper<StudentStatusChange> update) {
        if (statusChangeMapper.update(null, update) != 1) {
            throw new BusinessException(StatusChangeErrorCodes.INVALID_STATUS);
        }
    }

    private Integer parseStatus(String statusName) {
        try {
            StatusChangeStatus status = StatusChangeStatus.fromName(statusName);
            return status == null ? null : status.code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private void translateStatus(StatusChangeApplicationVo application) {
        application.setStatus(StatusChangeStatus.fromCode(application.getStatusCode()).name());
    }

    private void copyEditableFields(StatusChangeApplicationRequest request, StudentStatusChange application) {
        application.setChangeType(request.getChangeType());
        application.setReason(request.getReason().trim());
        application.setNewMajorId(request.getNewMajorId());
        application.setDesiredEffectiveDate(request.getDesiredEffectiveDate());
    }

    private void normalizeProfile(StudentProfileUpdateRequest request) {
        request.setCurrentAddress(normalize(request.getCurrentAddress()));
        request.setPhone(normalize(request.getPhone()));
        request.setEmail(normalize(request.getEmail()));
        request.setEmergencyContact(normalize(request.getEmergencyContact()));
        request.setEmergencyPhone(normalize(request.getEmergencyPhone()));
    }

    private String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String createApplicationNo() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "XJ" + LocalDate.now().format(APPLICATION_DATE) + suffix;
    }
}
