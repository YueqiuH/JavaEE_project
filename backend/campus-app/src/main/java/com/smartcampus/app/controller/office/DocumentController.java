package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentApprovalTaskService;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.IDocumentWorkflowService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import com.smartcampus.contract.entity.DocumentApprovalTask;
import com.smartcampus.contract.entity.DocumentApprover;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/office/document")
@Tag(name = "官方公文流转系统OA")
public class DocumentController {

    private static final List<String> SUPPORTED_DOCUMENT_TYPES =
            List.of("公文会签", "请示报告", "请假申请");

    @Autowired private IDocumentService documentService;
    @Autowired private IDocumentApprovalService approvalService;
    @Autowired private IDocumentApprovalTaskService taskService;
    @Autowired private IDocumentApproverService approverService;
    @Autowired private IDocumentWorkflowService workflowService;
    @Autowired private INotificationService notificationService;

    @GetMapping("/approvers")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询管理员启用的公文审批资格名单")
    public CommonResult<List<DocumentApprover>> approvers() {
        return CommonResult.success(approverService.listAvailable());
    }

    @PostMapping("/start")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Transactional
    @Operation(summary = "按管理员固定流程发起公文审批")
    public CommonResult<Document> start(@RequestBody StartRequest request) {
        Long initiatorId = CurrentUserContext.require().userId();
        validateSubmission(request);
        DocumentWorkflow workflow = requireActiveWorkflow(request.getDocType());
        List<TaskSeed> taskSeeds = seedsFromWorkflow(workflow);
        validateTaskSeeds(taskSeeds);

        Document document = new Document();
        applySubmission(document, request);
        document.setInitiatorId(initiatorId);
        document.setStatus(0);
        document.setWorkflowId(workflow.getWorkflowId());
        document.setCurrentStep(1);
        document.setApprovalRound(1);
        document.setCurrentApproverId(taskSeeds.getFirst().approverId());
        document.setApprovalChain(toApprovalChain(taskSeeds));
        document.setCreateTime(new Date());
        documentService.save(document);
        createTaskRound(document, taskSeeds, 1);
        notifyApprover(document, "待审批公文");
        return CommonResult.success(document);
    }

    @PostMapping("/{docId}/resubmit")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Transactional
    @Operation(summary = "修改被退回的公文并重新提交")
    public CommonResult<Document> resubmit(@PathVariable Long docId, @RequestBody StartRequest request) {
        Document document = requireDocument(docId);
        Long initiatorId = CurrentUserContext.require().userId();
        if (!initiatorId.equals(document.getInitiatorId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只有发起人可以重新提交公文");
        }
        if (!Integer.valueOf(3).equals(document.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "只有已退回的公文可以重新提交");
        }
        validateSubmission(request);
        if (!document.getDocType().equals(request.getDocType())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "退回重提时不能更改公文类型");
        }

        int previousRound = document.getApprovalRound() == null ? 1 : document.getApprovalRound();
        List<DocumentApprovalTask> previousTasks = taskService.list(
                new LambdaQueryWrapper<DocumentApprovalTask>()
                        .eq(DocumentApprovalTask::getDocId, docId)
                        .eq(DocumentApprovalTask::getRoundNo, previousRound)
                        .orderByAsc(DocumentApprovalTask::getStepOrder));
        List<TaskSeed> taskSeeds;
        if (previousTasks.isEmpty()) {
            DocumentWorkflow workflow = requireActiveWorkflow(document.getDocType());
            document.setWorkflowId(workflow.getWorkflowId());
            taskSeeds = seedsFromWorkflow(workflow);
        } else {
            taskSeeds = previousTasks.stream()
                    .map(task -> new TaskSeed(task.getStepName(), task.getApproverId()))
                    .toList();
        }
        validateTaskSeeds(taskSeeds);

        applySubmission(document, request);
        document.setStatus(0);
        document.setCurrentStep(1);
        document.setApprovalRound(previousRound + 1);
        document.setCurrentApproverId(taskSeeds.getFirst().approverId());
        document.setApprovalChain(toApprovalChain(taskSeeds));
        documentService.updateById(document);
        createTaskRound(document, taskSeeds, previousRound + 1);
        notifyApprover(document, "重新提交的待审批公文");
        return CommonResult.success(document);
    }

