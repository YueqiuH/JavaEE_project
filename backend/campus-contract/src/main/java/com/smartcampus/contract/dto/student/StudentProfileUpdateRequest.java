package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentProfileUpdateRequest {

    @Size(max = 128, message = "常住地址不能超过128个字符")
    private String currentAddress;

    @Pattern(regexp = "^$|^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱不能超过128个字符")
    private String email;

    @Size(max = 32, message = "紧急联系人不能超过32个字符")
    private String emergencyContact;

    @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "紧急联系电话格式不正确")
    private String emergencyPhone;
}
