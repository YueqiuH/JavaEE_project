package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "psychology_warning")
public class PsychologyWarning implements Serializable {
    @TableId(type = IdType.AUTO) private Long warningId;
    private Long studentId; private String warningLevel; private String reason;
    private Long counselorId; private Integer isRead;
    private String handleRemark; private Date createTime;
}
