package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教职工档案列表视图（不含密码，含院系名称）。
 */
@Data
public class StaffVo implements Serializable {

    private Long userId;

    /** 工号/登录账号 */
    private String username;

    /** 人员类别: 2=教师, 3=教职工 */
    private Integer userType;

    private String realName;

    /** 性别: 1=男, 2=女 */
    private Integer gender;

    private String phone;

    private String email;

    /** 职称 */
    private String title;

    /** 职务 */
    private String position;

    private Long deptId;

    private String deptName;

    /** 状态: 1=启用, 0=停用 */
    private Integer status;

    private LocalDateTime createdAt;
}
