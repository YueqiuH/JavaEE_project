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
}
