package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.CounselorEvaluation;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CounselorEvaluationMapper extends BaseMapper<CounselorEvaluation> {

    @Select("SELECT MAX(semester) FROM course_selection WHERE student_id = #{studentId}")
    String selectLatestSemester(@Param("studentId") Long studentId);

    @Select("""
            SELECT 'COUNSELOR' AS target_type, NULL AS selection_id, NULL AS schedule_id,
                   ce.evaluation_id, NULL AS course_id, '辅导员工作评教' AS course_name,
                   c.user_id AS teacher_id, COALESCE(c.real_name, c.username) AS teacher_name,
                   #{semester} AS semester, ce.score_teaching, ce.score_content, ce.score_method,
                   ROUND((ce.score_teaching + ce.score_content + ce.score_method) / 3, 2) AS overall_score,
                   ce.comment, ce.create_time AS submitted_at
            FROM student s
            JOIN user c ON c.user_id = s.counselor_id
            LEFT JOIN counselor_evaluation ce ON ce.student_id = s.student_id
                 AND ce.counselor_id = c.user_id AND ce.semester = #{semester}
            WHERE s.student_id = #{studentId}
            """)
    EvaluationTaskVo selectTask(@Param("studentId") Long studentId, @Param("semester") String semester);

    @Select("""
            <script>
            SELECT COUNT(*) AS response_count,
                   ROUND(AVG((score_teaching + score_content + score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(score_teaching), 2) AS teaching_average,
                   ROUND(AVG(score_content), 2) AS content_average,
                   ROUND(AVG(score_method), 2) AS method_average
            FROM counselor_evaluation
            WHERE 1 = 1
              <if test="counselorId != null">AND counselor_id = #{counselorId}</if>
            </script>
            """)
    EvaluationTeacherOverviewVo selectOverview(@Param("counselorId") Long counselorId);

    @Select("""
            <script>
            SELECT 'COUNSELOR' AS target_type, ce.counselor_id AS teacher_id,
                   COALESCE(u.real_name, u.username) AS teacher_name,
                   NULL AS course_id, '辅导员工作评教' AS course_name, ce.semester,
                   COUNT(*) AS response_count,
                   ROUND(AVG((ce.score_teaching + ce.score_content + ce.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(ce.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(ce.score_content), 2) AS content_average,
                   ROUND(AVG(ce.score_method), 2) AS method_average
            FROM counselor_evaluation ce
            JOIN user u ON u.user_id = ce.counselor_id
            WHERE 1 = 1
              <if test="counselorId != null">AND ce.counselor_id = #{counselorId}</if>
            GROUP BY ce.counselor_id, u.real_name, u.username, ce.semester
            ORDER BY ce.semester DESC, teacher_name
            </script>
            """)
    List<EvaluationCourseSummaryVo> selectSummaries(@Param("counselorId") Long counselorId);

    @Select("""
            SELECT 'COUNSELOR' AS target_type, ce.counselor_id AS teacher_id,
                   COALESCE(u.real_name, u.username) AS teacher_name,
                   NULL AS course_id, '辅导员工作评教' AS course_name, ce.semester,
                   COUNT(*) AS response_count,
                   ROUND(AVG((ce.score_teaching + ce.score_content + ce.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(ce.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(ce.score_content), 2) AS content_average,
                   ROUND(AVG(ce.score_method), 2) AS method_average
            FROM counselor_evaluation ce
            JOIN user u ON u.user_id = ce.counselor_id
            WHERE ce.counselor_id = #{counselorId} AND ce.semester = #{semester}
            GROUP BY ce.counselor_id, u.real_name, u.username, ce.semester
            """)
    EvaluationCourseSummaryVo selectSummary(
            @Param("counselorId") Long counselorId,
            @Param("semester") String semester);

    @Select("""
            SELECT comment FROM counselor_evaluation
            WHERE counselor_id = #{counselorId} AND semester = #{semester}
              AND comment IS NOT NULL AND TRIM(comment) != ''
            ORDER BY create_time DESC, evaluation_id DESC
            """)
    List<String> selectAnonymousComments(
            @Param("counselorId") Long counselorId,
            @Param("semester") String semester);
}
