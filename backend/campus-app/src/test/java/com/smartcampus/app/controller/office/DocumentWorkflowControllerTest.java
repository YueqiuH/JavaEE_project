package com.smartcampus.app.controller.office;

import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentWorkflowService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.DocumentWorkflowStep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentWorkflowControllerTest {

    @Mock private IDocumentApproverService approverService;
    @Mock private IDocumentWorkflowService workflowService;
    @InjectMocks private DocumentWorkflowController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void adminPublishesOrderedFixedApprovers() {
        CurrentUserContext.set(adminSession());
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(approverService.isAvailable(3L)).thenReturn(true);
        when(workflowService.createVersion(eq("请示报告"), eq("两级审批"), anyList(), eq(99L)))
                .thenReturn(new DocumentWorkflow());

        controller.saveWorkflow("请示报告", workflowRequest(2L, 3L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentWorkflowStep>> captor = ArgumentCaptor.forClass(List.class);
        verify(workflowService).createVersion(eq("请示报告"), eq("两级审批"), captor.capture(), eq(99L));
        assertEquals(List.of(2L, 3L), captor.getValue().stream().map(DocumentWorkflowStep::getApproverId).toList());
    }

    @Test
    void workflowRejectsUserWithoutNonStudentApprovalQualification() {
        CurrentUserContext.set(adminSession());
        when(approverService.isAvailable(2L)).thenReturn(true);
        when(approverService.isAvailable(3L)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.saveWorkflow("请示报告", workflowRequest(2L, 3L)));

        assertEquals(OfficeErrorCodeConstants.BAD_REQUEST.getCode(), exception.getErrorCode().getCode());
    }

    private AuthSession adminSession() {
        return new AuthSession(99L, "admin", 4, Set.of("ADMIN"),
                Set.of("document:self", "document:manage"), 1L);
    }

    private DocumentWorkflowController.WorkflowRequest workflowRequest(Long... approverIds) {
        DocumentWorkflowController.WorkflowRequest request = new DocumentWorkflowController.WorkflowRequest();
        request.setWorkflowName("两级审批");
        request.setSteps(java.util.stream.IntStream.range(0, approverIds.length).mapToObj(index -> {
            DocumentWorkflowController.WorkflowStepRequest step = new DocumentWorkflowController.WorkflowStepRequest();
            step.setStepName("第" + (index + 1) + "步审批");
            step.setApproverId(approverIds[index]);
            return step;
        }).toList());
        return request;
    }
}
