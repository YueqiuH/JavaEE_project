package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@TableName(value = "department")
public class Department implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long deptId;
    private String deptName;
    private String deptCode;
    private String description;
}
