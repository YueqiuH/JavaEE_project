package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.CounselorEvaluationMapper;
import com.smartcampus.app.dao.student.EvaluationMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.EvaluationSubmissionRequest;
import com.smartcampus.contract.entity.CounselorEvaluation;
import com.smartcampus.contract.entity.Evaluation;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.EvaluationCourseDetailVo;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EvaluationService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final String COUNSELOR_ROLE = "COUNSELOR";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String STUDENT_READ_PERMISSION = "evaluation:task:read-self";
    private static final String STUDENT_SUBMIT_PERMISSION = "evaluation:submit-self";

    private final EvaluationMapper evaluationMapper;
    private final CounselorEvaluationMapper counselorEvaluationMapper;

    public EvaluationService(
            EvaluationMapper evaluationMapper,
            CounselorEvaluationMapper counselorEvaluationMapper) {
        this.evaluationMapper = evaluationMapper;
        this.counselorEvaluationMapper = counselorEvaluationMapper;
    }

    public List<EvaluationTaskVo> listMyTasks() {
        AuthSession session = requireStudentSession(STUDENT_READ_PERMISSION);
        Student student = requireStudent(session);
        List<EvaluationTaskVo> tasks = new ArrayList<>(evaluationMapper.selectTasks(student.getStudentId()));
        EvaluationTaskVo counselorTask = counselorEvaluationMapper.selectTask(
                student.getStudentId(), resolveSemester(student.getStudentId()));
        if (counselorTask != null) {
            tasks.add(counselorTask);
        }
        tasks.forEach(this::translateStatus);
        return tasks;
    }

    @Transactional
    public EvaluationTaskVo submit(Long selectionId, EvaluationSubmissionRequest request) {
        AuthSession session = requireStudentSession(STUDENT_SUBMIT_PERMISSION);
        Student student = requireStudent(session);
        EvaluationTaskVo task = evaluationMapper.selectTask(student.getStudentId(), selectionId);
        if (task == null) {
            throw new BusinessException(EvaluationErrorCodes.TASK_NOT_AVAILABLE);
        }
        if (task.getEvaluationId() != null) {
            throw new BusinessException(EvaluationErrorCodes.ALREADY_SUBMITTED);
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setStudentId(student.getStudentId());
        evaluation.setScheduleId(task.getScheduleId());
        evaluation.setTeacherId(task.getTeacherId());
        evaluation.setCourseId(task.getCourseId());
        evaluation.setSemester(task.getSemester());
        copyScores(request, evaluation);
        try {
            if (evaluationMapper.insert(evaluation) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(EvaluationErrorCodes.ALREADY_SUBMITTED);
        }

        EvaluationTaskVo submitted = evaluationMapper.selectTask(student.getStudentId(), selectionId);
        if (submitted == null || submitted.getEvaluationId() == null) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        translateStatus(submitted);
        return submitted;
    }

    @Transactional
    public EvaluationTaskVo submitCounselor(EvaluationSubmissionRequest request) {
        AuthSession session = requireStudentSession(STUDENT_SUBMIT_PERMISSION);
        Student student = requireStudent(session);
        String semester = resolveSemester(student.getStudentId());
        EvaluationTaskVo task = counselorEvaluationMapper.selectTask(student.getStudentId(), semester);
        if (task == null) {
            throw new BusinessException(EvaluationErrorCodes.TASK_NOT_AVAILABLE);
        }
        if (task.getEvaluationId() != null) {
            throw new BusinessException(EvaluationErrorCodes.ALREADY_SUBMITTED);
        }

        CounselorEvaluation evaluation = new CounselorEvaluation();
        evaluation.setStudentId(student.getStudentId());
        evaluation.setCounselorId(task.getTeacherId());
        evaluation.setSemester(semester);
        evaluation.setScoreTeaching(request.getScoreTeaching());
        evaluation.setScoreContent(request.getScoreContent());
        evaluation.setScoreMethod(request.getScoreMethod());
        evaluation.setComment(normalize(request.getComment()));
        evaluation.setCreateTime(LocalDateTime.now());
        try {
            if (counselorEvaluationMapper.insert(evaluation) != 1) {
                throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
            }
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(EvaluationErrorCodes.ALREADY_SUBMITTED);
        }

        EvaluationTaskVo submitted = counselorEvaluationMapper.selectTask(student.getStudentId(), semester);
        if (submitted == null || submitted.getEvaluationId() == null) {
            throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        }
        translateStatus(submitted);
        return submitted;
    }

    public EvaluationTeacherOverviewVo getMyOverview() {
        EvaluationScope scope = requireResultScope();
        AuthSession session = CurrentUserContext.require();
        EvaluationTeacherOverviewVo overview;
        List<EvaluationCourseSummaryVo> summaries = new ArrayList<>();
        if (scope.counselorId() != null) {
            overview = normalizedOverview(counselorEvaluationMapper.selectOverview(scope.counselorId()));
            summaries.addAll(counselorEvaluationMapper.selectSummaries(scope.counselorId()));
        } else if (scope.all()) {
            EvaluationTeacherOverviewVo teacherOverview = normalizedOverview(evaluationMapper.selectOverview(null, null));
            EvaluationTeacherOverviewVo counselorOverview = normalizedOverview(counselorEvaluationMapper.selectOverview(null));
            overview = mergeOverviews(teacherOverview, counselorOverview);
            summaries.addAll(evaluationMapper.selectCourseSummaries(null, null));
            summaries.addAll(counselorEvaluationMapper.selectSummaries(null));
        } else {
            overview = normalizedOverview(evaluationMapper.selectOverview(scope.teacherId(), null));
            summaries.addAll(evaluationMapper.selectCourseSummaries(scope.teacherId(), null));
        }
        overview.setTeacherId(scope.teacherId() != null ? scope.teacherId() : scope.counselorId());
        overview.setTeacherName(session.roles().contains(TEACHER_ROLE) ? session.username()
                : session.roles().contains(COUNSELOR_ROLE) ? "我的辅导员评教" : "全校评教汇总");
        overview.setCourses(summaries);
        return overview;
    }

    public EvaluationCourseDetailVo getMyCourseDetail(Long courseId, String semester, Long requestedTeacherId) {
        EvaluationScope scope = requireResultScope();
        if (scope.counselorId() != null) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        Long teacherId = scope.teacherId() != null ? scope.teacherId() : requestedTeacherId;
        if (teacherId == null) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        EvaluationCourseSummaryVo summary = evaluationMapper.selectCourseSummary(
                teacherId, null, courseId, semester);
        if (summary == null) {
            throw new BusinessException(EvaluationErrorCodes.RESULT_NOT_FOUND);
        }
        EvaluationCourseDetailVo detail = toDetail(summary);
        detail.setAnonymousComments(evaluationMapper.selectAnonymousComments(
                teacherId, null, courseId, semester));
        return detail;
    }

    public EvaluationCourseDetailVo getCounselorDetail(Long counselorId, String semester) {
        EvaluationScope scope = requireResultScope();
        Long targetId;
        if (scope.counselorId() != null) {
            targetId = scope.counselorId();
            if (!targetId.equals(counselorId)) {
                throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
            }
        } else if (scope.all()) {
            targetId = counselorId;
        } else {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        EvaluationCourseSummaryVo summary = counselorEvaluationMapper.selectSummary(targetId, semester);
        if (summary == null) {
            throw new BusinessException(EvaluationErrorCodes.RESULT_NOT_FOUND);
        }
        EvaluationCourseDetailVo detail = toDetail(summary);
        detail.setAnonymousComments(counselorEvaluationMapper.selectAnonymousComments(targetId, semester));
        return detail;
    }

    private AuthSession requireStudentSession(String permission) {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(STUDENT_ROLE) || !session.hasPermission(permission)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private EvaluationScope requireResultScope() {
        AuthSession session = CurrentUserContext.require();
        if (session.roles().contains(TEACHER_ROLE) && session.hasPermission("evaluation:result:read-self")) {
            return new EvaluationScope(session.userId(), null, false);
        }
        if (session.roles().contains(COUNSELOR_ROLE)
                && session.hasPermission("evaluation:result:read-counseled")) {
            return new EvaluationScope(null, session.userId(), false);
        }
        if (session.roles().contains(ADMIN_ROLE) && session.hasPermission("evaluation:result:read-all")) {
            return new EvaluationScope(null, null, true);
        }
        throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
    }

    private Student requireStudent(AuthSession session) {
        try {
            Student student = evaluationMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) {
                return student;
            }
        } catch (NumberFormatException ignored) {
            // Student login names in this project are student numbers.
        }
        throw new BusinessException(EvaluationErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private String resolveSemester(Long studentId) {
        String semester = counselorEvaluationMapper.selectLatestSemester(studentId);
        if (semester != null && !semester.isBlank()) {
            return semester;
        }
        LocalDate today = LocalDate.now();
        int startYear = today.getMonthValue() >= 9 ? today.getYear() : today.getYear() - 1;
        int term = today.getMonthValue() >= 2 && today.getMonthValue() <= 8 ? 2 : 1;
        return startYear + "-" + (startYear + 1) + "-" + term;
    }

    private void copyScores(EvaluationSubmissionRequest request, Evaluation evaluation) {
        evaluation.setScoreTeaching(request.getScoreTeaching());
        evaluation.setScoreContent(request.getScoreContent());
        evaluation.setScoreMethod(request.getScoreMethod());
        evaluation.setComment(normalize(request.getComment()));
        evaluation.setCreateTime(LocalDateTime.now());
    }

    private EvaluationTeacherOverviewVo normalizedOverview(EvaluationTeacherOverviewVo overview) {
        if (overview == null) {
            overview = new EvaluationTeacherOverviewVo();
        }
        if (overview.getResponseCount() == null) {
            overview.setResponseCount(0L);
        }
        return overview;
    }

    private EvaluationTeacherOverviewVo mergeOverviews(
            EvaluationTeacherOverviewVo first,
            EvaluationTeacherOverviewVo second) {
        EvaluationTeacherOverviewVo merged = new EvaluationTeacherOverviewVo();
        long firstCount = first.getResponseCount();
        long secondCount = second.getResponseCount();
        merged.setResponseCount(firstCount + secondCount);
        merged.setOverallAverage(weighted(first.getOverallAverage(), firstCount, second.getOverallAverage(), secondCount));
        merged.setTeachingAverage(weighted(first.getTeachingAverage(), firstCount, second.getTeachingAverage(), secondCount));
        merged.setContentAverage(weighted(first.getContentAverage(), firstCount, second.getContentAverage(), secondCount));
        merged.setMethodAverage(weighted(first.getMethodAverage(), firstCount, second.getMethodAverage(), secondCount));
        return merged;
    }

    private BigDecimal weighted(BigDecimal first, long firstCount, BigDecimal second, long secondCount) {
        long total = firstCount + secondCount;
        if (total == 0) {
            return null;
        }
        BigDecimal firstValue = first == null ? BigDecimal.ZERO : first.multiply(BigDecimal.valueOf(firstCount));
        BigDecimal secondValue = second == null ? BigDecimal.ZERO : second.multiply(BigDecimal.valueOf(secondCount));
        return firstValue.add(secondValue).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private EvaluationCourseDetailVo toDetail(EvaluationCourseSummaryVo summary) {
        EvaluationCourseDetailVo detail = new EvaluationCourseDetailVo();
        detail.setCourseId(summary.getCourseId());
        detail.setCourseName(summary.getCourseName());
        detail.setSemester(summary.getSemester());
        detail.setResponseCount(summary.getResponseCount());
        detail.setOverallAverage(summary.getOverallAverage());
        detail.setTeachingAverage(summary.getTeachingAverage());
        detail.setContentAverage(summary.getContentAverage());
        detail.setMethodAverage(summary.getMethodAverage());
        return detail;
    }

    private void translateStatus(EvaluationTaskVo task) {
        task.setStatus(task.getEvaluationId() == null ? "PENDING" : "SUBMITTED");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record EvaluationScope(Long teacherId, Long counselorId, boolean all) {
    }
}
