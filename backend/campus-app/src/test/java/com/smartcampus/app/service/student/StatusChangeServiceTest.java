package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.StatusChangeMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.StatusChangeApplicationRequest;
import com.smartcampus.contract.dto.student.StatusChangeReviewRequest;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.entity.StudentStatusChange;
import com.smartcampus.contract.vo.student.StatusChangeApplicationVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusChangeServiceTest {

    @Mock
    private StatusChangeMapper statusChangeMapper;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void studentCannotSubmitAnotherStudentsApplication() {
        CurrentUserContext.set(studentSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 99L, StatusChangeStatus.DRAFT));
        when(statusChangeMapper.selectStudentByNo(600001L)).thenReturn(student(1L));

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.submit(10L))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.APPLICATION_NOT_OWNED.getCode());
        verify(statusChangeMapper, never()).update(any(), any());
    }

    @Test
    void studentCannotSubmitWhileAnotherApplicationIsUnderReview() {
        CurrentUserContext.set(studentSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.DRAFT));
        when(statusChangeMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        when(statusChangeMapper.countOtherActiveApplications(1L, 10L)).thenReturn(1);

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.submit(10L))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.ACTIVE_APPLICATION_EXISTS.getCode());
    }

    @Test
    void majorChangeRequiresTargetMajor() {
        CurrentUserContext.set(studentSession());
        StatusChangeApplicationRequest request = validRequest();
        request.setChangeType("MAJOR_CHANGE");

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.MAJOR_REQUIRED.getCode());
    }

    @Test
    void academicReviewCannotSkipCounselorStage() {
        CurrentUserContext.set(teacherSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.COUNSELOR_REVIEW));
        StatusChangeReviewRequest request = reviewRequest("ACADEMIC", "APPROVE", null);

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.review(10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.INVALID_STATUS.getCode());
    }

    @Test
    void rejectingAtEitherStageRequiresOpinion() {
        CurrentUserContext.set(teacherSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.COUNSELOR_REVIEW));
        StatusChangeReviewRequest request = reviewRequest("COUNSELOR", "REJECT", "");

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.review(10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.REVIEW_OPINION_REQUIRED.getCode());
    }

    @Test
    void counselorApprovalMovesApplicationToAcademicReview() {
        CurrentUserContext.set(teacherSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.COUNSELOR_REVIEW));
        when(statusChangeMapper.update(any(), any())).thenReturn(1);
        StatusChangeApplicationVo view = new StatusChangeApplicationVo();
        view.setChangeId(10L);
        view.setStatusCode(StatusChangeStatus.ACADEMIC_REVIEW.code());
        when(statusChangeMapper.selectApplicationById(10L)).thenReturn(view);
        StatusChangeReviewRequest request = reviewRequest("COUNSELOR", "APPROVE", "材料齐全");

        StatusChangeService service = new StatusChangeService(statusChangeMapper);
        StatusChangeApplicationVo result = service.review(10L, request);

        assertThat(result.getStatus()).isEqualTo("ACADEMIC_REVIEW");
        verify(statusChangeMapper).update(any(), any());
    }

    private StatusChangeApplicationRequest validRequest() {
        StatusChangeApplicationRequest request = new StatusChangeApplicationRequest();
        request.setChangeType("SUSPENSION");
        request.setReason("因个人原因申请休学");
        request.setDesiredEffectiveDate(LocalDate.now().plusDays(7));
        return request;
    }

    private StatusChangeReviewRequest reviewRequest(String stage, String decision, String opinion) {
        StatusChangeReviewRequest request = new StatusChangeReviewRequest();
        request.setStage(stage);
        request.setDecision(decision);
        request.setOpinion(opinion);
        return request;
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of(), 0);
    }

    private AuthSession teacherSession() {
        return new AuthSession(2L, "700001", 2, Set.of("TEACHER"), Set.of("status:review:read"), 0);
    }

    private StudentEntity student(Long id) {
        StudentEntity student = new StudentEntity();
        student.setStudentId(id);
        student.setStudentNo(600001L);
        return student;
    }

    private StudentStatusChange application(Long id, Long studentId, StatusChangeStatus status) {
        StudentStatusChange application = new StudentStatusChange();
        application.setChangeId(id);
        application.setStudentId(studentId);
        application.setStatus(status.code());
        return application;
    }
}
