package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentApprovalTaskService;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.IDocumentWorkflowService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import com.smartcampus.contract.entity.DocumentApprovalTask;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.DocumentWorkflowStep;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Document.class);
        TableInfoHelper.initTableInfo(assistant, DocumentApprovalTask.class);
        TableInfoHelper.initTableInfo(assistant, DocumentApproval.class);
    }

    @Mock private IDocumentService documentService;
    @Mock private IDocumentApprovalService approvalService;
    @Mock private IDocumentApprovalTaskService taskService;
    @Mock private IDocumentApproverService approverService;
    @Mock private IDocumentWorkflowService workflowService;
    @Mock private INotificationService notificationService;
    @InjectMocks private DocumentController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void startUsesAdminConfiguredFixedWorkflow() {
        CurrentUserContext.set(session(1L, "STAFF", 3));
        when(workflowService.getActive("请示报告")).thenReturn(workflow(10L, 2L, 3L));
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(approverService.isAvailable(3L)).thenReturn(true);
        when(documentService.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setDocId(7L);
            return true;
        });
        when(taskService.save(any(DocumentApprovalTask.class))).thenReturn(true);

        Document document = controller.start(request("请示报告")).getData();

        assertEquals(2L, document.getCurrentApproverId());
        assertEquals(1, document.getCurrentStep());
        assertEquals(1, document.getApprovalRound());
        assertEquals(10L, document.getWorkflowId());
        assertEquals("[2,3]", document.getApprovalChain());
        verify(taskService, atLeastOnce()).save(any(DocumentApprovalTask.class));
    }

    @Test
    void startAllowsWorkflowContainingInitiator() {
        CurrentUserContext.set(session(2L, "TEACHER", 2));
        when(workflowService.getActive("请假申请")).thenReturn(workflow(11L, 2L));
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(documentService.save(any(Document.class))).thenAnswer(invocation -> {
            Document document = invocation.getArgument(0);
            document.setDocId(12L);
            return true;
        });
        when(taskService.save(any(DocumentApprovalTask.class))).thenReturn(true);

        Document document = controller.start(request("请假申请")).getData();

        assertEquals(2L, document.getInitiatorId());
        assertEquals(2L, document.getCurrentApproverId());
        assertEquals(0, document.getStatus());
    }

    @Test
    void approvalAdvancesToNextFixedStep() {
        CurrentUserContext.set(session(2L, "STAFF", 3));
        Document document = pendingDocument(7L, 1L, 2L, 1);
        DocumentApprovalTask current = task(21L, 7L, 1, 1, 2L, 1);
        DocumentApprovalTask next = task(22L, 7L, 1, 2, 3L, 0);
        when(documentService.getById(7L)).thenReturn(document);
        when(taskService.getOne(any(Wrapper.class))).thenReturn(current, next);
        when(taskService.update(any(Wrapper.class))).thenReturn(true);
        when(documentService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);
        when(approvalService.save(any(DocumentApproval.class))).thenReturn(true);

        controller.approve(7L, approval("同意"));

        assertEquals(0, document.getStatus());
        assertEquals(2, document.getCurrentStep());
        assertEquals(3L, document.getCurrentApproverId());
        ArgumentCaptor<DocumentApproval> captor = ArgumentCaptor.forClass(DocumentApproval.class);
        verify(approvalService).save(captor.capture());
        assertEquals(21L, captor.getValue().getTaskId());
        assertEquals(1, captor.getValue().getStepOrder());
    }

    @Test
    void lastApprovalCompletesDocument() {
        CurrentUserContext.set(session(3L, "STAFF", 3));
        Document document = pendingDocument(8L, 1L, 3L, 2);
        DocumentApprovalTask current = task(23L, 8L, 1, 2, 3L, 1);
        when(documentService.getById(8L)).thenReturn(document);
        when(taskService.getOne(any(Wrapper.class))).thenReturn(current, (DocumentApprovalTask) null);
        when(taskService.update(any(Wrapper.class))).thenReturn(true);
        when(documentService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);
        when(approvalService.save(any(DocumentApproval.class))).thenReturn(true);

        controller.approve(8L, approval("同意"));

        assertEquals(1, document.getStatus());
        assertEquals(null, document.getCurrentApproverId());
    }

    @Test
    void returnedDocumentRestartsOriginalSnapshotAsNewRound() {
        CurrentUserContext.set(session(1L, "STAFF", 3));
        Document document = pendingDocument(9L, 1L, null, 2);
        document.setStatus(3);
        document.setApprovalRound(1);
        List<DocumentApprovalTask> oldTasks = List.of(
                task(31L, 9L, 1, 1, 2L, 2),
                task(32L, 9L, 1, 2, 3L, 4));
        when(documentService.getById(9L)).thenReturn(document);
        when(taskService.list(any(Wrapper.class))).thenReturn(oldTasks);
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(approverService.isAvailable(3L)).thenReturn(true);
        when(documentService.updateById(any(Document.class))).thenReturn(true);
        when(taskService.save(any(DocumentApprovalTask.class))).thenReturn(true);

        Document updated = controller.resubmit(9L, request("请示报告")).getData();

        assertEquals(2, updated.getApprovalRound());
        assertEquals(1, updated.getCurrentStep());
        assertEquals(2L, updated.getCurrentApproverId());
        assertEquals("[2,3]", updated.getApprovalChain());
    }

    @Test
    void currentApproverCanApproveOwnDocument() {
        CurrentUserContext.set(session(2L, "TEACHER", 2));
        Document document = pendingDocument(11L, 2L, 2L, 1);
        DocumentApprovalTask current = task(24L, 11L, 1, 1, 2L, 1);
        when(documentService.getById(11L)).thenReturn(document);
        when(taskService.getOne(any(Wrapper.class))).thenReturn(current, (DocumentApprovalTask) null);
        when(taskService.update(any(Wrapper.class))).thenReturn(true);
        when(documentService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);
        when(approvalService.save(any(DocumentApproval.class))).thenReturn(true);

        controller.approve(11L, approval("同意"));

        assertEquals(1, document.getStatus());
        assertEquals(null, document.getCurrentApproverId());
    }

    private AuthSession session(Long userId, String role, int userType) {
        return new AuthSession(userId, "test", userType, Set.of(role),
                Set.of("document:self", "document:approve"), 1L);
    }

    private DocumentController.StartRequest request(String type) {
        DocumentController.StartRequest request = new DocumentController.StartRequest();
        request.setTitle("测试公文");
        request.setDocType(type);
        request.setContent("测试正文");
        return request;
    }

    private DocumentController.ApprovalRequest approval(String action) {
        DocumentController.ApprovalRequest request = new DocumentController.ApprovalRequest();
        request.setAction(action);
        request.setOpinion(action + "办理");
        return request;
    }

    private DocumentWorkflow workflow(Long workflowId, Long... approvers) {
        DocumentWorkflow workflow = new DocumentWorkflow();
        workflow.setWorkflowId(workflowId);
        workflow.setSteps(java.util.stream.IntStream.range(0, approvers.length).mapToObj(index -> {
            DocumentWorkflowStep step = new DocumentWorkflowStep();
            step.setStepOrder(index + 1);
            step.setStepName("第" + (index + 1) + "步审批");
            step.setApproverId(approvers[index]);
            return step;
        }).toList());
        return workflow;
    }

    private Document pendingDocument(Long docId, Long initiatorId, Long approverId, int step) {
        Document document = new Document();
        document.setDocId(docId);
        document.setTitle("测试公文");
        document.setDocType("请示报告");
        document.setInitiatorId(initiatorId);
        document.setCurrentApproverId(approverId);
        document.setCurrentStep(step);
        document.setApprovalRound(1);
        document.setStatus(0);
        return document;
    }

    private DocumentApprovalTask task(Long taskId, Long docId, int round, int step,
                                      Long approverId, int status) {
        DocumentApprovalTask task = new DocumentApprovalTask();
        task.setTaskId(taskId);
        task.setDocId(docId);
        task.setRoundNo(round);
        task.setStepOrder(step);
        task.setStepName("第" + step + "步审批");
        task.setApproverId(approverId);
        task.setStatus(status);
        return task;
    }
}
