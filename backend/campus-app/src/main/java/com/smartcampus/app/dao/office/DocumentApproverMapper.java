package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.DocumentApprover;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DocumentApproverMapper extends BaseMapper<DocumentApprover> {

    @Select("""
            SELECT da.approver_config_id, da.user_id, da.display_name, da.status,
                   da.create_time, u.username
            FROM document_approver da
            JOIN `user` u ON u.user_id = da.user_id AND u.status = 1 AND u.user_type IN (2, 3, 4)
            WHERE da.status = 1
              AND EXISTS (
                  SELECT 1
                  FROM user_role ur
                  JOIN role_permission rp ON rp.role_id = ur.role_id
                  JOIN permission p ON p.permission_id = rp.permission_id AND p.status = 1
                  WHERE ur.user_id = da.user_id
                    AND p.permission_code = 'document:approve'
              )
            ORDER BY da.approver_config_id
            """)
    List<DocumentApprover> findAvailable();

    @Select("""
            SELECT da.approver_config_id, u.user_id, u.username, u.user_type,
                   COALESCE(da.display_name, u.username) AS display_name,
                   COALESCE(da.status, 0) AS status, da.create_time, da.updated_by, da.updated_time,
                   CASE WHEN da.approver_config_id IS NOT NULL AND da.status = 1 THEN TRUE ELSE FALSE END AS qualified
            FROM `user` u
            LEFT JOIN document_approver da ON da.user_id = u.user_id
            WHERE u.status = 1 AND u.user_type IN (2, 3, 4)
            ORDER BY u.user_type, u.username
            """)
    List<DocumentApprover> findCandidates();

    @Select("""
            SELECT COUNT(*) FROM `user`
            WHERE user_id = #{userId} AND status = 1 AND user_type IN (2, 3, 4)
            """)
    int countEligibleUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM document_workflow_step s
            JOIN document_workflow w ON w.workflow_id = s.workflow_id AND w.status = 1
            WHERE s.approver_id = #{userId}
            """)
    int countActiveWorkflowReferences(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM document_approver da
            JOIN `user` u ON u.user_id = da.user_id AND u.status = 1 AND u.user_type IN (2, 3, 4)
            WHERE da.user_id = #{userId}
              AND da.status = 1
              AND EXISTS (
                  SELECT 1
                  FROM user_role ur
                  JOIN role_permission rp ON rp.role_id = ur.role_id
                  JOIN permission p ON p.permission_id = rp.permission_id AND p.status = 1
                  WHERE ur.user_id = da.user_id
                    AND p.permission_code = 'document:approve'
              )
            """)
    int countAvailableByUserId(@Param("userId") Long userId);
}
