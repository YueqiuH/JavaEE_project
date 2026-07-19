package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WorkPlanAssigneeVo implements Serializable {

    private Long userId;
    private String username;
    private Integer userType;
}
