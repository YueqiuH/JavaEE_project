package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.vo.StudentFeeOverviewVo;

public interface IFeeService extends IService<Fee> {

    IPage<StudentFeeOverviewVo> getStudentFeeOverview(long page, long size, String keyword, String paymentStatus);
}
