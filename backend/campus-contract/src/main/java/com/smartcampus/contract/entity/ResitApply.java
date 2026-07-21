package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;
import java.util.Date;

@Data @ToString @TableName(value = "resit_apply")
public class ResitApply implements Serializable {
    @TableId(type = IdType.AUTO) private Long applyId;
    private Long studentId;
    private Long courseId;
    private Long examId;
    private String applyType;
    private String reason;
    private Integer status;
    private Date applyTime;
}
