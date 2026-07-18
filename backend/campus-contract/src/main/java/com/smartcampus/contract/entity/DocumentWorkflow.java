package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@ToString
@TableName("document_workflow")
public class DocumentWorkflow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long workflowId;
    private String workflowName;
    private String docType;
    private Integer version;
    private Integer status;
    private Long createdBy;
    private Date createTime;

    @TableField(exist = false)
    private List<DocumentWorkflowStep> steps;
}
