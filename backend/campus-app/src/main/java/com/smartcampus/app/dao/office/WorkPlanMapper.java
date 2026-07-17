package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.WorkPlan;
import com.smartcampus.contract.vo.WorkPlanAssigneeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface WorkPlanMapper extends BaseMapper<WorkPlan> {

    @Select("""
            SELECT DISTINCT u.user_id, u.username, u.user_type
            FROM `user` u
            JOIN user_role ur ON ur.user_id = u.user_id
            JOIN `role` r ON r.role_id = ur.role_id AND r.status = 1
            JOIN role_permission rp ON rp.role_id = r.role_id
            JOIN permission p ON p.permission_id = rp.permission_id AND p.status = 1
            WHERE u.status = 1
              AND u.user_type IN (2, 3)
              AND u.user_id <> #{excludeUserId}
              AND p.permission_code = 'work-plan:self'
            ORDER BY u.username
            """)
    List<WorkPlanAssigneeVo> findAssignableUsers(@Param("excludeUserId") Long excludeUserId);

    @Select("""
            SELECT COUNT(DISTINCT u.user_id)
            FROM `user` u
            JOIN user_role ur ON ur.user_id = u.user_id
            JOIN `role` r ON r.role_id = ur.role_id AND r.status = 1
            JOIN role_permission rp ON rp.role_id = r.role_id
            JOIN permission p ON p.permission_id = rp.permission_id AND p.status = 1
            WHERE u.status = 1
              AND u.user_type IN (2, 3)
              AND u.user_id = #{userId}
              AND p.permission_code = 'work-plan:self'
            """)
    int countAssignableUser(@Param("userId") Long userId);
}
