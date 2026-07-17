package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @ToString @TableName(value = "score")
public class ScoreEntity implements Serializable {
    @TableId(type = IdType.AUTO) private Long scoreId;
    private Long studentId;
    private Long courseId;
    /** 教学班 schedule_id —— 成绩种类(必修/选修/限选)绑定在教学班上 */
    private Long scheduleId;
    /** 平时成绩 0-100 */
    private Integer regularScore;
    /** 期末考试成绩 0-100 */
    private Integer examScore;
    /** 总评成绩 = round(regularScore*regularRatio + examScore*examRatio)，由后端计算 */
    private Integer scoreScore;
    private String semester;
    /** 平时占比 0-1 (如 0.3) */
    private BigDecimal regularRatio;
    /** 期末占比 0-1 (如 0.7) */
    private BigDecimal examRatio;
    /** 绩点 0-4.0 */
    private BigDecimal gpa;
    /** 1=已通过, 0=未通过 */
    private Integer status;
    /** 1=草稿(教师暂存), 2=已发布(学生可见), 3=已归档(只读) */
    private Integer publishStatus;
    /** 录入教师ID */
    private Long teacherId;
    /** 发布时间 */
    private LocalDateTime publishTime;
    /** 最后修改时间 */
    private LocalDateTime modifiedTime;
    /** 是否为重修成绩 */
    private Integer isRetake;
}
