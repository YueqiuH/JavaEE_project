package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@TableName("document_approval_task")
public class DocumentApprovalTask implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long taskId;
    private Long docId;
    private Long workflowId;
    private Integer roundNo;
    private Integer stepOrder;
    private String stepName;
    private Long approverId;
    private Integer status;
    private Date handledTime;
    private Date createTime;
}
