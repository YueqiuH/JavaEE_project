package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString(exclude = "password")
@TableName(value = "user")
public class UserEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    @JsonIgnore
    private String password;

    @TableField(value = "user_type")
    private Integer userType;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
