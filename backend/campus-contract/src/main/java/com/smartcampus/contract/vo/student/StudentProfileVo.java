package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentProfileVo {
    private Long studentId;
    private Long studentNo;
    private String studentName;
    private LocalDate studentBirth;
    private Integer studentAge;
    private Long gradeId;
    private String gradeName;
    private String currentAddress;
    private String phone;
    private String email;
    private String emergencyContact;
    private String emergencyPhone;
    private LocalDateTime updatedAt;
}
