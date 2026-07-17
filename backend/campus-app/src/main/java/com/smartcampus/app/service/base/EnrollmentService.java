package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.EnrollmentSaveRequest;
import com.smartcampus.contract.entity.Enrollment;
import com.smartcampus.contract.vo.EnrollmentStatsVo;
import com.smartcampus.contract.vo.EnrollmentVo;

public interface EnrollmentService {

    IPage<EnrollmentVo> pageVo(PageParam pageParam, Integer year, Long deptId);

    Enrollment create(EnrollmentSaveRequest request);

    Enrollment update(Long enrollmentId, EnrollmentSaveRequest request);

    void delete(Long enrollmentId);

    /** 指定年度的全校汇总、院系对比、历年趋势与生源地分布 */
    EnrollmentStatsVo stats(Integer year);
}
