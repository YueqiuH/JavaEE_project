package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生档案新增/修改请求。
 */
@Data
public class StudentSaveRequest implements Serializable {

    @NotNull(message = "学号不能为空")
    private Long studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名不能超过 32 个字符")
    private String studentName;

    /** 性别: 1=男, 2=女 */
    private Integer gender;

    /** 生日，格式 yyyy-MM-dd */
    private String studentBirth;

    private Integer studentAge;

    @Size(max = 128, message = "地址不能超过 128 个字符")
    private String studentAddress;

    /** 生源地(省份) */
    @Size(max = 32, message = "生源地不能超过 32 个字符")
    private String originPlace;

    @Size(max = 32, message = "班级不能超过 32 个字符")
    private String className;

    private Integer enrollYear;

    private Long deptId;

    private Long majorId;

    /** 学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学 */
    private Integer status;
}
