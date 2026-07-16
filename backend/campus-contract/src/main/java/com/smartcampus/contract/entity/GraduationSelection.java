package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "graduation_selection")
public class GraduationSelection implements Serializable {
    @TableId(type = IdType.AUTO) private Long selectId;
    private Long topicId; private Long studentId;
    private Integer status; private Date selectTime;
}
