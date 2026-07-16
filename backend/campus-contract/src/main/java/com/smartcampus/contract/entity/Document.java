package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "document")
public class Document implements Serializable {
    @TableId(type = IdType.AUTO) private Long docId;
    private String title; private String docType; private String content;
    private Long initiatorId; private Long currentApproverId; private Integer status;
    private String approvalChain; private Date createTime;
}
