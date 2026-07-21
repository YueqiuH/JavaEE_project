package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@TableName("document_workflow_step")
public class DocumentWorkflowStep implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long stepId;
    private Long workflowId;
    private Integer stepOrder;
    private String stepName;
    private Long approverId;

    @TableField(exist = false)
    private String approverName;

    @TableField(exist = false)
    private String approverUsername;
}
