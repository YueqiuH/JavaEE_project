package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 学生档案列表视图（含院系/专业名称）。
 */
@Data
public class StudentVo implements Serializable {

    private Long studentId;

    private Long studentNo;

    private String studentName;

    /** 性别: 1=男, 2=女 */
    private Integer gender;

    private LocalDate studentBirth;

    private Integer studentAge;

    private String studentAddress;

    private String phone;

    /** 生源地(省份) */
    private String originPlace;

    private String className;

    private Integer enrollYear;

    /** 学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学 */
    private Integer status;

    private Long deptId;

    private String deptName;

    private Long majorId;

    private String majorName;
}
