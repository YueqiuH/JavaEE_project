package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.PaymentMapper;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.contract.entity.Payment;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {
}
