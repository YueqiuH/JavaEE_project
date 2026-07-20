package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.Scholarship;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.ScholarshipApplicationVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ScholarshipMapper extends BaseMapper<Scholarship> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            <script>
            SELECT s.scholarship_id, s.application_no, s.student_id,
                   st.student_no, st.student_name, s.scholarship_type,
                   s.title, s.reason, s.attachment_url, s.status AS status_code,
                   s.reviewer_id, u.username AS reviewer_name, s.review_opinion,
                   s.counselor_id, cu.username AS counselor_name, s.counselor_opinion, s.counselor_reviewed_at,
                   s.academic_reviewer_id, au.username AS academic_reviewer_name,
                   s.academic_opinion, s.academic_reviewed_at,
                   s.apply_time, s.reviewed_at, s.selected_at, s.created_at, s.updated_at
            FROM scholarship s
            JOIN student st ON st.student_id = s.student_id
            LEFT JOIN user u ON u.user_id = s.reviewer_id
            LEFT JOIN user cu ON cu.user_id = s.counselor_id
            LEFT JOIN user au ON au.user_id = s.academic_reviewer_id
            WHERE 1 = 1
            <if test="studentId != null">AND s.student_id = #{studentId}</if>
            <if test="counselorUserId != null">AND st.counselor_id = #{counselorUserId}</if>
            <if test="status != null">AND s.status = #{status}</if>
            ORDER BY s.updated_at DESC, s.scholarship_id DESC
            </script>
            """)
    IPage<ScholarshipApplicationVo> selectApplicationPage(
            Page<ScholarshipApplicationVo> page,
            @Param("studentId") Long studentId,
            @Param("counselorUserId") Long counselorUserId,
            @Param("status") Integer status
    );

    @Select("""
            SELECT s.scholarship_id, s.application_no, s.student_id,
                   st.student_no, st.student_name, s.scholarship_type,
                   s.title, s.reason, s.attachment_url, s.status AS status_code,
                   s.reviewer_id, u.username AS reviewer_name, s.review_opinion,
                   s.counselor_id, cu.username AS counselor_name, s.counselor_opinion, s.counselor_reviewed_at,
                   s.academic_reviewer_id, au.username AS academic_reviewer_name,
                   s.academic_opinion, s.academic_reviewed_at,
                   s.apply_time, s.reviewed_at, s.selected_at, s.created_at, s.updated_at
            FROM scholarship s
            JOIN student st ON st.student_id = s.student_id
            LEFT JOIN user u ON u.user_id = s.reviewer_id
            LEFT JOIN user cu ON cu.user_id = s.counselor_id
            LEFT JOIN user au ON au.user_id = s.academic_reviewer_id
            WHERE s.scholarship_id = #{id}
            """)
    ScholarshipApplicationVo selectApplicationById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM student WHERE student_id = #{studentId} AND counselor_id = #{counselorUserId}")
    int countCounseledStudent(@Param("studentId") Long studentId, @Param("counselorUserId") Long counselorUserId);
}
