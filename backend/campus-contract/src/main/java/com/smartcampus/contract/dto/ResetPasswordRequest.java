package com.smartcampus.contract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class ResetPasswordRequest implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度 6-32 位")
    private String newPassword;

    /** 验证码（前端展示后用户输入） */
    @NotBlank(message = "验证码不能为空")
    private String code;
}
