package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.dao.office.DocumentApproverMapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.service.office.impl.DocumentApproverServiceImpl;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.DocumentApprover;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentApproverServiceImplTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                DocumentApprover.class);
    }

    @Mock private DocumentApproverMapper approverMapper;
    @Mock private IDocumentApprovalTaskService taskService;
    @InjectMocks private DocumentApproverServiceImpl approverService;

    @BeforeEach
    void setBaseMapper() {
        ReflectionTestUtils.setField(approverService, "baseMapper", approverMapper);
    }

    @Test
    void rejectsStudentApprovalQualification() {
        when(approverMapper.countEligibleUser(1L)).thenReturn(0);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> approverService.updateQualification(1L, "学生", true, 99L));

        assertEquals(OfficeErrorCodeConstants.BAD_REQUEST.getCode(), exception.getErrorCode().getCode());
    }

    @Test
    void blocksRevocationWhileApproverHasUnfinishedTask() {
        DocumentApprover config = new DocumentApprover();
        config.setApproverConfigId(5L);
        config.setUserId(2L);
        config.setStatus(1);
        when(approverMapper.countEligibleUser(2L)).thenReturn(1);
        when(approverMapper.selectOne(any(Wrapper.class))).thenReturn(config);
        when(approverMapper.countActiveWorkflowReferences(2L)).thenReturn(0);
        when(taskService.countOpenByApprover(2L)).thenReturn(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> approverService.updateQualification(2L, "负责人", false, 99L));

        assertEquals(OfficeErrorCodeConstants.STATE_CONFLICT.getCode(), exception.getErrorCode().getCode());
    }
}
