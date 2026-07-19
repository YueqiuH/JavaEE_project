package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EvaluationTaskVo {

    private String targetType;
    private Long selectionId;
    private Long scheduleId;
    private Long evaluationId;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private String semester;
    private String status;
    private Integer scoreTeaching;
    private Integer scoreContent;
    private Integer scoreMethod;
    private BigDecimal overallScore;
    private String comment;
    private LocalDateTime submittedAt;
}
