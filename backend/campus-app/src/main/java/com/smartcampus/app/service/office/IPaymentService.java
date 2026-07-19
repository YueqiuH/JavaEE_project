package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.Payment;

import java.math.BigDecimal;

public interface IPaymentService extends IService<Payment> {

    BigDecimal getCardBalance(Long studentId);
}
