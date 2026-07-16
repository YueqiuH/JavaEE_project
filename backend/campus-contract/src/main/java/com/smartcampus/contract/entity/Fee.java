package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.math.BigDecimal; import java.util.Date;

@Data @ToString @TableName(value = "fee")
public class Fee implements Serializable {
    @TableId(type = IdType.AUTO) private Long feeId;
    private Long studentId; private String feeType;
    private BigDecimal amount; private String semester;
    private Integer status; private Date dueDate; private Date createTime;
}
