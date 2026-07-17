package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import com.smartcampus.contract.entity.DocumentApprover;
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

@RestController
@RequestMapping("/api/v1/office/document")
@Tag(name = "官方公文流转系统OA")
public class DocumentController {

    private static final List<String> SUPPORTED_DOCUMENT_TYPES =
            List.of("公文会签", "请示报告", "请假申请");

    @Autowired private IDocumentService documentService;
    @Autowired private IDocumentApprovalService approvalService;
    @Autowired private IDocumentApproverService approverService;
    @Autowired private INotificationService notificationService;

    @GetMapping("/approvers")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Operation(summary = "查询两名指定公文审批人")
    public CommonResult<List<DocumentApprover>> approvers() {
        return CommonResult.success(approverService.listAvailable());
    }

    @PostMapping("/start")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Transactional
    @Operation(summary = "发起单步公文审批")
    public CommonResult<Document> start(@RequestBody StartRequest request) {
        Long initiatorId = CurrentUserContext.require().userId();
        validateSubmission(request, initiatorId);

        Document document = new Document();
        applySubmission(document, request);
        document.setInitiatorId(initiatorId);
        document.setStatus(0);
        document.setCreateTime(new Date());
        documentService.save(document);
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
        validateSubmission(request, initiatorId);

        applySubmission(document, request);
        document.setStatus(0);
        documentService.updateById(document);
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

    @PostMapping("/{docId}/approve")
    @RequirePermission(OfficePermissions.DOCUMENT_APPROVE)
    @Transactional
    @Operation(summary = "完成单步审批，支持同意、拒绝和退回发起人")
    public CommonResult<Document> approve(@PathVariable Long docId, @RequestBody ApprovalRequest request) {
        Document document = requireDocument(docId);
        if (!Integer.valueOf(0).equals(document.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该公文当前不在审批中");
        }
        Long approverId = CurrentUserContext.require().userId();
        if (!approverId.equals(document.getCurrentApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "您不是当前审批人");
        }
        if (approverId.equals(document.getInitiatorId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "审批人不能审批自己发起的公文");
        }
        if (request == null || !List.of("同意", "拒绝", "退回").contains(request.getAction())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批操作必须为同意、拒绝或退回");
        }

        int targetStatus = switch (request.getAction()) {
            case "同意" -> 1;
            case "拒绝" -> 2;
            default -> 3;
        };
        boolean updated = documentService.update(new LambdaUpdateWrapper<Document>()
                .eq(Document::getDocId, docId)
                .eq(Document::getStatus, 0)
                .eq(Document::getCurrentApproverId, approverId)
                .set(Document::getStatus, targetStatus)
                .set(Document::getCurrentApproverId, null));
        if (!updated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "公文状态已变化，请刷新后重试");
        }

        DocumentApproval approval = new DocumentApproval();
        approval.setDocId(docId);
        approval.setApproverId(approverId);
        approval.setAction(request.getAction());
        approval.setOpinion(request.getOpinion());
        approval.setApprovalTime(new Date());
        approvalService.save(approval);

        document.setStatus(targetStatus);
        document.setCurrentApproverId(null);
        String progress = targetStatus == 3 ? "已退回，请修改后重新提交" : "审批操作：" + request.getAction();
        notifyUser(document.getInitiatorId(), "公文审批进度", "《" + document.getTitle() + "》" + progress, "公文通知");
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

    private void validateSubmission(StartRequest request, Long initiatorId) {
        if (request == null || request.getTitle() == null || request.getTitle().isBlank()
                || request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "标题和正文不能为空");
        }
        if (!SUPPORTED_DOCUMENT_TYPES.contains(request.getDocType())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "公文类型必须为公文会签、请示报告或请假申请");
        }
        if (request.getApproverId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "请选择审批人");
        }
        if (initiatorId.equals(request.getApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能选择自己审批公文");
        }
        if (!approverService.isAvailable(request.getApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "所选用户不是当前指定审批人");
        }
    }

    private void applySubmission(Document document, StartRequest request) {
        document.setTitle(request.getTitle().trim());
        document.setDocType(request.getDocType());
        document.setContent(request.getContent().trim());
        document.setCurrentApproverId(request.getApproverId());
        document.setApprovalChain("[" + request.getApproverId() + "]");
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
        private Long approverId;

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

        public Long getApproverId() {
            return approverId;
        }

        public void setApproverId(Long approverId) {
            this.approverId = approverId;
        }
    }

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
