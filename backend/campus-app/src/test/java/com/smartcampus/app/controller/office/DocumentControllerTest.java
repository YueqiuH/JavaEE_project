package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Document.class);
    }

    @Mock
    private IDocumentService documentService;
    @Mock
    private IDocumentApprovalService approvalService;
    @Mock
    private INotificationService notificationService;
    @InjectMocks
    private DocumentController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void approvalUsesAuthenticatedUserAsApprover() {
        CurrentUserContext.set(new AuthSession(2L, "approver", 3, Set.of("STAFF"), Set.of("document:approve"), 1L));
        Document document = new Document();
        document.setDocId(7L);
        document.setTitle("测试公文");
        document.setInitiatorId(1L);
        document.setCurrentApproverId(2L);
        document.setApprovalChain("[2]");
        document.setStatus(0);
        when(documentService.getById(7L)).thenReturn(document);
        when(approvalService.save(any(DocumentApproval.class))).thenReturn(true);
        when(documentService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        DocumentController.ApprovalRequest request = new DocumentController.ApprovalRequest();
        request.setAction("同意");
        request.setOpinion("同意办理");
        controller.approve(7L, request);

        ArgumentCaptor<DocumentApproval> captor = ArgumentCaptor.forClass(DocumentApproval.class);
        verify(approvalService).save(captor.capture());
        assertEquals(2L, captor.getValue().getApproverId());
        assertEquals(1, document.getStatus());
    }
}
