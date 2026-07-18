package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EvaluationTeacherOverviewVo {

    private Long teacherId;
    private String teacherName;
    private Long responseCount;
    private BigDecimal overallAverage;
    private BigDecimal teachingAverage;
    private BigDecimal contentAverage;
    private BigDecimal methodAverage;
    private List<EvaluationCourseSummaryVo> courses;
}
