package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.smartcampus.app.dao.office.FeeMapper;
import com.smartcampus.app.service.office.impl.FeeServiceImpl;
import com.smartcampus.contract.vo.StudentFeeOverviewVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceImplTest {

    @Mock
    private FeeMapper feeMapper;
    @InjectMocks
    private FeeServiceImpl feeService;

    @BeforeEach
    void setBaseMapper() {
        ReflectionTestUtils.setField(feeService, "baseMapper", feeMapper);
    }

    @Test
    void trimsKeywordAndDelegatesOverviewQuery() {
        Page<StudentFeeOverviewVo> resultPage = new Page<>(1, 20, 0);
        when(feeMapper.selectStudentFeeOverview(any(Page.class), any(), any())).thenReturn(resultPage);

        var result = feeService.getStudentFeeOverview(1, 20, " 600001 ", "欠费");

        assertEquals(resultPage, result);
        verify(feeMapper).selectStudentFeeOverview(any(Page.class),
                org.mockito.ArgumentMatchers.eq("600001"), org.mockito.ArgumentMatchers.eq("欠费"));
    }

    @Test
    void overviewSqlAnnotationCanBeParsed() {
        MybatisConfiguration configuration = new MybatisConfiguration();

        assertDoesNotThrow(() -> configuration.addMapper(FeeMapper.class));
    }
}
