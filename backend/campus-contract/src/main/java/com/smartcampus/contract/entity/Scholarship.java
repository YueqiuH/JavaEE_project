package com.smartcampus.contract.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
@TableName(value = "scholarship")
public class Scholarship implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long scholarshipId;
    private String applicationNo;
    private Long studentId;
    private String scholarshipType;
    private String title;
    private String reason;
    private String attachmentUrl;
    private Integer status;
    private Long reviewerId;
    private String reviewOpinion;
    private LocalDateTime applyTime;
    private LocalDateTime reviewedAt;
    private LocalDateTime selectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
