package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "student_status_change")
public class StudentStatusChange implements Serializable {
    @TableId(type = IdType.AUTO) private Long changeId;
    private Long studentId; private String changeType; private String reason;
    private Long newMajorId; private Integer status;
    private Long counselorId; private String counselorOpinion;
    private Long adminId; private String adminOpinion; private Date applyTime;
}
