package com.smartcampus.app.service.student;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.student.ScholarshipMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.ScholarshipApplicationRequest;
import com.smartcampus.contract.dto.student.ScholarshipResultRequest;
import com.smartcampus.contract.dto.student.ScholarshipReviewRequest;
import com.smartcampus.contract.entity.Scholarship;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.ScholarshipApplicationVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ScholarshipService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String COUNSELOR_ROLE = "COUNSELOR";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String REVIEW_PERMISSION = "scholarship:review:read";
    private static final DateTimeFormatter APPLICATION_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ScholarshipMapper scholarshipMapper;

    public ScholarshipService(ScholarshipMapper scholarshipMapper) {
        this.scholarshipMapper = scholarshipMapper;
    }

    public PageResult<ScholarshipApplicationVo> listMine(long page, long size, String statusName) {
        Long studentId = requireStudent().getStudentId();
        return listApplications(page, size, studentId, null, parseStatus(statusName));
    }

    public PageResult<ScholarshipApplicationVo> listForReview(long page, long size, String stage, String statusName) {
        AuthSession session = requireReviewer();
        String reviewStage = stage == null || stage.isBlank()
                ? (session.roles().contains(COUNSELOR_ROLE) ? "COUNSELOR" : "ACADEMIC")
                : stage.toUpperCase();
        if (session.roles().contains(COUNSELOR_ROLE) && !"COUNSELOR".equals(reviewStage)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        if (session.roles().contains(ADMIN_ROLE) && "COUNSELOR".equals(reviewStage)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        Integer status = "COUNSELOR".equals(reviewStage)
                ? ScholarshipStatus.SUBMITTED.code()
                : "ACADEMIC".equals(reviewStage) ? ScholarshipStatus.ACADEMIC_REVIEW.code() : parseStatus(statusName);
        Long counselorUserId = session.roles().contains(COUNSELOR_ROLE) ? session.userId() : null;
        return listApplications(page, size, null, counselorUserId, status);
    }

    public PageResult<ScholarshipApplicationVo> listResults(long page, long size) {
        AuthSession session = CurrentUserContext.require();
        Long studentId = session.roles().contains(STUDENT_ROLE) ? requireStudent().getStudentId() : null;
        if (studentId == null && !session.hasPermission(REVIEW_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        Long counselorUserId = session.roles().contains(COUNSELOR_ROLE) ? session.userId() : null;
        return listApplications(page, size, studentId, counselorUserId, ScholarshipStatus.SELECTED.code());
    }

    public ScholarshipApplicationVo getApplication(Long id) {
        ScholarshipApplicationVo application = requireApplicationView(id);
        AuthSession session = CurrentUserContext.require();
        if (session.roles().contains(STUDENT_ROLE)) {
            requireOwned(application.getStudentId());
        } else if (!isReviewer(session) || !session.hasPermission(REVIEW_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        } else if (session.roles().contains(COUNSELOR_ROLE)) {
            requireCounseledStudent(application.getStudentId(), session.userId());
        }
        return application;
    }

    @Transactional
    public ScholarshipApplicationVo create(ScholarshipApplicationRequest request) {
        StudentEntity student = requireStudent();
        LocalDateTime now = LocalDateTime.now();
        Scholarship application = new Scholarship();
        application.setApplicationNo(createApplicationNo());
        application.setStudentId(student.getStudentId());
        copyEditableFields(request, application);
        application.setStatus(ScholarshipStatus.DRAFT.code());
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        if (scholarshipMapper.insert(application) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        return requireApplicationView(application.getScholarshipId());
    }

    @Transactional
    public ScholarshipApplicationVo update(Long id, ScholarshipApplicationRequest request) {
        Scholarship current = requireOwnedApplication(id);
        requireStatus(current, ScholarshipStatus.DRAFT, ScholarshipStatus.RETURNED);

        Scholarship update = new Scholarship();
        update.setScholarshipId(id);
        copyEditableFields(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        if (scholarshipMapper.updateById(update) != 1) {
            throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return requireApplicationView(id);
    }

    @Transactional
    public ScholarshipApplicationVo submit(Long id) {
        Scholarship current = requireOwnedApplication(id);
        requireStatus(current, ScholarshipStatus.DRAFT, ScholarshipStatus.RETURNED);
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<Scholarship> update = new UpdateWrapper<Scholarship>()
                .eq("scholarship_id", id)
                .eq("status", current.getStatus())
                .set("status", ScholarshipStatus.SUBMITTED.code())
                .set("apply_time", now)
                .set("reviewer_id", null)
                .set("review_opinion", null)
                .set("reviewed_at", null)
                .set("counselor_id", null)
                .set("counselor_opinion", null)
                .set("counselor_reviewed_at", null)
                .set("academic_reviewer_id", null)
                .set("academic_opinion", null)
                .set("academic_reviewed_at", null)
                .set("updated_at", now);
        updateStatus(update);
        return requireApplicationView(id);
    }

    @Transactional
    public ScholarshipApplicationVo withdraw(Long id) {
        Scholarship current = requireOwnedApplication(id);
        requireStatus(current, ScholarshipStatus.DRAFT, ScholarshipStatus.SUBMITTED,
                ScholarshipStatus.ACADEMIC_REVIEW, ScholarshipStatus.RETURNED);
        UpdateWrapper<Scholarship> update = new UpdateWrapper<Scholarship>()
                .eq("scholarship_id", id)
                .eq("status", current.getStatus())
                .set("status", ScholarshipStatus.WITHDRAWN.code())
                .set("updated_at", LocalDateTime.now());
        updateStatus(update);
        return requireApplicationView(id);
    }

    @Transactional
    public ScholarshipApplicationVo review(Long id, ScholarshipReviewRequest request) {
        AuthSession reviewer = requireReviewer();
        Scholarship current = requireApplication(id);
        boolean counselorStage = "COUNSELOR".equals(request.getStage());
        if (counselorStage) {
            if (!reviewer.roles().contains(COUNSELOR_ROLE)) {
                throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
            }
            requireCounseledStudent(current.getStudentId(), reviewer.userId());
            requireStatus(current, ScholarshipStatus.SUBMITTED);
        } else {
            if (!reviewer.roles().contains(ADMIN_ROLE)) {
                throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
            }
            requireStatus(current, ScholarshipStatus.ACADEMIC_REVIEW);
        }
        ScholarshipStatus target = "APPROVE".equals(request.getDecision())
                ? (counselorStage ? ScholarshipStatus.ACADEMIC_REVIEW : ScholarshipStatus.APPROVED)
                : "RETURN".equals(request.getDecision()) ? ScholarshipStatus.RETURNED
                : "REJECT".equals(request.getDecision()) ? ScholarshipStatus.REJECTED
                : null;
        if (target == null) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        if ((target == ScholarshipStatus.RETURNED || target == ScholarshipStatus.REJECTED)
                && (request.getOpinion() == null || request.getOpinion().isBlank())) {
            throw new BusinessException(ScholarshipErrorCodes.REVIEW_OPINION_REQUIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<Scholarship> update = new UpdateWrapper<Scholarship>()
                .eq("scholarship_id", id)
                .eq("status", current.getStatus())
                .set("status", target.code())
                .set("updated_at", now);
        if (counselorStage) {
            update.set("counselor_id", reviewer.userId())
                    .set("counselor_opinion", normalize(request.getOpinion()))
                    .set("counselor_reviewed_at", now);
        } else {
            update.set("academic_reviewer_id", reviewer.userId())
                    .set("academic_opinion", normalize(request.getOpinion()))
                    .set("academic_reviewed_at", now)
                    .set("reviewer_id", reviewer.userId())
                    .set("review_opinion", normalize(request.getOpinion()))
                    .set("reviewed_at", now);
        }
        updateStatus(update);
        return requireApplicationView(id);
    }

    @Transactional
    public List<ScholarshipApplicationVo> generateResults(ScholarshipResultRequest request) {
        Set<Long> ids = new LinkedHashSet<>(request.getApplicationIds());
        LocalDateTime now = LocalDateTime.now();
        for (Long id : ids) {
            Scholarship current = requireApplication(id);
            if (current.getStatus() != ScholarshipStatus.APPROVED.code()) {
                throw new BusinessException(ScholarshipErrorCodes.RESULT_SELECTION_INVALID);
            }
            UpdateWrapper<Scholarship> update = new UpdateWrapper<Scholarship>()
                    .eq("scholarship_id", id)
                    .eq("status", ScholarshipStatus.APPROVED.code())
                    .set("status", ScholarshipStatus.SELECTED.code())
                    .set("selected_at", now)
                    .set("updated_at", now);
            updateStatus(update);
        }
        return ids.stream().map(this::requireApplicationView).toList();
    }

    private PageResult<ScholarshipApplicationVo> listApplications(
            long page, long size, Long studentId, Long counselorUserId, Integer status) {
        Page<ScholarshipApplicationVo> queryPage = new Page<>(page, size);
        IPage<ScholarshipApplicationVo> result = scholarshipMapper.selectApplicationPage(
                queryPage, studentId, counselorUserId, status);
        result.getRecords().forEach(this::translateStatus);
        return PageResult.from(result);
    }

    private Scholarship requireOwnedApplication(Long id) {
        Scholarship application = requireApplication(id);
        requireOwned(application.getStudentId());
        return application;
    }

    private void requireOwned(Long studentId) {
        if (!requireStudent().getStudentId().equals(studentId)) {
            throw new BusinessException(ScholarshipErrorCodes.APPLICATION_NOT_OWNED);
        }
    }

    private AuthSession requireReviewer() {
        AuthSession session = CurrentUserContext.require();
        if (!isReviewer(session) || !session.hasPermission(REVIEW_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private boolean isReviewer(AuthSession session) {
        return session.roles().contains(COUNSELOR_ROLE) || session.roles().contains(ADMIN_ROLE);
    }

    private void requireCounseledStudent(Long studentId, Long counselorUserId) {
        if (scholarshipMapper.countCounseledStudent(studentId, counselorUserId) == 0) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
    }

    private Scholarship requireApplication(Long id) {
        Scholarship application = scholarshipMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ScholarshipErrorCodes.APPLICATION_NOT_FOUND);
        }
        return application;
    }

    private ScholarshipApplicationVo requireApplicationView(Long id) {
        ScholarshipApplicationVo application = scholarshipMapper.selectApplicationById(id);
        if (application == null) {
            throw new BusinessException(ScholarshipErrorCodes.APPLICATION_NOT_FOUND);
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
            StudentEntity student = scholarshipMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) {
                return student;
            }
        } catch (NumberFormatException ignored) {
            // Student login names in this project are student numbers.
        }
        throw new BusinessException(ScholarshipErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private void requireStatus(Scholarship application, ScholarshipStatus... statuses) {
        for (ScholarshipStatus status : statuses) {
            if (application.getStatus() == status.code()) {
                return;
            }
        }
        throw new BusinessException(ScholarshipErrorCodes.INVALID_STATUS);
    }

    private void updateStatus(UpdateWrapper<Scholarship> update) {
        if (scholarshipMapper.update(null, update) != 1) {
            throw new BusinessException(ScholarshipErrorCodes.INVALID_STATUS);
        }
    }

    private Integer parseStatus(String statusName) {
        try {
            ScholarshipStatus status = ScholarshipStatus.fromName(statusName);
            return status == null ? null : status.code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private void translateStatus(ScholarshipApplicationVo application) {
        application.setStatus(ScholarshipStatus.fromCode(application.getStatusCode()).name());
    }

    private void copyEditableFields(ScholarshipApplicationRequest request, Scholarship application) {
        application.setScholarshipType(request.getScholarshipType());
        application.setTitle(request.getTitle().trim());
        application.setReason(request.getReason().trim());
        application.setAttachmentUrl(normalize(request.getAttachmentUrl()));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String createApplicationNo() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ZZD" + LocalDate.now().format(APPLICATION_DATE) + suffix;
    }
}
