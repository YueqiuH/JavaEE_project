package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.math.BigDecimal;

@Data @ToString @TableName(value = "enrollment")
public class Enrollment implements Serializable {
    @TableId(type = IdType.AUTO) private Long enrollmentId;
    private Long majorId; private Integer planCount;
    private Integer actualCount; private Integer year; private BigDecimal reportRate;
}
