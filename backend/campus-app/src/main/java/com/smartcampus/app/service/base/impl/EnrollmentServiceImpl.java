package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.EnrollmentMapper;
import com.smartcampus.app.dao.base.MajorMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.EnrollmentService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.EnrollmentSaveRequest;
import com.smartcampus.contract.entity.Enrollment;
import com.smartcampus.contract.vo.EnrollmentStatVo;
import com.smartcampus.contract.vo.EnrollmentStatsVo;
import com.smartcampus.contract.vo.EnrollmentVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentMapper enrollmentMapper;
    private final MajorMapper majorMapper;

    public EnrollmentServiceImpl(EnrollmentMapper enrollmentMapper, MajorMapper majorMapper) {
        this.enrollmentMapper = enrollmentMapper;
        this.majorMapper = majorMapper;
    }

    @Override
    public IPage<EnrollmentVo> pageVo(PageParam pageParam, Integer year, Long deptId) {
        Page<EnrollmentVo> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        return enrollmentMapper.selectVoPage(page, year, deptId);
    }

    @Override
    public Enrollment create(EnrollmentSaveRequest request) {
        if (majorMapper.selectById(request.getMajorId()) == null) {
            throw new BusinessException(BaseErrorCodes.MAJOR_NOT_FOUND);
        }
        assertPlanAvailable(request.getMajorId(), request.getYear(), null);
        Enrollment enrollment = new Enrollment();
        applyRequest(enrollment, request);
        enrollmentMapper.insert(enrollment);
        return enrollment;
    }

    @Override
    public Enrollment update(Long enrollmentId, EnrollmentSaveRequest request) {
        Enrollment enrollment = requireEnrollment(enrollmentId);
        if (majorMapper.selectById(request.getMajorId()) == null) {
            throw new BusinessException(BaseErrorCodes.MAJOR_NOT_FOUND);
        }
        assertPlanAvailable(request.getMajorId(), request.getYear(), enrollmentId);
        applyRequest(enrollment, request);
        enrollmentMapper.updateById(enrollment);
        return enrollment;
    }

    @Override
    public void delete(Long enrollmentId) {
        requireEnrollment(enrollmentId);
        enrollmentMapper.deleteById(enrollmentId);
    }

    @Override
    public EnrollmentStatsVo stats(Integer year) {
        List<EnrollmentStatVo> byDept = enrollmentMapper.statsByDept(year);
        byDept.forEach(stat -> stat.setReportRate(rate(stat.getActualCount(), stat.getPlanCount())));

        List<EnrollmentStatVo> trend = enrollmentMapper.statsTrend();
        trend.forEach(stat -> stat.setReportRate(rate(stat.getActualCount(), stat.getPlanCount())));

        long planTotal = byDept.stream()
                .mapToLong(s -> { Long v = s.getPlanCount(); return v != null ? v : 0L; }).sum();
        long actualTotal = byDept.stream()
                .mapToLong(s -> { Long v = s.getActualCount(); return v != null ? v : 0L; }).sum();

        EnrollmentStatsVo stats = new EnrollmentStatsVo();
        stats.setYear(year);
        stats.setPlanTotal(planTotal);
        stats.setActualTotal(actualTotal);
        stats.setReportRate(rate(actualTotal, planTotal));
        stats.setByDept(byDept);
        stats.setTrend(trend);
        stats.setOriginDistribution(enrollmentMapper.originDistribution(year));
        return stats;
    }

    private Enrollment requireEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BusinessException(BaseErrorCodes.ENROLLMENT_NOT_FOUND);
        }
        return enrollment;
    }

    /** 同一专业同一年度只能有一条计划 */
    private void assertPlanAvailable(Long majorId, Integer year, Long excludeId) {
        LambdaQueryWrapper<Enrollment> wrapper = new LambdaQueryWrapper<Enrollment>()
                .eq(Enrollment::getMajorId, majorId)
                .eq(Enrollment::getYear, year);
        if (excludeId != null) {
            wrapper.ne(Enrollment::getEnrollmentId, excludeId);
        }
        if (enrollmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCodes.ENROLLMENT_DUPLICATE);
        }
    }

    private void applyRequest(Enrollment enrollment, EnrollmentSaveRequest request) {
        enrollment.setMajorId(request.getMajorId());
        enrollment.setYear(request.getYear());
        enrollment.setPlanCount(request.getPlanCount());
        Integer actualCount = request.getActualCount();
        enrollment.setActualCount(actualCount != null ? actualCount : 0);
        Integer enrollmentActual = enrollment.getActualCount();
        Integer enrollmentPlan = enrollment.getPlanCount();
        enrollment.setReportRate(rate(
                enrollmentActual == null ? null : enrollmentActual.longValue(),
                enrollmentPlan == null ? null : enrollmentPlan.longValue()));
    }

    /** 报到率(%)，保留两位小数；计划数为 0 时返回 null */
    private BigDecimal rate(Long actual, Long plan) {
        if (actual == null || plan == null || plan == 0) {
            return null;
        }
        return BigDecimal.valueOf(actual * 100L)
                .divide(BigDecimal.valueOf(plan), 2, RoundingMode.HALF_UP);
    }
}
