package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("student_profile_extension")
public class StudentProfileExtension implements Serializable {

    @TableId(type = IdType.INPUT)
    private Long studentId;
    private String currentAddress;
    private String phone;
    private String email;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDateTime updatedAt;
}
