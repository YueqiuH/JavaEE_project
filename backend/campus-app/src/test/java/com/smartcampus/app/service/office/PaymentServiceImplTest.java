package com.smartcampus.app.service.office;

import com.smartcampus.app.dao.office.PaymentMapper;
import com.smartcampus.app.service.office.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentMapper paymentMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setBaseMapper() {
        ReflectionTestUtils.setField(paymentService, "baseMapper", paymentMapper);
    }

    @Test
    void returnsZeroWhenStudentHasNoCardPayments() {
        when(paymentMapper.selectCardBalance(1L)).thenReturn(null);

        BigDecimal balance = paymentService.getCardBalance(1L);

        assertEquals(new BigDecimal("0.00"), balance);
        verify(paymentMapper).selectCardBalance(1L);
    }

    @Test
    void keepsTwoDecimalPlacesForCalculatedBalance() {
        when(paymentMapper.selectCardBalance(1L)).thenReturn(new BigDecimal("88.50"));

        BigDecimal balance = paymentService.getCardBalance(1L);

        assertEquals(new BigDecimal("88.50"), balance);
    }
}
