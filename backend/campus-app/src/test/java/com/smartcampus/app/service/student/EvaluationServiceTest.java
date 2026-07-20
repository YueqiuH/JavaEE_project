package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.CounselorEvaluationMapper;
import com.smartcampus.app.dao.student.EvaluationMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.EvaluationSubmissionRequest;
import com.smartcampus.contract.entity.CounselorEvaluation;
import com.smartcampus.contract.entity.Evaluation;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class EvaluationServiceTest {

    @Mock
    private EvaluationMapper evaluationMapper;
    @Mock
    private CounselorEvaluationMapper counselorEvaluationMapper;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void studentCanOnlySubmitAnOwnedActiveSelection() {
        CurrentUserContext.set(studentSession());
        when(evaluationMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        when(evaluationMapper.selectTask(1L, 99L)).thenReturn(null);

        EvaluationService service = new EvaluationService(evaluationMapper, counselorEvaluationMapper);

        assertThatThrownBy(() -> service.submit(99L, submission()))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(EvaluationErrorCodes.TASK_NOT_AVAILABLE.getCode());
        verify(evaluationMapper, never()).insert(any(Evaluation.class));
    }

    @Test
    void submittedEvaluationCannotBeChangedOrSubmittedAgain() {
        CurrentUserContext.set(studentSession());
        when(evaluationMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        EvaluationTaskVo submitted = task();
        submitted.setEvaluationId(88L);
        when(evaluationMapper.selectTask(1L, 10L)).thenReturn(submitted);

        EvaluationService service = new EvaluationService(evaluationMapper, counselorEvaluationMapper);

        assertThatThrownBy(() -> service.submit(10L, submission()))
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(EvaluationErrorCodes.ALREADY_SUBMITTED.getCode());
        verify(evaluationMapper, never()).insert(any(Evaluation.class));
    }

    @Test
    void submissionUsesServerSideCourseTeacherAndScheduleOwnership() {
        CurrentUserContext.set(studentSession());
        when(evaluationMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        EvaluationTaskVo pending = task();
        EvaluationTaskVo submitted = task();
        submitted.setEvaluationId(88L);
        when(evaluationMapper.selectTask(1L, 10L)).thenReturn(pending, submitted);
        when(evaluationMapper.insert(any(Evaluation.class))).thenReturn(1);

        EvaluationService service = new EvaluationService(evaluationMapper, counselorEvaluationMapper);
        EvaluationTaskVo result = service.submit(10L, submission());

        ArgumentCaptor<Evaluation> captor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationMapper).insert(captor.capture());
        Evaluation saved = captor.getValue();
        assertThat(saved.getStudentId()).isEqualTo(1L);
        assertThat(saved.getScheduleId()).isEqualTo(20L);
        assertThat(saved.getCourseId()).isEqualTo(30L);
        assertThat(saved.getTeacherId()).isEqualTo(40L);
        assertThat(saved.getSemester()).isEqualTo("2026-2027-1");
        assertThat(saved.getComment()).isEqualTo("讲解清晰");
        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
    }

    @Test
    void teacherOverviewIsAlwaysScopedToCurrentTeacher() {
        CurrentUserContext.set(teacherSession());
        EvaluationTeacherOverviewVo overview = new EvaluationTeacherOverviewVo();
        overview.setResponseCount(3L);
        EvaluationCourseSummaryVo course = new EvaluationCourseSummaryVo();
        course.setCourseId(30L);
        when(evaluationMapper.selectOverview(2L, null)).thenReturn(overview);
        when(evaluationMapper.selectCourseSummaries(2L, null)).thenReturn(List.of(course));

        EvaluationService service = new EvaluationService(evaluationMapper, counselorEvaluationMapper);
        EvaluationTeacherOverviewVo result = service.getMyOverview();

        assertThat(result.getTeacherId()).isEqualTo(2L);
        assertThat(result.getTeacherName()).isEqualTo("700001");
        assertThat(result.getCourses()).containsExactly(course);
        verify(evaluationMapper).selectOverview(2L, null);
        verify(evaluationMapper).selectCourseSummaries(2L, null);
    }

    @Test
    void studentCanSubmitOneAnonymousEvaluationForAssignedCounselor() {
        CurrentUserContext.set(studentSession());
        when(evaluationMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        when(counselorEvaluationMapper.selectLatestSemester(1L)).thenReturn("2025-2026-1");
        EvaluationTaskVo pending = task();
        pending.setTargetType("COUNSELOR");
        pending.setTeacherId(2L);
        pending.setEvaluationId(null);
        EvaluationTaskVo submitted = task();
        submitted.setTargetType("COUNSELOR");
        submitted.setTeacherId(2L);
        submitted.setEvaluationId(99L);
        when(counselorEvaluationMapper.selectTask(1L, "2025-2026-1"))
                .thenReturn(pending, submitted);
        when(counselorEvaluationMapper.insert(any(CounselorEvaluation.class))).thenReturn(1);

        EvaluationTaskVo result = new EvaluationService(evaluationMapper, counselorEvaluationMapper)
                .submitCounselor(submission());

        assertThat(result.getStatus()).isEqualTo("SUBMITTED");
        ArgumentCaptor<CounselorEvaluation> captor = ArgumentCaptor.forClass(CounselorEvaluation.class);
        verify(counselorEvaluationMapper).insert(captor.capture());
        assertThat(captor.getValue().getCounselorId()).isEqualTo(2L);
        assertThat(captor.getValue().getStudentId()).isEqualTo(1L);
    }

    @Test
    void counselorOverviewOnlyUsesEvaluationsTargetingCurrentCounselor() {
        CurrentUserContext.set(counselorSession());
        EvaluationTeacherOverviewVo overview = new EvaluationTeacherOverviewVo();
        overview.setResponseCount(2L);
        EvaluationCourseSummaryVo summary = new EvaluationCourseSummaryVo();
        summary.setTargetType("COUNSELOR");
        summary.setTeacherId(2L);
        when(counselorEvaluationMapper.selectOverview(2L)).thenReturn(overview);
        when(counselorEvaluationMapper.selectSummaries(2L)).thenReturn(List.of(summary));

        EvaluationTeacherOverviewVo result = new EvaluationService(evaluationMapper, counselorEvaluationMapper)
                .getMyOverview();

        assertThat(result.getTeacherId()).isEqualTo(2L);
        assertThat(result.getCourses()).containsExactly(summary);
        verify(evaluationMapper, never()).selectOverview(any(), any());
    }

    @Test
    void studentCannotReadTeacherEvaluationResults() {
        CurrentUserContext.set(studentSession());
        EvaluationService service = new EvaluationService(evaluationMapper, counselorEvaluationMapper);

        assertThatThrownBy(service::getMyOverview)
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(403001);
        verify(evaluationMapper, never()).selectOverview(any(), any());
    }

    private EvaluationSubmissionRequest submission() {
        EvaluationSubmissionRequest request = new EvaluationSubmissionRequest();
        request.setScoreTeaching(5);
        request.setScoreContent(4);
        request.setScoreMethod(5);
        request.setComment("  讲解清晰  ");
        return request;
    }

    private EvaluationTaskVo task() {
        EvaluationTaskVo task = new EvaluationTaskVo();
        task.setSelectionId(10L);
        task.setScheduleId(20L);
        task.setCourseId(30L);
        task.setTeacherId(40L);
        task.setSemester("2026-2027-1");
        return task;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setStudentNo(600001L);
        return student;
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"),
                Set.of("evaluation:task:read-self", "evaluation:submit-self"), 0);
    }

    private AuthSession teacherSession() {
        return new AuthSession(2L, "700001", 2, Set.of("TEACHER"),
                Set.of("evaluation:result:read-self"), 0);
    }

    private AuthSession counselorSession() {
        return new AuthSession(2L, "700001", 3, Set.of("COUNSELOR"),
                Set.of("evaluation:result:read-counseled"), 0);
    }
}
