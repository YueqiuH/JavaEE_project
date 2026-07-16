package com.smartcampus.app.controller.office;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Document;
import com.smartcampus.contract.entity.DocumentApproval;
import com.smartcampus.contract.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/office/document")
@Tag(name = "官方公文流转系统OA")
public class DocumentController {
    @Autowired private IDocumentService documentService;
    @Autowired private IDocumentApprovalService approvalService;
    @Autowired private INotificationService notificationService;

    @PostMapping("/start")
    @Transactional
    @Operation(summary = "发起公文并设置逐级审批链")
    public CommonResult<Document> start(@RequestBody Document document) {
        if (document.getTitle() == null || document.getContent() == null || document.getInitiatorId() == null)
            return CommonResult.error(1301, "标题、内容和发起人不能为空");
        List<Long> chain = parseChain(document.getApprovalChain());
        if (chain.isEmpty()) return CommonResult.error(1302, "审批链不能为空，格式应为用户ID数组");
        document.setApprovalChain(JSON.toJSONString(chain));
        document.setCurrentApproverId(chain.get(0));
        document.setStatus(0);
        document.setCreateTime(new Date());
        documentService.save(document);
        notifyUser(chain.get(0), "待审批公文", "《" + document.getTitle() + "》等待您的审批", "公文通知");
        return CommonResult.success(document);
    }

    @GetMapping("/initiator/{userId}")
    @Operation(summary = "查询我发起的公文")
    public CommonResult<List<Document>> initiated(@PathVariable Long userId) {
        return CommonResult.success(documentService.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getInitiatorId, userId).orderByDesc(Document::getCreateTime)));
    }

    @GetMapping("/pending/{userId}")
    @Operation(summary = "查询我的待审批公文")
    public CommonResult<List<Document>> pending(@PathVariable Long userId) {
        return CommonResult.success(documentService.list(new LambdaQueryWrapper<Document>()
                .eq(Document::getCurrentApproverId, userId).eq(Document::getStatus, 0)
                .orderByDesc(Document::getCreateTime)));
    }

    @GetMapping("/{docId}/history")
    @Operation(summary = "查询公文审批历史")
    public CommonResult<List<DocumentApproval>> history(@PathVariable Long docId) {
        return CommonResult.success(approvalService.list(new LambdaQueryWrapper<DocumentApproval>()
                .eq(DocumentApproval::getDocId, docId).orderByAsc(DocumentApproval::getApprovalTime)));
    }

    @PostMapping("/{docId}/approve")
    @Transactional
    @Operation(summary = "审批公文，支持同意拒绝和退回")
    public CommonResult<Document> approve(@PathVariable Long docId, @RequestBody ApprovalRequest request) {
        Document document = documentService.getById(docId);
        if (document == null) return CommonResult.error(1303, "公文不存在");
        if (!Integer.valueOf(0).equals(document.getStatus())) return CommonResult.error(1304, "该公文已结束审批");
        if (request.getApproverId() == null || !request.getApproverId().equals(document.getCurrentApproverId()))
            return CommonResult.error(1305, "您不是当前审批人");
        if (!List.of("同意", "拒绝", "退回").contains(request.getAction())) return CommonResult.error(1306, "审批操作必须为同意、拒绝或退回");

        DocumentApproval approval = new DocumentApproval();
        approval.setDocId(docId);
        approval.setApproverId(request.getApproverId());
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
            int currentIndex = chain.indexOf(request.getApproverId());
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
    @Operation(summary = "催办当前公文审批人")
    public CommonResult<Void> remind(@PathVariable Long docId) {
        Document document = documentService.getById(docId);
        if (document == null || document.getCurrentApproverId() == null) return CommonResult.error(1307, "当前没有可催办的审批人");
        notifyUser(document.getCurrentApproverId(), "公文催办", "请尽快审批《" + document.getTitle() + "》", "公文通知");
        return CommonResult.success();
    }

    private List<Long> parseChain(String json) {
        try { return json == null ? List.of() : JSON.parseArray(json, Long.class); }
        catch (RuntimeException exception) { return List.of(); }
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

    @Data
    public static class ApprovalRequest {
        private Long approverId;
        private String action;
        private String opinion;
    }
}
