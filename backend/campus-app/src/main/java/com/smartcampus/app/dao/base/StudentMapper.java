package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.dto.StudentQuery;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.NameValueVo;
import com.smartcampus.contract.vo.StudentVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper extends BaseMapper<Student> {

    @Select("""
            SELECT s.student_id, s.student_no, s.student_name, s.gender, s.student_birth, s.student_age,
                   s.student_address, s.origin_place, s.class_name, s.enroll_year, s.status,
                   s.dept_id, d.dept_name, s.major_id, m.major_name
            FROM student s
            LEFT JOIN department d ON d.dept_id = s.dept_id
            LEFT JOIN major m      ON m.major_id = s.major_id
            WHERE (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR s.student_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR CAST(s.student_no AS CHAR) LIKE CONCAT('%', #{q.keyword}, '%'))
              AND (#{q.deptId} IS NULL OR s.dept_id = #{q.deptId})
              AND (#{q.majorId} IS NULL OR s.major_id = #{q.majorId})
              AND (#{q.enrollYear} IS NULL OR s.enroll_year = #{q.enrollYear})
              AND (#{q.status} IS NULL OR s.status = #{q.status})
            ORDER BY s.student_no
            """)
    IPage<StudentVo> selectVoPage(IPage<StudentVo> page, @Param("q") StudentQuery query);

    // ===== D3 多维统计（在读学生，支持院系/专业/年级筛选） =====

    @Select("""
            SELECT COUNT(*) FROM student s
            WHERE s.status = 1
              AND (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            """)
    Long countActive(@Param("deptId") Long deptId, @Param("majorId") Long majorId,
                     @Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT COALESCE(d.dept_name, '未分配') AS name, COUNT(*) AS value
            FROM student s
            LEFT JOIN department d ON d.dept_id = s.dept_id
            WHERE s.status = 1
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.dept_id, d.dept_name
            ORDER BY value DESC
            """)
    List<NameValueVo> statsByDept(@Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT COALESCE(m.major_name, '未分配') AS name, COUNT(*) AS value
            FROM student s
            LEFT JOIN major m ON m.major_id = s.major_id
            WHERE s.status = 1 AND s.dept_id = #{deptId}
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.major_id, m.major_name
            ORDER BY value DESC
            """)
    List<NameValueVo> statsByMajor(@Param("deptId") Long deptId, @Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT COALESCE(s.class_name, '未分班') AS name, COUNT(*) AS value
            FROM student s
            WHERE s.status = 1 AND s.major_id = #{majorId}
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.class_name
            ORDER BY name
            """)
    List<NameValueVo> statsByClass(@Param("majorId") Long majorId, @Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT COALESCE(CONCAT(s.enroll_year, '级'), '未知') AS name, COUNT(*) AS value
            FROM student s
            WHERE s.status = 1
              AND (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
            GROUP BY s.enroll_year
            ORDER BY s.enroll_year
            """)
    List<NameValueVo> statsByEnrollYear(@Param("deptId") Long deptId, @Param("majorId") Long majorId);

    @Select("""
            SELECT CASE s.gender WHEN 1 THEN '男' WHEN 2 THEN '女' ELSE '未知' END AS name, COUNT(*) AS value
            FROM student s
            WHERE s.status = 1
              AND (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.gender
            """)
    List<NameValueVo> statsByGender(@Param("deptId") Long deptId, @Param("majorId") Long majorId,
                                    @Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT COALESCE(s.origin_place, '未知') AS name, COUNT(*) AS value
            FROM student s
            WHERE s.status = 1
              AND (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.origin_place
            ORDER BY value DESC
            """)
    List<NameValueVo> statsByOrigin(@Param("deptId") Long deptId, @Param("majorId") Long majorId,
                                    @Param("enrollYear") Integer enrollYear);

    @Select("""
            SELECT CASE s.status WHEN 1 THEN '在读' WHEN 2 THEN '休学' WHEN 3 THEN '毕业' WHEN 0 THEN '退学' ELSE '未知' END AS name,
                   COUNT(*) AS value
            FROM student s
            WHERE (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY s.status
            """)
    List<NameValueVo> statsByStatus(@Param("deptId") Long deptId, @Param("majorId") Long majorId,
                                    @Param("enrollYear") Integer enrollYear);

    /** 选课偏好 Top10：只读教务域的 course_selection/course 数据 */
    @Select("""
            SELECT c.course_name AS name, COUNT(*) AS value
            FROM course_selection cs
            JOIN course c  ON c.course_id = cs.course_id
            JOIN student s ON s.student_id = cs.student_id
            WHERE cs.status = 1 AND s.status = 1
              AND (#{deptId} IS NULL OR s.dept_id = #{deptId})
              AND (#{majorId} IS NULL OR s.major_id = #{majorId})
              AND (#{enrollYear} IS NULL OR s.enroll_year = #{enrollYear})
            GROUP BY c.course_id, c.course_name
            ORDER BY value DESC
            LIMIT 10
            """)
    List<NameValueVo> statsCoursePreference(@Param("deptId") Long deptId, @Param("majorId") Long majorId,
                                            @Param("enrollYear") Integer enrollYear);
}
