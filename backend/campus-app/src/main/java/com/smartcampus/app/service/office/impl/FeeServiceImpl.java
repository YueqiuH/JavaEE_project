package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.FeeMapper;
import com.smartcampus.app.service.office.IFeeService;
import com.smartcampus.contract.entity.Fee;
import org.springframework.stereotype.Service;

@Service
public class FeeServiceImpl extends ServiceImpl<FeeMapper, Fee> implements IFeeService {
}
