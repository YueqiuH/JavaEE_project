package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "scholarship")
public class Scholarship implements Serializable {
    @TableId(type = IdType.AUTO) private Long scholarshipId;
    private Long studentId; private String scholarshipType; private String title;
    private String reason; private String attachmentUrl; private Integer status;
    private Long reviewerId; private String reviewOpinion; private Date applyTime;
}
