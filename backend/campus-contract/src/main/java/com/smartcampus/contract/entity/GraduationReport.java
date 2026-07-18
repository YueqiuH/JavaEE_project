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
    /** 开题报告/中期报告/论文终稿/答辩记录 */
    private String reportType;
    private String fileUrl; private String content;
    private Date submitTime;
    /** 反馈/批注/评分记录(JSON格式存储多功能数据) */
    private String teacherFeedback;
    private Date feedbackTime;
    /** 通用评分字段，用途由reportType决定 */
    private Integer score;
}
