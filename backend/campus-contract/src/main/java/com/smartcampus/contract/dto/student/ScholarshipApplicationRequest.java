package com.smartcampus.contract.dto.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScholarshipApplicationRequest {

    @NotBlank(message = "请选择申请类型")
    @Pattern(regexp = "SCHOLARSHIP|DIFFICULTY_GRANT|STUDENT_LOAN", message = "申请类型不正确")
    private String scholarshipType;

    @NotBlank(message = "请填写申请标题")
    @Size(max = 128, message = "申请标题不能超过128个字符")
    private String title;

    @NotBlank(message = "请填写申请理由")
    @Size(max = 2000, message = "申请理由不能超过2000个字符")
    private String reason;

    @Size(max = 256, message = "附件地址不能超过256个字符")
    private String attachmentUrl;
}
