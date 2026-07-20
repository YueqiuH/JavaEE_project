package com.smartcampus.contract.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateProfileRequest implements Serializable {

    @Size(max = 32, message = "姓名不能超过 32 个字符")
    private String realName;

    /** 1=男, 2=女 */
    private Integer gender;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱不能超过 64 个字符")
    private String email;
}
