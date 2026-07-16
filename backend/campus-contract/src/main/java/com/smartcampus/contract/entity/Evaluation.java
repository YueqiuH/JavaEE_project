package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "evaluation")
public class Evaluation implements Serializable {
    @TableId(type = IdType.AUTO) private Long evaluationId;
    private Long studentId; private Long teacherId; private Long courseId;
    private String semester;
    private Integer scoreTeaching; private Integer scoreContent; private Integer scoreMethod;
    private String comment; private Date createTime;
}
