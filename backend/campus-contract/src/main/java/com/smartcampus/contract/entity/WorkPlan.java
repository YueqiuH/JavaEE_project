package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "work_plan")
public class WorkPlan implements Serializable {
    @TableId(type = IdType.AUTO) private Long planId;
    private Long userId; private String planType; private String content;
    private Date startDate; private Date endDate; private Integer status;
    private String supervisorComment; private Date createTime;
}
