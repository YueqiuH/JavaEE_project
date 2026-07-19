package com.smartcampus.contract.vo.student;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class EvaluationCourseDetailVo {

    private Long courseId;
    private String courseName;
    private String semester;
    private Long responseCount;
    private BigDecimal overallAverage;
    private BigDecimal teachingAverage;
    private BigDecimal contentAverage;
    private BigDecimal methodAverage;
    private List<String> anonymousComments;
}
