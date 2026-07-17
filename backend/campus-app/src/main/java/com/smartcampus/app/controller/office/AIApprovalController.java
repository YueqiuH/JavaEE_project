package com.smartcampus.app.controller.office;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.app.service.office.IDocumentService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Document;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import com.smartcampus.contract.entity.DocumentApproval;

@RestController
@RequestMapping("/api/v1/office/ai-approval")
@Tag(name = "AI公文摘要与审批助手")
public class AIApprovalController {
    private static final Logger log = LoggerFactory.getLogger(AIApprovalController.class);

    @Autowired private IDocumentService documentService;
    @Autowired private IDocumentApprovalService approvalService;
    @Autowired private ObjectProvider<ChatModel> chatModels;

    @PostMapping("/summary/{docId}")
    @RequirePermission(OfficePermissions.AI_APPROVAL_USE)
    @Operation(summary = "AI提炼公文核心要点和诉求")
    public CommonResult<String> summary(@PathVariable Long docId) {
        Document document = requirePendingDocument(docId);
        String prompt = """
                你是高校公文处理助手。请严格根据下列公文生成简洁摘要：
                1. 用一句话概括事项；2. 列出核心要点；3. 明确申请人诉求；4. 不得编造原文没有的信息。
                标题：%s
                类型：%s
                正文：%s
                """.formatted(document.getTitle(), document.getDocType(), document.getContent());
        return callModel(prompt);
    }

    @PostMapping("/recommend/{docId}")
    @RequirePermission(OfficePermissions.AI_APPROVAL_USE)
    @Operation(summary = "AI生成同意或不同意的审批意见草稿")
    public CommonResult<String> recommend(@PathVariable Long docId,
                                          @RequestBody(required = false) RecommendRequest request) {
        Document document = requirePendingDocument(docId);
        List<DocumentApproval> history = document.getCurrentApproverId() == null ? List.of() :
                approvalService.list(new LambdaQueryWrapper<DocumentApproval>()
                        .eq(DocumentApproval::getApproverId, document.getCurrentApproverId())
                        .orderByDesc(DocumentApproval::getApprovalTime).last("LIMIT 10"));
        String providedRules = request == null ? null : request.getSchoolRules();
        if (providedRules != null && providedRules.length() > 4000) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "学校规章内容不能超过4000字");
        }
        String rules = providedRules == null || providedRules.isBlank()
                ? "未提供具体规章，不得自行编造制度依据" : providedRules;
        String prompt = """
                你是高校审批辅助人员。请根据公文内容给出审批建议草稿。
                依次输出：建议结论（同意/不同意/需补充材料）、事实依据、风险点、可直接使用的审批意见。
                参考同一审批人的历史记录归纳其审批关注点，但不得机械照搬；仅引用下方提供的规章，不得虚构制度。
                提醒使用者：AI仅提供辅助建议，最终决定由审批人作出。
                公文标题：%s
                公文类型：%s
                公文内容：%s
                最近审批记录：%s
                本次适用的学校规章：%s
                """.formatted(document.getTitle(), document.getDocType(), document.getContent(),
                JSON.toJSONString(history), rules);
        return callModel(prompt);
    }

    private CommonResult<String> callModel(String prompt) {
        ChatModel model = chatModels.orderedStream().findFirst().orElse(null);
        if (model == null) throw new BusinessException(OfficeErrorCodeConstants.AI_NOT_CONFIGURED);
        try { return CommonResult.success(model.call(prompt)); }
        catch (RuntimeException exception) {
            log.error("AI 公文审批调用失败，doc prompt length={}", prompt.length(), exception);
            throw new BusinessException(OfficeErrorCodeConstants.AI_UNAVAILABLE);
        }
    }

    private Document requirePendingDocument(Long docId) {
        Document document = documentService.getById(docId);
        if (document == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "公文不存在");
        Long userId = CurrentUserContext.require().userId();
        if (!Integer.valueOf(0).equals(document.getStatus()) || !userId.equals(document.getCurrentApproverId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只能分析当前由您审批的公文");
        }
        return document;
    }

    public static class RecommendRequest {
        private String schoolRules;

        public String getSchoolRules() {
            return schoolRules;
        }

        public void setSchoolRules(String schoolRules) {
            this.schoolRules = schoolRules;
        }
    }
}
