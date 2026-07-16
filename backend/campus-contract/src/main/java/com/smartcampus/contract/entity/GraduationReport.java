package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "graduation_report")
public class GraduationReport implements Serializable {
    @TableId(type = IdType.AUTO) private Long reportId;
    private Long studentId; private Long topicId;
    private String reportType; private String fileUrl; private String content;
    private Date submitTime; private String teacherFeedback;
    private Date feedbackTime; private Integer score;
}
