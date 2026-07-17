package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.ScholarshipMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.ScholarshipResultRequest;
import com.smartcampus.contract.dto.student.ScholarshipReviewRequest;
import com.smartcampus.contract.entity.Scholarship;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.ScholarshipApplicationVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScholarshipServiceTest {

    @Mock
    private ScholarshipMapper scholarshipMapper;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void studentCannotUpdateAnotherStudentsApplication() {
        CurrentUserContext.set(studentSession());
        Scholarship application = application(10L, 99L, ScholarshipStatus.DRAFT);
        when(scholarshipMapper.selectById(10L)).thenReturn(application);
        when(scholarshipMapper.selectStudentByNo(600001L)).thenReturn(student(1L));

        ScholarshipService service = new ScholarshipService(scholarshipMapper);

        assertThatThrownBy(() -> service.submit(10L))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(ScholarshipErrorCodes.APPLICATION_NOT_OWNED.getCode());
        verify(scholarshipMapper, never()).update(any(), any());
    }

    @Test
    void teacherMustProvideOpinionWhenReturningApplication() {
        CurrentUserContext.set(teacherSession());
        Scholarship application = application(10L, 1L, ScholarshipStatus.SUBMITTED);
        when(scholarshipMapper.selectById(10L)).thenReturn(application);
        ScholarshipReviewRequest request = new ScholarshipReviewRequest();
        request.setDecision("RETURN");

        ScholarshipService service = new ScholarshipService(scholarshipMapper);

        assertThatThrownBy(() -> service.review(10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(ScholarshipErrorCodes.REVIEW_OPINION_REQUIRED.getCode());
        verify(scholarshipMapper, never()).update(any(), any());
    }

    @Test
    void teacherCannotReviewDraftApplication() {
        CurrentUserContext.set(teacherSession());
        Scholarship application = application(10L, 1L, ScholarshipStatus.DRAFT);
        when(scholarshipMapper.selectById(10L)).thenReturn(application);
        ScholarshipReviewRequest request = new ScholarshipReviewRequest();
        request.setDecision("APPROVE");

        ScholarshipService service = new ScholarshipService(scholarshipMapper);

        assertThatThrownBy(() -> service.review(10L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(ScholarshipErrorCodes.INVALID_STATUS.getCode());
    }

    @Test
    void generateResultsOnlyAcceptsApprovedApplications() {
        CurrentUserContext.set(teacherSession());
        when(scholarshipMapper.selectById(10L))
                .thenReturn(application(10L, 1L, ScholarshipStatus.REJECTED));
        ScholarshipResultRequest request = new ScholarshipResultRequest();
        request.setApplicationIds(List.of(10L));

        ScholarshipService service = new ScholarshipService(scholarshipMapper);

        assertThatThrownBy(() -> service.generateResults(request))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(ScholarshipErrorCodes.RESULT_SELECTION_INVALID.getCode());
        verify(scholarshipMapper, never()).update(any(), any());
    }

    @Test
    void generateResultsMarksEveryApprovedApplicationAsSelected() {
        CurrentUserContext.set(teacherSession());
        when(scholarshipMapper.selectById(10L))
                .thenReturn(application(10L, 1L, ScholarshipStatus.APPROVED));
        when(scholarshipMapper.update(any(), any())).thenReturn(1);
        ScholarshipApplicationVo selected = new ScholarshipApplicationVo();
        selected.setScholarshipId(10L);
        selected.setStatusCode(ScholarshipStatus.SELECTED.code());
        when(scholarshipMapper.selectApplicationById(10L)).thenReturn(selected);
        ScholarshipResultRequest request = new ScholarshipResultRequest();
        request.setApplicationIds(List.of(10L, 10L));

        ScholarshipService service = new ScholarshipService(scholarshipMapper);
        List<ScholarshipApplicationVo> result = service.generateResults(request);

        assertThat(result).singleElement().extracting(ScholarshipApplicationVo::getStatus).isEqualTo("SELECTED");
        verify(scholarshipMapper).update(any(), any());
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of(), 0);
    }

    private AuthSession teacherSession() {
        return new AuthSession(2L, "700001", 2, Set.of("TEACHER"),
                Set.of("scholarship:review:read"), 0);
    }

    private StudentEntity student(Long id) {
        StudentEntity student = new StudentEntity();
        student.setStudentId(id);
        student.setStudentNo(600001L);
        return student;
    }

    private Scholarship application(Long id, Long studentId, ScholarshipStatus status) {
        Scholarship application = new Scholarship();
        application.setScholarshipId(id);
        application.setStudentId(studentId);
        application.setStatus(status.code());
        return application;
    }
}
