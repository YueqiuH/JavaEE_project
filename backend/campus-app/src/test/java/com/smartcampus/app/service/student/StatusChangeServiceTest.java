package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.StatusChangeMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
        CurrentUserContext.set(academicSession());
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
        CurrentUserContext.set(counselorSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.COUNSELOR_REVIEW));
        when(statusChangeMapper.countCounseledStudent(1L, 2L)).thenReturn(1);
        StatusChangeReviewRequest request = reviewRequest("COUNSELOR", "REJECT", "");

        StatusChangeService service = new StatusChangeService(statusChangeMapper);

        assertThatThrownBy(() -> service.review(10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(StatusChangeErrorCodes.REVIEW_OPINION_REQUIRED.getCode());
    }

    @Test
    void counselorApprovalMovesApplicationToAcademicReview() {
        CurrentUserContext.set(counselorSession());
        when(statusChangeMapper.selectById(10L))
                .thenReturn(application(10L, 1L, StatusChangeStatus.COUNSELOR_REVIEW));
        when(statusChangeMapper.countCounseledStudent(1L, 2L)).thenReturn(1);
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

    @Test
    void academicOfficeCanListAllSubmittedHistoryWithoutDrafts() {
        CurrentUserContext.set(academicSession());
        Page<StatusChangeApplicationVo> queryResult = new Page<>(1, 10);
        StatusChangeApplicationVo approved = new StatusChangeApplicationVo();
        approved.setChangeId(10L);
        approved.setStatusCode(StatusChangeStatus.APPROVED.code());
        queryResult.setRecords(List.of(approved));
        queryResult.setTotal(1);
        when(statusChangeMapper.selectApplicationPage(any(), isNull(), isNull(), isNull(), eq(true)))
                .thenReturn(queryResult);

        var result = new StatusChangeService(statusChangeMapper)
                .listForReview(1, 10, "ALL", null);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().getFirst().getStatus()).isEqualTo("APPROVED");
        verify(statusChangeMapper).selectApplicationPage(any(), isNull(), isNull(), isNull(), eq(true));
    }

    @Test
    void academicOfficeCanFilterAllRecordsByCompletedStatus() {
        CurrentUserContext.set(academicSession());
        Page<StatusChangeApplicationVo> queryResult = new Page<>(1, 10);
        queryResult.setRecords(List.of());
        when(statusChangeMapper.selectApplicationPage(any(), isNull(), isNull(),
                eq(StatusChangeStatus.ACADEMIC_REJECTED.code()), eq(true))).thenReturn(queryResult);

        new StatusChangeService(statusChangeMapper)
                .listForReview(1, 10, "ALL", "ACADEMIC_REJECTED");

        verify(statusChangeMapper).selectApplicationPage(any(), isNull(), isNull(),
                eq(StatusChangeStatus.ACADEMIC_REJECTED.code()), eq(true));
    }

    @Test
    void studentCannotListTeachersStatusChangeHistory() {
        CurrentUserContext.set(studentSession());

        assertThatThrownBy(() -> new StatusChangeService(statusChangeMapper)
                .listForReview(1, 10, "ALL", null))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(GlobalErrorCodeConstants.FORBIDDEN.getCode());

        verify(statusChangeMapper, never()).selectApplicationPage(any(), any(), any(), any(), anyBoolean());
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

    private AuthSession counselorSession() {
        return new AuthSession(2L, "700001", 3, Set.of("COUNSELOR"), Set.of("status:review:read"), 0);
    }

    private AuthSession academicSession() {
        return new AuthSession(4L, "admin", 4, Set.of("ADMIN"), Set.of("status:review:read"), 0);
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
