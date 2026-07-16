package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@TableName(value = "menu")
public class MenuEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long menuId;
    private String title;
    private String path;
    private String icon;
    private Long parentId;
    private String permissionCode;
    private Integer sortOrder;
    private Integer status;
}
