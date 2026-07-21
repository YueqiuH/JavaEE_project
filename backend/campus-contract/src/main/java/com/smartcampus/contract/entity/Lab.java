package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "lab")
public class Lab implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long labId;
    private String labNo;
    private String labName;
    private String location;
    private Integer capacity;
    private String description;
    private Long managerId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
