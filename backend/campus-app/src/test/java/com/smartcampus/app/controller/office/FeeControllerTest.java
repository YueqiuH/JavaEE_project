package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.service.office.IFeeService;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.entity.Payment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeControllerTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Fee.class);
    }

    @Mock
    private IFeeService feeService;
    @Mock
    private IPaymentService paymentService;
    @InjectMocks
    private FeeController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void rejectsPayingAnotherUsersBill() {
        CurrentUserContext.set(session(1L));
        Fee fee = fee(9L, 2L);
        when(feeService.getById(9L)).thenReturn(fee);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.pay(9L));

        assertEquals(OfficeErrorCodeConstants.FORBIDDEN.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    void createsPaymentForCurrentUsersBill() {
        CurrentUserContext.set(session(1L));
        Fee fee = fee(9L, 1L);
        when(feeService.getById(9L)).thenReturn(fee);
        when(feeService.update(any(Wrapper.class))).thenReturn(true);
        when(paymentService.save(any(Payment.class))).thenReturn(true);

        CommonResult<Payment> result = controller.pay(9L);

        assertEquals(1L, result.getData().getStudentId());
        assertEquals(new BigDecimal("100.00"), result.getData().getAmount());
        verify(paymentService).save(any(Payment.class));
    }

    private AuthSession session(Long userId) {
        return new AuthSession(userId, "test", 1, Set.of("STUDENT"), Set.of("fee:self:pay"), 1L);
    }

    private Fee fee(Long feeId, Long studentId) {
        Fee fee = new Fee();
        fee.setFeeId(feeId);
        fee.setStudentId(studentId);
        fee.setFeeType("学费");
        fee.setSemester("2026-2027-1");
        fee.setAmount(new BigDecimal("100.00"));
        fee.setStatus(0);
        return fee;
    }
}
