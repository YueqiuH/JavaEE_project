package com.smartcampus.app.controller.office;

import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentWorkflowService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.DocumentApprover;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.DocumentWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/office/document")
@Tag(name = "公文审批资格与流程配置")
public class DocumentWorkflowController {

    private static final List<String> SUPPORTED_DOCUMENT_TYPES =
            List.of("公文会签", "请示报告", "请假申请");

    @Autowired
    private IDocumentApproverService approverService;
    @Autowired
    private IDocumentWorkflowService workflowService;

    @GetMapping("/workflows")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询当前启用的固定审批流程")
    public CommonResult<List<DocumentWorkflow>> workflows() {
        return CommonResult.success(workflowService.listActive());
    }

    @GetMapping("/approver-candidates")
    @RequirePermission(OfficePermissions.DOCUMENT_MANAGE)
    @Operation(summary = "管理员查询非学生审批资格候选人")
    public CommonResult<List<DocumentApprover>> approverCandidates() {
        return CommonResult.success(approverService.listCandidates());
    }

    @PutMapping("/approvers/{userId}")
    @RequirePermission(OfficePermissions.DOCUMENT_MANAGE)
    @Operation(summary = "管理员启用或停用用户的公文审批资格")
    public CommonResult<DocumentApprover> updateApprover(@PathVariable Long userId,
                                                         @RequestBody QualificationRequest request) {
        if (request == null || request.getEnabled() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "请明确审批资格状态");
        }
        return CommonResult.success(approverService.updateQualification(userId,
                request.getDisplayName(), request.getEnabled(), CurrentUserContext.require().userId()));
    }

    @PutMapping("/workflows/{docType}")
    @RequirePermission(OfficePermissions.DOCUMENT_MANAGE)
    @Transactional
    @Operation(summary = "管理员为公文类型发布新的固定审批流程版本")
    public CommonResult<DocumentWorkflow> saveWorkflow(@PathVariable String docType,
                                                       @RequestBody WorkflowRequest request) {
        validateWorkflow(docType, request);
        List<DocumentWorkflowStep> steps = request.getSteps().stream().map(item -> {
            DocumentWorkflowStep step = new DocumentWorkflowStep();
            step.setStepName(item.getStepName());
            step.setApproverId(item.getApproverId());
            return step;
        }).toList();
        return CommonResult.success(workflowService.createVersion(docType,
                request.getWorkflowName().trim(), steps, CurrentUserContext.require().userId()));
    }

    private void validateWorkflow(String docType, WorkflowRequest request) {
        if (!SUPPORTED_DOCUMENT_TYPES.contains(docType)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "不支持的公文类型");
        }
        if (request == null || request.getWorkflowName() == null
                || request.getWorkflowName().isBlank() || request.getWorkflowName().length() > 64) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "流程名称不能为空且不能超过64个字符");
        }
        if (request.getSteps() == null || request.getSteps().isEmpty() || request.getSteps().size() > 10) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批流程必须包含1至10个步骤");
        }
        for (WorkflowStepRequest step : request.getSteps()) {
            if (step == null || step.getStepName() == null || step.getStepName().isBlank()
                    || step.getStepName().length() > 64 || step.getApproverId() == null) {
                throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "每个步骤都必须填写名称并指定审批人");
            }
            if (!approverService.isAvailable(step.getApproverId())) {
                throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST,
                        "流程审批人必须具有已启用的审批资格且不能是学生");
            }
        }
    }

    public static class QualificationRequest {
        private String displayName;
        private Boolean enabled;

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }

    public static class WorkflowRequest {
        private String workflowName;
        private List<WorkflowStepRequest> steps;

        public String getWorkflowName() { return workflowName; }
        public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
        public List<WorkflowStepRequest> getSteps() { return steps; }
        public void setSteps(List<WorkflowStepRequest> steps) { this.steps = steps; }
    }

    public static class WorkflowStepRequest {
        private String stepName;
        private Long approverId;

        public String getStepName() { return stepName; }
        public void setStepName(String stepName) { this.stepName = stepName; }
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
    }
}
