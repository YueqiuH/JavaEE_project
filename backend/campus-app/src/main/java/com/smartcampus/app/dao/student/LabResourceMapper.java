package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.LabResource;
import com.smartcampus.contract.vo.student.LabResourceVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LabResourceMapper extends BaseMapper<LabResource> {

    @Select("""
            <script>
            SELECT r.resource_id, r.lab_id, l.lab_name, l.location, r.resource_no,
                   r.resource_name, r.resource_type, r.description, r.status AS status_code,
                   r.created_at, r.updated_at
            FROM lab_resource r
            JOIN lab l ON l.lab_id = r.lab_id
            WHERE 1 = 1
            <if test="labId != null">AND r.lab_id = #{labId}</if>
            <if test="managerId != null">AND l.manager_id = #{managerId}</if>
            <if test="studentVisible">AND l.status = 1 AND r.status = 1</if>
            ORDER BY l.lab_name, r.resource_type, r.resource_no
            </script>
            """)
    List<LabResourceVo> selectResourceList(
            @Param("labId") Long labId,
            @Param("managerId") Long managerId,
            @Param("studentVisible") boolean studentVisible
    );

    @Select("""
            SELECT r.resource_id, r.lab_id, l.lab_name, l.location, r.resource_no,
                   r.resource_name, r.resource_type, r.description, r.status AS status_code,
                   r.created_at, r.updated_at
            FROM lab_resource r
            JOIN lab l ON l.lab_id = r.lab_id
            WHERE r.resource_id = #{id}
            """)
    LabResourceVo selectResourceView(@Param("id") Long id);
}
