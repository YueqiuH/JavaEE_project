package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "psychology_questionnaire")
public class PsychologyQuestionnaire implements Serializable {
    @TableId(type = IdType.AUTO) private Long questionnaireId;
    private Long studentId; private String answers;
    private String riskLevel; private String aiAnalysis; private Date submitTime;
}
