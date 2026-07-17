package com.smartcampus.app.controller.office;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import com.smartcampus.contract.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/document")
@Tag(name = "官方公文流转系统OA")
public class DocumentController {
    @Autowired private IDocumentService documentService;
    @Autowired private IDocumentApprovalService approvalService;
    @Autowired private INotificationService notificationService;

    @PostMapping("/start")
    @RequirePermission(OfficePermissions.DOCUMENT_SELF)
    @Transactional
    @Operation(summary = "发起公文并设置逐级审批链")
    public CommonResult<Document> start(@RequestBody Document document) {
        if (document.getTitle() == null || document.getTitle().isBlank()
                || document.getContent() == null || document.getContent().isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "标题和正文不能为空");
        }
        List<Long> chain = parseChain(document.getApprovalChain()).stream().distinct().toList();
        if (chain.isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批链不能为空，格式应为用户ID数组");
        }
        document.setDocId(null);
        document.setInitiatorId(CurrentUserContext.require().userId());
        document.setApprovalChain(JSON.toJSONString(chain));
        document.setCurrentApproverId(chain.get(0));
        document.setStatus(0);
        document.setCreateTime(new Date());
        documentService.save(document);
        notifyUser(chain.get(0), "待审批公文", "《" + document.getTitle() + "》等待您的审批", "公文通知");
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
    @Operation(summary = "审批公文，支持同意拒绝和退回")
    public CommonResult<Document> approve(@PathVariable Long docId, @RequestBody ApprovalRequest request) {
        Document document = requireDocument(docId);
        if (!Integer.valueOf(0).equals(document.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该公文已结束审批");
        }
        Long approverId = CurrentUserContext.require().userId();
        if (!approverId.equals(document.getCurrentApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "您不是当前审批人");
        }
        if (request == null || !List.of("同意", "拒绝", "退回").contains(request.getAction())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批操作必须为同意、拒绝或退回");
        }

        DocumentApproval approval = new DocumentApproval();
        approval.setDocId(docId);
        approval.setApproverId(approverId);
        approval.setAction(request.getAction());
        approval.setOpinion(request.getOpinion());
        approval.setApprovalTime(new Date());
        approvalService.save(approval);

        if ("拒绝".equals(request.getAction())) {
            document.setStatus(2);
            document.setCurrentApproverId(null);
        } else if ("退回".equals(request.getAction())) {
            document.setStatus(3);
            document.setCurrentApproverId(null);
        } else {
            List<Long> chain = parseChain(document.getApprovalChain());
            int currentIndex = chain.indexOf(approverId);
            if (currentIndex >= 0 && currentIndex + 1 < chain.size()) {
                Long next = chain.get(currentIndex + 1);
                document.setCurrentApproverId(next);
                notifyUser(next, "待审批公文", "《" + document.getTitle() + "》已流转至您审批", "公文通知");
            } else {
                document.setStatus(1);
                document.setCurrentApproverId(null);
            }
        }
        documentService.update(new LambdaUpdateWrapper<Document>()
                .eq(Document::getDocId, document.getDocId())
                .set(Document::getStatus, document.getStatus())
                .set(Document::getCurrentApproverId, document.getCurrentApproverId()));
        notifyUser(document.getInitiatorId(), "公文审批进度", "《" + document.getTitle() + "》审批操作：" + request.getAction(), "公文通知");
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
        if (document.getCurrentApproverId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "当前没有可催办的审批人");
        }
        notifyUser(document.getCurrentApproverId(), "公文催办", "请尽快审批《" + document.getTitle() + "》", "公文通知");
        return CommonResult.success();
    }

    private List<Long> parseChain(String json) {
        try { return json == null ? List.of() : JSON.parseArray(json, Long.class); }
        catch (RuntimeException exception) { return List.of(); }
    }

    private Document requireDocument(Long docId) {
        Document document = documentService.getById(docId);
        if (document == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "公文不存在");
        return document;
    }

    private void requireDocumentAccess(Document document, Long userId) {
        boolean involved = userId.equals(document.getInitiatorId()) || userId.equals(document.getCurrentApproverId())
                || approvalService.count(new LambdaQueryWrapper<DocumentApproval>()
                .eq(DocumentApproval::getDocId, document.getDocId())
                .eq(DocumentApproval::getApproverId, userId)) > 0;
        if (!involved) throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "无权查看该公文");
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
