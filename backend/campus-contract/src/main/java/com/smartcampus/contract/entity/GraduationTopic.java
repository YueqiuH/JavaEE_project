package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "graduation_topic")
public class GraduationTopic implements Serializable {
    @TableId(type = IdType.AUTO) private Long topicId;
    private String title; private String description;
    private Long teacherId; private String majorRequire;
    private Integer maxStudent; private Integer currentStudent;
    private String semester;
    /** 1=可选, 0=不可选/已满, 2=待审批 */
    private Integer status;
}
