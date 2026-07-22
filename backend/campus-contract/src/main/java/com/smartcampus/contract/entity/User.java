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
public class User implements Serializable {

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

    /** 姓名 */
    private String realName;

    /** 性别: 1=男, 2=女 */
    private Integer gender;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 家庭住址 */
    private String address;

    /** 职称 */
    private String title;

    /** 职务 */
    private String position;

    /** 所属院系ID */
    private Long deptId;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 院系名称（JOIN 填充） */
    @TableField(exist = false)
    private String deptName;
}
