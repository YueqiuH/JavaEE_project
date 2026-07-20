package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.dto.student.StudentProfileUpdateRequest;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.entity.StudentStatusChange;
import com.smartcampus.contract.vo.student.MajorOptionVo;
import com.smartcampus.contract.vo.student.StatusChangeApplicationVo;
import com.smartcampus.contract.vo.student.StudentProfileVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatusChangeMapper extends BaseMapper<StudentStatusChange> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            SELECT s.student_id, s.student_no, s.student_name, s.student_birth, s.student_age,
                   s.grade_id, g.grade_name, p.current_address, p.phone, p.email,
                   p.emergency_contact, p.emergency_phone, p.updated_at
            FROM student s
            LEFT JOIN grade g ON g.grade_id = s.grade_id
            LEFT JOIN student_profile_extension p ON p.student_id = s.student_id
            WHERE s.student_id = #{studentId}
            """)
    StudentProfileVo selectProfile(@Param("studentId") Long studentId);

    @Insert("""
            INSERT INTO student_profile_extension
                (student_id, current_address, phone, email, emergency_contact, emergency_phone, updated_at)
            VALUES
                (#{studentId}, #{profile.currentAddress}, #{profile.phone}, #{profile.email},
                 #{profile.emergencyContact}, #{profile.emergencyPhone}, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
                current_address = VALUES(current_address), phone = VALUES(phone), email = VALUES(email),
                emergency_contact = VALUES(emergency_contact), emergency_phone = VALUES(emergency_phone),
                updated_at = CURRENT_TIMESTAMP
            """)
    int upsertProfile(@Param("studentId") Long studentId, @Param("profile") StudentProfileUpdateRequest profile);

    @Select("SELECT major_id, major_name, major_code FROM major ORDER BY major_name")
    List<MajorOptionVo> selectMajorOptions();

    @Select("SELECT COUNT(*) FROM major WHERE major_id = #{majorId}")
    int countMajor(@Param("majorId") Long majorId);

    @Select("""
            <script>
            SELECT c.change_id, c.application_no, c.student_id, s.student_no, s.student_name,
                   c.change_type, c.reason, c.new_major_id, m.major_name AS new_major_name,
                   c.desired_effective_date, c.status AS status_code,
                   c.counselor_id, cu.username AS counselor_name, c.counselor_opinion, c.counselor_reviewed_at,
                   c.academic_reviewer_id, au.username AS academic_reviewer_name,
                   c.academic_opinion, c.academic_reviewed_at,
                   c.apply_time, c.created_at, c.updated_at
            FROM student_status_change c
            JOIN student s ON s.student_id = c.student_id
            LEFT JOIN major m ON m.major_id = c.new_major_id
            LEFT JOIN user cu ON cu.user_id = c.counselor_id
            LEFT JOIN user au ON au.user_id = c.academic_reviewer_id
            WHERE 1 = 1
            <if test="studentId != null">AND c.student_id = #{studentId}</if>
            <if test="counselorUserId != null">AND s.counselor_id = #{counselorUserId}</if>
            <if test="status != null">AND c.status = #{status}</if>
            <if test="excludeDraft">AND c.status != 0</if>
            ORDER BY c.updated_at DESC, c.change_id DESC
            </script>
            """)
    IPage<StatusChangeApplicationVo> selectApplicationPage(
            Page<StatusChangeApplicationVo> page,
            @Param("studentId") Long studentId,
            @Param("counselorUserId") Long counselorUserId,
            @Param("status") Integer status,
            @Param("excludeDraft") boolean excludeDraft
    );

    @Select("""
            SELECT c.change_id, c.application_no, c.student_id, s.student_no, s.student_name,
                   c.change_type, c.reason, c.new_major_id, m.major_name AS new_major_name,
                   c.desired_effective_date, c.status AS status_code,
                   c.counselor_id, cu.username AS counselor_name, c.counselor_opinion, c.counselor_reviewed_at,
                   c.academic_reviewer_id, au.username AS academic_reviewer_name,
                   c.academic_opinion, c.academic_reviewed_at,
                   c.apply_time, c.created_at, c.updated_at
            FROM student_status_change c
            JOIN student s ON s.student_id = c.student_id
            LEFT JOIN major m ON m.major_id = c.new_major_id
            LEFT JOIN user cu ON cu.user_id = c.counselor_id
            LEFT JOIN user au ON au.user_id = c.academic_reviewer_id
            WHERE c.change_id = #{id}
            """)
    StatusChangeApplicationVo selectApplicationById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM student WHERE student_id = #{studentId} AND counselor_id = #{counselorUserId}")
    int countCounseledStudent(@Param("studentId") Long studentId, @Param("counselorUserId") Long counselorUserId);

    @Select("""
            SELECT COUNT(*) FROM student_status_change
            WHERE student_id = #{studentId} AND change_id != #{excludeId} AND status IN (1, 2)
            """)
    int countOtherActiveApplications(@Param("studentId") Long studentId, @Param("excludeId") Long excludeId);
}
