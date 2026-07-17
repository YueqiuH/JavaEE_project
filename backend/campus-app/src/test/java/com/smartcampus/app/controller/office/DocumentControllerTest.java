package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private IDocumentApproverService approverService;
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

    @Test
    void startGeneratesSingleStepChainForDesignatedApprover() {
        CurrentUserContext.set(new AuthSession(1L, "initiator", 3, Set.of("STAFF"), Set.of("document:self"), 1L));
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(documentService.save(any(Document.class))).thenReturn(true);

        DocumentController.StartRequest request = request("请示报告", 2L);
        Document document = controller.start(request).getData();

        assertEquals(1L, document.getInitiatorId());
        assertEquals(2L, document.getCurrentApproverId());
        assertEquals("[2]", document.getApprovalChain());
        assertEquals(0, document.getStatus());
    }

    @Test
    void startRejectsSelfApproval() {
        CurrentUserContext.set(new AuthSession(2L, "initiator", 2, Set.of("TEACHER"), Set.of("document:self"), 1L));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.start(request("请假申请", 2L)));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    @Test
    void returnedDocumentCanBeEditedAndResubmittedByInitiator() {
        CurrentUserContext.set(new AuthSession(1L, "initiator", 3, Set.of("STAFF"), Set.of("document:self"), 1L));
        Document document = new Document();
        document.setDocId(9L);
        document.setTitle("退回前标题");
        document.setInitiatorId(1L);
        document.setStatus(3);
        when(documentService.getById(9L)).thenReturn(document);
        when(approverService.isAvailable(3L)).thenReturn(true);
        when(documentService.updateById(any(Document.class))).thenReturn(true);

        Document updated = controller.resubmit(9L, request("公文会签", 3L)).getData();

        assertEquals("测试公文", updated.getTitle());
        assertEquals("公文会签", updated.getDocType());
        assertEquals(3L, updated.getCurrentApproverId());
        assertEquals("[3]", updated.getApprovalChain());
        assertEquals(0, updated.getStatus());
    }

    @Test
    void approverCannotApproveOwnDocument() {
        CurrentUserContext.set(new AuthSession(2L, "self", 2, Set.of("TEACHER"), Set.of("document:approve"), 1L));
        Document document = new Document();
        document.setDocId(11L);
        document.setTitle("自审测试");
        document.setInitiatorId(2L);
        document.setCurrentApproverId(2L);
        document.setStatus(0);
        when(documentService.getById(11L)).thenReturn(document);
        DocumentController.ApprovalRequest approval = new DocumentController.ApprovalRequest();
        approval.setAction("同意");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.approve(11L, approval));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    private DocumentController.StartRequest request(String type, Long approverId) {
        DocumentController.StartRequest request = new DocumentController.StartRequest();
        request.setTitle("测试公文");
        request.setDocType(type);
        request.setContent("测试正文");
        request.setApproverId(approverId);
        return request;
    }
}
