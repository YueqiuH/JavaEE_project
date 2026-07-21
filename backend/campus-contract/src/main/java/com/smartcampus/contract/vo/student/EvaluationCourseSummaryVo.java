package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluationCourseSummaryVo {

    private String targetType;
    private Long teacherId;
    private String teacherName;
    private Long courseId;
    private String courseName;
    private String semester;
    private Long responseCount;
    private BigDecimal overallAverage;
    private BigDecimal teachingAverage;
    private BigDecimal contentAverage;
    private BigDecimal methodAverage;
}
