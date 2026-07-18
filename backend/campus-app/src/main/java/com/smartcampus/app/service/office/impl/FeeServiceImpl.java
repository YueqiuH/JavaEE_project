package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.office.FeeMapper;
import com.smartcampus.app.service.office.IFeeService;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.vo.StudentFeeOverviewVo;
import org.springframework.stereotype.Service;

@Service
public class FeeServiceImpl extends ServiceImpl<FeeMapper, Fee> implements IFeeService {

    @Override
    public IPage<StudentFeeOverviewVo> getStudentFeeOverview(long page, long size, String keyword,
                                                              String paymentStatus) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedStatus = paymentStatus == null || paymentStatus.isBlank() ? null : paymentStatus;
        return baseMapper.selectStudentFeeOverview(new Page<>(page, size), normalizedKeyword, normalizedStatus);
    }
}
