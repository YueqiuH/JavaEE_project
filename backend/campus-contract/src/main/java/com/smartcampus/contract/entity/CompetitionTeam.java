package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable; import java.util.Date;

@Data @ToString @TableName(value = "competition_team")
public class CompetitionTeam implements Serializable {
    @TableId(type = IdType.AUTO) private Long teamId;
    private Long competitionId; private String teamName; private Long leaderId;
    private Integer status; private Long reviewerId; private Date applyTime;
}
