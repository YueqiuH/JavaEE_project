package com.smartcampus.contract.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 教职工档案新增/修改请求（修改时忽略 username）。
 */
@Data
public class StaffSaveRequest implements Serializable {

    /** 工号/登录账号，创建后不可修改 */
    @NotBlank(message = "工号不能为空")
    @Size(max = 64, message = "工号不能超过 64 个字符")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名不能超过 32 个字符")
    private String realName;

    /** 人员类别: 2=教师, 3=教职工 */
    @NotNull(message = "人员类别不能为空")
    @Min(value = 2, message = "人员类别只能是教师(2)或教职工(3)")
    @Max(value = 3, message = "人员类别只能是教师(2)或教职工(3)")
    private Integer userType;

    /** 性别: 1=男, 2=女 */
    private Integer gender;

    @Size(max = 20, message = "电话不能超过 20 个字符")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过 64 个字符")
    private String email;

    /** 职称 */
    @Size(max = 32, message = "职称不能超过 32 个字符")
    private String title;

    /** 职务 */
    @Size(max = 32, message = "职务不能超过 32 个字符")
    private String position;

    private Long deptId;

    /** 状态: 1=启用, 0=停用 */
    private Integer status;
}
