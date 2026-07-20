package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.Evaluation;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EvaluationMapper extends BaseMapper<Evaluation> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            SELECT 'TEACHER' AS target_type, cs.selection_id, cs.schedule_id, e.evaluation_id, c.course_id, c.course_name,
                   sc.teacher_id, u.username AS teacher_name, cs.semester,
                   e.score_teaching, e.score_content, e.score_method,
                   ROUND((e.score_teaching + e.score_content + e.score_method) / 3, 2) AS overall_score,
                   e.comment, e.create_time AS submitted_at
            FROM course_selection cs
            JOIN schedule sc ON sc.schedule_id = cs.schedule_id AND sc.course_id = cs.course_id
            JOIN course c ON c.course_id = cs.course_id
            JOIN user u ON u.user_id = sc.teacher_id
            LEFT JOIN evaluation e ON e.student_id = cs.student_id AND e.schedule_id = cs.schedule_id
                 AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            WHERE cs.student_id = #{studentId} AND cs.status = 1
            ORDER BY cs.semester DESC, c.course_name, cs.selection_id DESC
            """)
    List<EvaluationTaskVo> selectTasks(@Param("studentId") Long studentId);

    @Select("""
            SELECT 'TEACHER' AS target_type, cs.selection_id, cs.schedule_id, e.evaluation_id, c.course_id, c.course_name,
                   sc.teacher_id, u.username AS teacher_name, cs.semester,
                   e.score_teaching, e.score_content, e.score_method,
                   ROUND((e.score_teaching + e.score_content + e.score_method) / 3, 2) AS overall_score,
                   e.comment, e.create_time AS submitted_at
            FROM course_selection cs
            JOIN schedule sc ON sc.schedule_id = cs.schedule_id AND sc.course_id = cs.course_id
            JOIN course c ON c.course_id = cs.course_id
            JOIN user u ON u.user_id = sc.teacher_id
            LEFT JOIN evaluation e ON e.student_id = cs.student_id AND e.schedule_id = cs.schedule_id
                 AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            WHERE cs.student_id = #{studentId} AND cs.selection_id = #{selectionId} AND cs.status = 1
            LIMIT 1
            """)
    EvaluationTaskVo selectTask(
            @Param("studentId") Long studentId,
            @Param("selectionId") Long selectionId
    );

    @Select("""
            <script>
            SELECT COUNT(*) AS response_count,
                   ROUND(AVG((score_teaching + score_content + score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(score_teaching), 2) AS teaching_average,
                   ROUND(AVG(score_content), 2) AS content_average,
                   ROUND(AVG(score_method), 2) AS method_average
            FROM evaluation e
            JOIN student s ON s.student_id = e.student_id
            WHERE 1 = 1
              <if test="teacherId != null">AND e.teacher_id = #{teacherId}</if>
              <if test="counselorId != null">AND s.counselor_id = #{counselorId}</if>
              AND score_teaching IS NOT NULL AND score_content IS NOT NULL AND score_method IS NOT NULL
            </script>
            """)
    EvaluationTeacherOverviewVo selectOverview(
            @Param("teacherId") Long teacherId,
            @Param("counselorId") Long counselorId);

    @Select("""
            <script>
            SELECT 'TEACHER' AS target_type, e.teacher_id, COALESCE(u.real_name, u.username) AS teacher_name,
                   e.course_id, c.course_name, e.semester, COUNT(*) AS response_count,
                   ROUND(AVG((e.score_teaching + e.score_content + e.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(e.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(e.score_content), 2) AS content_average,
                   ROUND(AVG(e.score_method), 2) AS method_average
            FROM evaluation e
            JOIN course c ON c.course_id = e.course_id
            JOIN user u ON u.user_id = e.teacher_id
            JOIN student s ON s.student_id = e.student_id
            WHERE 1 = 1
              <if test="teacherId != null">AND e.teacher_id = #{teacherId}</if>
              <if test="counselorId != null">AND s.counselor_id = #{counselorId}</if>
              AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            GROUP BY e.teacher_id, u.real_name, u.username, e.course_id, c.course_name, e.semester
            ORDER BY e.semester DESC, teacher_name, c.course_name
            </script>
            """)
    List<EvaluationCourseSummaryVo> selectCourseSummaries(
            @Param("teacherId") Long teacherId,
            @Param("counselorId") Long counselorId);

    @Select("""
            <script>
            SELECT 'TEACHER' AS target_type, e.teacher_id, COALESCE(u.real_name, u.username) AS teacher_name,
                   e.course_id, c.course_name, e.semester, COUNT(*) AS response_count,
                   ROUND(AVG((e.score_teaching + e.score_content + e.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(e.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(e.score_content), 2) AS content_average,
                   ROUND(AVG(e.score_method), 2) AS method_average
            FROM evaluation e
            JOIN course c ON c.course_id = e.course_id
            JOIN user u ON u.user_id = e.teacher_id
            JOIN student s ON s.student_id = e.student_id
            WHERE e.teacher_id = #{teacherId} AND e.course_id = #{courseId} AND e.semester = #{semester}
              <if test="counselorId != null">AND s.counselor_id = #{counselorId}</if>
              AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            GROUP BY e.teacher_id, u.real_name, u.username, e.course_id, c.course_name, e.semester
            </script>
            """)
    EvaluationCourseSummaryVo selectCourseSummary(
            @Param("teacherId") Long teacherId,
            @Param("counselorId") Long counselorId,
            @Param("courseId") Long courseId,
            @Param("semester") String semester
    );

    @Select("""
            <script>
            SELECT e.comment
            FROM evaluation e
            JOIN student s ON s.student_id = e.student_id
            WHERE e.teacher_id = #{teacherId} AND e.course_id = #{courseId} AND e.semester = #{semester}
              <if test="counselorId != null">AND s.counselor_id = #{counselorId}</if>
              AND score_teaching IS NOT NULL AND score_content IS NOT NULL AND score_method IS NOT NULL
              AND comment IS NOT NULL AND TRIM(comment) != ''
            ORDER BY e.create_time DESC, e.evaluation_id DESC
            </script>
            """)
    List<String> selectAnonymousComments(
            @Param("teacherId") Long teacherId,
            @Param("counselorId") Long counselorId,
            @Param("courseId") Long courseId,
            @Param("semester") String semester
    );
}
