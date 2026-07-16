package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.math.BigDecimal; import java.util.Date;

@Data @ToString @TableName(value = "payment")
public class Payment implements Serializable {
    @TableId(type = IdType.AUTO) private Long paymentId;
    private Long studentId; private Long feeId;
    private BigDecimal amount; private String paymentType; private String description;
    private Date paymentTime;
}
