package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@TableName("document_approver")
public class DocumentApprover implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long approverConfigId;
    private Long userId;
    private String displayName;
    private Integer status;
    private Date createTime;
    private Long updatedBy;
    private Date updatedTime;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private Integer userType;

    @TableField(exist = false)
    private Boolean qualified;
}
