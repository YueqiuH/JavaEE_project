package com.smartcampus.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.Menu;
import com.smartcampus.contract.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AuthUserMapper extends BaseMapper<User> {

    @Select("""
            SELECT u.user_id, u.username, u.password, u.user_type, u.real_name, u.gender,
                   u.phone, u.email, u.title, u.position, u.dept_id, u.status,
                   u.created_at, u.updated_at, d.dept_name
            FROM `user` u
            LEFT JOIN department d ON d.dept_id = u.dept_id
            WHERE u.username = #{username} AND u.status = 1
            LIMIT 1
            """)
    User findActiveByUsername(@Param("username") String username);

    @Select("""
            SELECT u.user_id, u.username, u.password, u.user_type, u.real_name, u.gender,
                   u.phone, u.email, u.title, u.position, u.dept_id, u.status,
                   u.created_at, u.updated_at, d.dept_name
            FROM `user` u
            LEFT JOIN department d ON d.dept_id = u.dept_id
            WHERE u.user_id = #{userId} AND u.status = 1
            LIMIT 1
            """)
    User findActiveById(@Param("userId") Long userId);

    @Select("""
            SELECT r.role_code
            FROM `role` r
            JOIN user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId} AND r.status = 1
            ORDER BY r.role_code
            """)
    List<String> findRoleCodes(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT p.permission_code
            FROM permission p
            JOIN role_permission rp ON rp.permission_id = p.permission_id
            JOIN `role` r ON r.role_id = rp.role_id AND r.status = 1
            JOIN user_role ur ON ur.role_id = r.role_id
            WHERE ur.user_id = #{userId} AND p.status = 1
            ORDER BY p.permission_code
            """)
    List<String> findPermissionCodes(@Param("userId") Long userId);

    @Select("""
            SELECT m.menu_id, m.title, m.path, m.icon, m.parent_id,
                   m.permission_code, m.sort_order, m.status
            FROM menu m
            WHERE m.status = 1
              AND (
                  m.permission_code IS NULL
                  OR m.permission_code IN (
                      SELECT p.permission_code
                      FROM permission p
                      JOIN role_permission rp ON rp.permission_id = p.permission_id
                      JOIN `role` r ON r.role_id = rp.role_id AND r.status = 1
                      JOIN user_role ur ON ur.role_id = r.role_id
                      WHERE ur.user_id = #{userId} AND p.status = 1
                  )
              )
            ORDER BY m.sort_order, m.menu_id
            """)
    List<Menu> findMenus(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM student WHERE student_no = #{username} AND phone = #{phone}")
    int findStudentPhone(@Param("username") String username, @Param("phone") String phone);
}
