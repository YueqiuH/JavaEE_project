package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.dto.StaffQuery;
import com.smartcampus.contract.entity.UserEntity;
import com.smartcampus.contract.vo.StaffVo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 教职工档案数据访问（user 表中 user_type IN (2,3) 的记录）。
 * 登录认证相关查询见 campus-auth 的 AuthUserMapper，此处仅维护档案业务。
 */
public interface StaffMapper extends BaseMapper<UserEntity> {

    @Select("""
            SELECT u.user_id, u.username, u.user_type, u.real_name, u.gender, u.phone, u.email,
                   u.title, u.position, u.dept_id, d.dept_name, u.status, u.created_at
            FROM `user` u
            LEFT JOIN department d ON d.dept_id = u.dept_id
            WHERE u.user_type IN (2, 3)
              AND (#{q.userType} IS NULL OR u.user_type = #{q.userType})
              AND (#{q.deptId} IS NULL OR u.dept_id = #{q.deptId})
              AND (#{q.keyword} IS NULL OR #{q.keyword} = ''
                   OR u.username LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.real_name LIKE CONCAT('%', #{q.keyword}, '%')
                   OR u.phone LIKE CONCAT('%', #{q.keyword}, '%'))
            ORDER BY u.username
            """)
    IPage<StaffVo> selectVoPage(IPage<StaffVo> page, @Param("q") StaffQuery query);

    @Insert("""
            INSERT IGNORE INTO user_role (user_id, role_id)
            SELECT #{userId}, role_id FROM `role` WHERE role_code = #{roleCode}
            """)
    int assignRole(@Param("userId") Long userId, @Param("roleCode") String roleCode);
}
