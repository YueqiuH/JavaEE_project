package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "document_approval")
public class DocumentApproval implements Serializable {
    @TableId(type = IdType.AUTO) private Long approvalId;
    private Long docId; private Long approverId;
    private String action; private String opinion; private Date approvalTime;
}
