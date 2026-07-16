package com.smartcampus.contract.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data; import lombok.ToString;
import java.io.Serializable;

@Data @ToString @TableName(value = "competition_member")
public class CompetitionMember implements Serializable {
    @TableId(type = IdType.AUTO) private Long memberId;
    private Long teamId; private Long studentId; private String role;
}
