package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.Lab;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.LabVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LabMapper extends BaseMapper<Lab> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            <script>
            SELECT l.lab_id, l.lab_no, l.lab_name, l.location, l.capacity, l.description,
                   l.manager_id, manager.username AS manager_name, l.status AS status_code,
                   l.created_at, l.updated_at,
                   (SELECT COUNT(*) FROM lab_resource r WHERE r.lab_id = l.lab_id AND r.status = 1) AS resource_count,
                   (SELECT COUNT(*) FROM lab_open_slot s WHERE s.lab_id = l.lab_id AND s.open_date &gt;= CURRENT_DATE) AS upcoming_slot_count,
                   (SELECT MIN(s.open_date) FROM lab_open_slot s WHERE s.lab_id = l.lab_id AND s.open_date &gt;= CURRENT_DATE) AS next_open_date,
                   (SELECT COUNT(*) FROM lab_booking b WHERE b.lab_id = l.lab_id
                    AND b.booking_date = CURRENT_DATE
                    AND (b.status = 4 OR (b.status = 1 AND b.expires_at &gt; CURRENT_TIMESTAMP))) AS active_booking_count
            FROM lab l
            LEFT JOIN user manager ON manager.user_id = l.manager_id
            WHERE 1 = 1
            <if test="managerId != null">AND l.manager_id = #{managerId}</if>
            <if test="studentVisible">AND l.status = 1</if>
            <if test="status != null">AND l.status = #{status}</if>
            ORDER BY l.updated_at DESC, l.lab_id DESC
            </script>
            """)
    IPage<LabVo> selectLabPage(
            Page<LabVo> page,
            @Param("managerId") Long managerId,
            @Param("studentVisible") boolean studentVisible,
            @Param("status") Integer status
    );

    @Select("""
            SELECT l.lab_id, l.lab_no, l.lab_name, l.location, l.capacity, l.description,
                   l.manager_id, manager.username AS manager_name, l.status AS status_code,
                   l.created_at, l.updated_at,
                   (SELECT COUNT(*) FROM lab_resource r WHERE r.lab_id = l.lab_id AND r.status = 1) AS resource_count,
                   (SELECT COUNT(*) FROM lab_open_slot s WHERE s.lab_id = l.lab_id AND s.open_date >= CURRENT_DATE) AS upcoming_slot_count,
                   (SELECT MIN(s.open_date) FROM lab_open_slot s WHERE s.lab_id = l.lab_id AND s.open_date >= CURRENT_DATE) AS next_open_date,
                   (SELECT COUNT(*) FROM lab_booking b WHERE b.lab_id = l.lab_id
                    AND b.booking_date = CURRENT_DATE
                    AND (b.status = 4 OR (b.status = 1 AND b.expires_at > CURRENT_TIMESTAMP))) AS active_booking_count
            FROM lab l
            LEFT JOIN user manager ON manager.user_id = l.manager_id
            WHERE l.lab_id = #{id}
            """)
    LabVo selectLabView(@Param("id") Long id);

    @Select("SELECT * FROM lab WHERE lab_id = #{id} FOR UPDATE")
    Lab selectByIdForUpdate(@Param("id") Long id);
}
