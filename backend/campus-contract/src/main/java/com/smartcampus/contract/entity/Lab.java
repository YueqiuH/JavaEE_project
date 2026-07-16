package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "lab")
public class Lab implements Serializable {
    @TableId(type = IdType.AUTO) private Long labId;
    private String labName; private String location;
    private Integer capacity; private String description; private Integer status;
}
