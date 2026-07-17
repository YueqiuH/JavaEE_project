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
            JOIN `user` u ON u.user_id = da.user_id AND u.status = 1
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
            SELECT COUNT(*)
            FROM document_approver da
            JOIN `user` u ON u.user_id = da.user_id AND u.status = 1
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
