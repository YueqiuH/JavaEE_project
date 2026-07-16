package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "asset")
public class Asset implements Serializable {
    @TableId(type = IdType.AUTO) private Long assetId;
    private String assetName; private String assetType; private Integer quantity;
    private Long deptId; private Long userId; private Integer status;
    private Long applyUserId; private Integer approveStatus; private Date createTime;
}
