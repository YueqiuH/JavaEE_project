package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.Evaluation;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.EvaluationCourseSummaryVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EvaluationMapper extends BaseMapper<Evaluation> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    StudentEntity selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            SELECT cs.selection_id, cs.schedule_id, e.evaluation_id, c.course_id, c.course_name,
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
            SELECT cs.selection_id, cs.schedule_id, e.evaluation_id, c.course_id, c.course_name,
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
            SELECT COUNT(*) AS response_count,
                   ROUND(AVG((score_teaching + score_content + score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(score_teaching), 2) AS teaching_average,
                   ROUND(AVG(score_content), 2) AS content_average,
                   ROUND(AVG(score_method), 2) AS method_average
            FROM evaluation
            WHERE teacher_id = #{teacherId}
              AND score_teaching IS NOT NULL AND score_content IS NOT NULL AND score_method IS NOT NULL
            """)
    EvaluationTeacherOverviewVo selectTeacherOverview(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT e.course_id, c.course_name, e.semester, COUNT(*) AS response_count,
                   ROUND(AVG((e.score_teaching + e.score_content + e.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(e.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(e.score_content), 2) AS content_average,
                   ROUND(AVG(e.score_method), 2) AS method_average
            FROM evaluation e
            JOIN course c ON c.course_id = e.course_id
            WHERE e.teacher_id = #{teacherId}
              AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            GROUP BY e.course_id, c.course_name, e.semester
            ORDER BY e.semester DESC, c.course_name
            """)
    List<EvaluationCourseSummaryVo> selectCourseSummaries(@Param("teacherId") Long teacherId);

    @Select("""
            SELECT e.course_id, c.course_name, e.semester, COUNT(*) AS response_count,
                   ROUND(AVG((e.score_teaching + e.score_content + e.score_method) / 3), 2) AS overall_average,
                   ROUND(AVG(e.score_teaching), 2) AS teaching_average,
                   ROUND(AVG(e.score_content), 2) AS content_average,
                   ROUND(AVG(e.score_method), 2) AS method_average
            FROM evaluation e
            JOIN course c ON c.course_id = e.course_id
            WHERE e.teacher_id = #{teacherId} AND e.course_id = #{courseId} AND e.semester = #{semester}
              AND e.score_teaching IS NOT NULL AND e.score_content IS NOT NULL AND e.score_method IS NOT NULL
            GROUP BY e.course_id, c.course_name, e.semester
            """)
    EvaluationCourseSummaryVo selectCourseSummary(
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId,
            @Param("semester") String semester
    );

    @Select("""
            SELECT comment
            FROM evaluation
            WHERE teacher_id = #{teacherId} AND course_id = #{courseId} AND semester = #{semester}
              AND score_teaching IS NOT NULL AND score_content IS NOT NULL AND score_method IS NOT NULL
              AND comment IS NOT NULL AND TRIM(comment) != ''
            ORDER BY create_time DESC, evaluation_id DESC
            """)
    List<String> selectAnonymousComments(
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId,
            @Param("semester") String semester
    );
}
