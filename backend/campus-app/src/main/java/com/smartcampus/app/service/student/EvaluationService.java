package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.EvaluationMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.EvaluationSubmissionRequest;
import com.smartcampus.contract.entity.Evaluation;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.EvaluationCourseDetailVo;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvaluationService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final String STUDENT_READ_PERMISSION = "evaluation:task:read-self";
    private static final String STUDENT_SUBMIT_PERMISSION = "evaluation:submit-self";
    private static final String TEACHER_READ_PERMISSION = "evaluation:result:read-self";

    private final EvaluationMapper evaluationMapper;

    public EvaluationService(EvaluationMapper evaluationMapper) {
        this.evaluationMapper = evaluationMapper;
    }

    public List<EvaluationTaskVo> listMyTasks() {
        AuthSession session = requireStudentSession(STUDENT_READ_PERMISSION);
        StudentEntity student = requireStudent(session);
        List<EvaluationTaskVo> tasks = evaluationMapper.selectTasks(student.getStudentId());
        tasks.forEach(this::translateStatus);
        return tasks;
    }

    @Transactional
    public EvaluationTaskVo submit(Long selectionId, EvaluationSubmissionRequest request) {
        AuthSession session = requireStudentSession(STUDENT_SUBMIT_PERMISSION);
        StudentEntity student = requireStudent(session);
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
        evaluation.setScoreTeaching(request.getScoreTeaching());
        evaluation.setScoreContent(request.getScoreContent());
        evaluation.setScoreMethod(request.getScoreMethod());
        evaluation.setComment(normalize(request.getComment()));
        evaluation.setCreateTime(LocalDateTime.now());
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

    public EvaluationTeacherOverviewVo getMyOverview() {
        AuthSession session = requireTeacherSession();
        EvaluationTeacherOverviewVo overview = evaluationMapper.selectTeacherOverview(session.userId());
        if (overview == null) {
            overview = new EvaluationTeacherOverviewVo();
            overview.setResponseCount(0L);
        }
        overview.setTeacherId(session.userId());
        overview.setTeacherName(session.username());
        overview.setCourses(evaluationMapper.selectCourseSummaries(session.userId()));
        return overview;
    }

    public EvaluationCourseDetailVo getMyCourseDetail(Long courseId, String semester) {
        AuthSession session = requireTeacherSession();
        EvaluationCourseSummaryVo summary = evaluationMapper.selectCourseSummary(session.userId(), courseId, semester);
        if (summary == null) {
            throw new BusinessException(EvaluationErrorCodes.RESULT_NOT_FOUND);
        }
        EvaluationCourseDetailVo detail = new EvaluationCourseDetailVo();
        detail.setCourseId(summary.getCourseId());
        detail.setCourseName(summary.getCourseName());
        detail.setSemester(summary.getSemester());
        detail.setResponseCount(summary.getResponseCount());
        detail.setOverallAverage(summary.getOverallAverage());
        detail.setTeachingAverage(summary.getTeachingAverage());
        detail.setContentAverage(summary.getContentAverage());
        detail.setMethodAverage(summary.getMethodAverage());
        detail.setAnonymousComments(evaluationMapper.selectAnonymousComments(session.userId(), courseId, semester));
        return detail;
    }

    private AuthSession requireStudentSession(String permission) {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(STUDENT_ROLE) || !session.hasPermission(permission)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private AuthSession requireTeacherSession() {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(TEACHER_ROLE) || !session.hasPermission(TEACHER_READ_PERMISSION)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private StudentEntity requireStudent(AuthSession session) {
        try {
            StudentEntity student = evaluationMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) {
                return student;
            }
        } catch (NumberFormatException ignored) {
            // Student login names in this project are student numbers.
        }
        throw new BusinessException(EvaluationErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private void translateStatus(EvaluationTaskVo task) {
        task.setStatus(task.getEvaluationId() == null ? "PENDING" : "SUBMITTED");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