    @GetMapping("/mine")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询我发起的公文")
    public CommonResult<List<Document>> initiated() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(documentService.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getInitiatorId, userId).orderByDesc(Document::getCreateTime)));
    }

    @GetMapping("/pending")
    @RequirePermission(OfficePermissions.DOCUMENT_APPROVE)
    @Operation(summary = "查询我的待审批公文")
    public CommonResult<List<Document>> pending() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(documentService.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getCurrentApproverId, userId).eq(Document::getStatus, 0)
                .orderByDesc(Document::getCreateTime)));
    }

    @GetMapping("/{docId}/history")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询公文审批历史")
    public CommonResult<List<DocumentApproval>> history(@PathVariable Long docId) {
        Document document = requireDocument(docId);
        requireDocumentAccess(document, CurrentUserContext.require().userId());
        return CommonResult.success(approvalService.list(new LambdaQueryWrapper<DocumentApproval>()
                .eq(DocumentApproval::getDocId, docId).orderByAsc(DocumentApproval::getApprovalTime)));
    }

    @GetMapping("/{docId}/tasks")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询公文固定审批流程快照")
    public CommonResult<List<DocumentApprovalTask>> tasks(@PathVariable Long docId) {
        Document document = requireDocument(docId);
        requireDocumentAccess(document, CurrentUserContext.require().userId());
        return CommonResult.success(taskService.list(new LambdaQueryWrapper<DocumentApprovalTask>()
                .eq(DocumentApprovalTask::getDocId, docId)
                .orderByAsc(DocumentApprovalTask::getRoundNo)
                .orderByAsc(DocumentApprovalTask::getStepOrder)));
    }

    @PostMapping("/{docId}/approve")
    @RequirePermission(OfficePermissions.DOCUMENT_APPROVE)
    @Transactional
    @Operation(summary = "处理当前固定审批步骤，支持同意、拒绝和退回")
    public CommonResult<Document> approve(@PathVariable Long docId, @RequestBody ApprovalRequest request) {
        Document document = requireDocument(docId);
        if (!Integer.valueOf(0).equals(document.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该公文当前不在审批中");
        }
        Long approverId = CurrentUserContext.require().userId();
        if (!approverId.equals(document.getCurrentApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "您不是当前审批人");
        }
        if (request == null || !List.of("同意", "拒绝", "退回").contains(request.getAction())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批操作必须为同意、拒绝或退回");
        }

        int roundNo = document.getApprovalRound() == null ? 1 : document.getApprovalRound();
        int stepOrder = document.getCurrentStep() == null ? 1 : document.getCurrentStep();
        DocumentApprovalTask currentTask = taskService.getOne(new LambdaQueryWrapper<DocumentApprovalTask>()
                .eq(DocumentApprovalTask::getDocId, docId)
                .eq(DocumentApprovalTask::getRoundNo, roundNo)
                .eq(DocumentApprovalTask::getStepOrder, stepOrder)
                .eq(DocumentApprovalTask::getApproverId, approverId)
                .eq(DocumentApprovalTask::getStatus, 1));
        if (currentTask == null) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "当前审批任务不存在或已处理");
        }

        int taskStatus = switch (request.getAction()) {
            case "同意" -> 2;
            case "拒绝" -> 3;
            default -> 4;
        };
        boolean taskUpdated = taskService.update(new LambdaUpdateWrapper<DocumentApprovalTask>()
                .eq(DocumentApprovalTask::getTaskId, currentTask.getTaskId())
                .eq(DocumentApprovalTask::getStatus, 1)
                .set(DocumentApprovalTask::getStatus, taskStatus)
                .set(DocumentApprovalTask::getHandledTime, new Date()));
        if (!taskUpdated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "审批任务已被处理，请刷新后重试");
        }

        DocumentApproval approval = new DocumentApproval();
        approval.setDocId(docId);
        approval.setTaskId(currentTask.getTaskId());
        approval.setApproverId(approverId);
        approval.setRoundNo(roundNo);
        approval.setStepOrder(stepOrder);
        approval.setStepName(currentTask.getStepName());
        approval.setAction(request.getAction());
        approval.setOpinion(request.getOpinion());
        approval.setApprovalTime(new Date());
        approvalService.save(approval);

        if ("同意".equals(request.getAction())) {
            advanceAfterApproval(document, currentTask);
        } else {
            int documentStatus = "拒绝".equals(request.getAction()) ? 2 : 3;
            finishDocument(document, approverId, stepOrder, documentStatus);
            taskService.update(new LambdaUpdateWrapper<DocumentApprovalTask>()
                    .eq(DocumentApprovalTask::getDocId, docId)
                    .eq(DocumentApprovalTask::getRoundNo, roundNo)
                    .gt(DocumentApprovalTask::getStepOrder, stepOrder)
                    .eq(DocumentApprovalTask::getStatus, 0)
                    .set(DocumentApprovalTask::getStatus, 5));
            String progress = documentStatus == 3 ? "已退回，请修改后重新提交" : "审批操作：拒绝";
            notifyUser(document.getInitiatorId(), "公文审批进度",
                    "《" + document.getTitle() + "》" + progress, "公文通知");
        }
        return CommonResult.success(document);
    }

    @PostMapping("/{docId}/remind")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "催办当前公文审批人")
    public CommonResult<Void> remind(@PathVariable Long docId) {
        Document document = requireDocument(docId);
        if (!CurrentUserContext.require().userId().equals(document.getInitiatorId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只有发起人可以催办");
        }
        if (!Integer.valueOf(0).equals(document.getStatus()) || document.getCurrentApproverId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "当前没有可催办的审批人");
        }
        notifyUser(document.getCurrentApproverId(), "公文催办", "请尽快审批《" + document.getTitle() + "》", "公文通知");
        return CommonResult.success();
    }

    private void validateSubmission(StartRequest request) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()
                || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "标题和正文不能为空");
        }
        if (!SUPPORTED_DOCUMENT_TYPES.contains(request.getDocType())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "公文类型必须为公文会签、请示报告或请假申请");
        }
    }

    private void applySubmission(Document document, StartRequest request) {
        document.setTitle(request.getTitle().trim());
        document.setDocType(request.getDocType());
        document.setContent(request.getContent().trim());
    }

    private DocumentWorkflow requireActiveWorkflow(String docType) {
        DocumentWorkflow workflow = workflowService.getActive(docType);
        if (workflow == null || workflow.getSteps() == null || workflow.getSteps().isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT,
                    "该公文类型尚未配置启用的审批流程，请联系管理员");
        }
        return workflow;
    }

    private List<TaskSeed> seedsFromWorkflow(DocumentWorkflow workflow) {
        return workflow.getSteps().stream()
                .map(step -> new TaskSeed(step.getStepName(), step.getApproverId()))
                .toList();
    }

    private void validateTaskSeeds(List<TaskSeed> taskSeeds) {
        for (TaskSeed seed : taskSeeds) {
            if (!approverService.isAvailable(seed.approverId())) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT,
                        "固定流程中的审批人已失效，请联系管理员调整流程");
            }
        }
    }

    private void createTaskRound(Document document, List<TaskSeed> taskSeeds, int roundNo) {
        for (int index = 0; index < taskSeeds.size(); index++) {
            TaskSeed seed = taskSeeds.get(index);
            DocumentApprovalTask task = new DocumentApprovalTask();
            task.setDocId(document.getDocId());
            task.setWorkflowId(document.getWorkflowId());
            task.setRoundNo(roundNo);
            task.setStepOrder(index + 1);
            task.setStepName(seed.stepName());
            task.setApproverId(seed.approverId());
            task.setStatus(index == 0 ? 1 : 0);
            task.setCreateTime(new Date());
            taskService.save(task);
        }
    }

    private String toApprovalChain(List<TaskSeed> taskSeeds) {
        return taskSeeds.stream().map(seed -> String.valueOf(seed.approverId()))
                .collect(Collectors.joining(",", "[", "]"));
    }

    private void advanceAfterApproval(Document document, DocumentApprovalTask currentTask) {
        int roundNo = currentTask.getRoundNo();
        int currentStep = currentTask.getStepOrder();
        DocumentApprovalTask nextTask = taskService.getOne(new LambdaQueryWrapper<DocumentApprovalTask>()
                .eq(DocumentApprovalTask::getDocId, document.getDocId())
                .eq(DocumentApprovalTask::getRoundNo, roundNo)
                .eq(DocumentApprovalTask::getStepOrder, currentStep + 1));
        if (nextTask == null) {
            finishDocument(document, currentTask.getApproverId(), currentStep, 1);
            notifyUser(document.getInitiatorId(), "公文审批完成",
                    "《" + document.getTitle() + "》已通过全部审批", "公文通知");
            return;
        }
        if (!Integer.valueOf(0).equals(nextTask.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "下一审批步骤状态异常");
        }

        boolean documentUpdated = documentService.update(new LambdaUpdateWrapper<Document>()
                .eq(Document::getDocId, document.getDocId())
                .eq(Document::getStatus, 0)
                .eq(Document::getCurrentApproverId, currentTask.getApproverId())
                .eq(Document::getCurrentStep, currentStep)
                .set(Document::getCurrentStep, nextTask.getStepOrder())
                .set(Document::getCurrentApproverId, nextTask.getApproverId()));
        boolean nextUpdated = taskService.update(new LambdaUpdateWrapper<DocumentApprovalTask>()
                .eq(DocumentApprovalTask::getTaskId, nextTask.getTaskId())
                .eq(DocumentApprovalTask::getStatus, 0)
                .set(DocumentApprovalTask::getStatus, 1));
        if (!documentUpdated || !nextUpdated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "公文状态已变化，请刷新后重试");
        }
        document.setCurrentStep(nextTask.getStepOrder());
        document.setCurrentApproverId(nextTask.getApproverId());
        notifyApprover(document, "待审批公文");
        notifyUser(document.getInitiatorId(), "公文审批进度",
                "《" + document.getTitle() + "》已完成第" + currentStep + "步审批", "公文通知");
    }

    private void finishDocument(Document document, Long approverId, int currentStep, int status) {
        boolean updated = documentService.update(new LambdaUpdateWrapper<Document>()
                .eq(Document::getDocId, document.getDocId())
                .eq(Document::getStatus, 0)
                .eq(Document::getCurrentApproverId, approverId)
                .eq(Document::getCurrentStep, currentStep)
                .set(Document::getStatus, status)
                .set(Document::getCurrentApproverId, null));
        if (!updated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "公文状态已变化，请刷新后重试");
        }
        document.setStatus(status);
        document.setCurrentApproverId(null);
    }

    private Document requireDocument(Long docId) {
        Document document = documentService.getById(docId);
        if (document == null) {
            throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "公文不存在");
        }
        return document;
    }

    private void requireDocumentAccess(Document document, Long userId) {
        boolean involved = userId.equals(document.getInitiatorId()) || userId.equals(document.getCurrentApproverId())
                || approvalService.count(new LambdaQueryWrapper<DocumentApproval>()
                .eq(DocumentApproval::getDocId, document.getDocId())
                .eq(DocumentApproval::getApproverId, userId)) > 0;
        if (!involved) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "无权查看该公文");
        }
    }

    private void notifyApprover(Document document, String title) {
        notifyUser(document.getCurrentApproverId(), title,
                "《" + document.getTitle() + "》等待您的审批", "公文通知");
    }

    private void notifyUser(Long userId, String title, String content, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setNotifyType(type);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationService.save(notification);
    }

    public static class StartRequest {
        private String title;
        private String docType;
        private String content;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDocType() {
            return docType;
        }

        public void setDocType(String docType) {
            this.docType = docType;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    private record TaskSeed(String stepName, Long approverId) { }

    public static class ApprovalRequest {
        private String action;
        private String opinion;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getOpinion() {
            return opinion;
        }

        public void setOpinion(String opinion) {
            this.opinion = opinion;
        }
    }
}
