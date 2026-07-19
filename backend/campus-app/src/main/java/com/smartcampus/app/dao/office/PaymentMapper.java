package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.Payment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("""
            SELECT COALESCE(SUM(CASE
                WHEN payment_type IN ('一卡通充值', '勤工俭学工资') THEN amount
                WHEN payment_type = '消费' THEN -amount
                ELSE 0
            END), 0.00)
            FROM payment
            WHERE student_id = #{studentId}
              AND payment_type IN ('一卡通充值', '勤工俭学工资', '消费')
            """)
    BigDecimal selectCardBalance(@Param("studentId") Long studentId);
}
