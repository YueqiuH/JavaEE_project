package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;

@Data @ToString @TableName(value = "score")
public class ScoreEntity implements Serializable {
    @TableId(type = IdType.AUTO) private Long scoreId;
    private Long studentId;
    private Long courseId;
    private Integer scoreScore;
    private String semester;
    private BigDecimal gpa;
    private Integer status;
}
