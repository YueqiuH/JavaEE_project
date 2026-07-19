package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@TableName(value = "student")
public class StudentEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long studentId;
    private String studentName;
    private String studentBirth;
    private String studentAddress;
    private Long studentNo;
    private Long gradeId;
    private Integer studentAge;
    /** 性别: 1=男, 2=女 */
    private Integer gender;
    /** 所属院系ID */
    private Long deptId;
    /** 所属专业ID */
    private Long majorId;
    /** 班级 */
    private String className;
    /** 生源地(省份) */
    private String originPlace;
    /** 入学年份 */
    private Integer enrollYear;
    /** 学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学 */
    private Integer status;
}
