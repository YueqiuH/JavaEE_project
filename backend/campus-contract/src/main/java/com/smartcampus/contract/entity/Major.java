package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@TableName(value = "major")
public class Major implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long majorId;
    private Long deptId;
    private String majorName;
    private String majorCode;
    private String cultivationPlan;
}
