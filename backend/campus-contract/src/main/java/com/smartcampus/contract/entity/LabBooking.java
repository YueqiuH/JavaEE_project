package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "lab_booking")
public class LabBooking implements Serializable {
    @TableId(type = IdType.AUTO) private Long bookingId;
    private Long labId; private Long studentId;
    private Date bookingDate; private Integer startPeriod; private Integer endPeriod;
    private String purpose; private Integer status; private Date createTime;
}
