package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.IAiChatService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.dto.AiChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/teaching/ai")
@Tag(name = "教学服务 - AI 智能学习助理")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final IAiChatService aiChatService;

    public AiChatController(IAiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 智能对话(SSE 流式)")
    public SseEmitter chat(@Valid @RequestBody AiChatRequest request) {
        Long userId = CurrentUserContext.require().userId();
        return aiChatService.chat(request.getMessage(), request.getConversationId(), userId);
    }

    @GetMapping("/history")
    @Operation(summary = "对话历史列表")
    public CommonResult getHistory() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(aiChatService.getHistory(userId));
    }

    @GetMapping("/history/{convId}")
    @Operation(summary = "查看指定对话")
    public CommonResult getConversation(@PathVariable String convId) {
        return CommonResult.success(aiChatService.getConversation(convId));
    }

    @DeleteMapping("/history/{convId}")
    @Operation(summary = "删除对话")
    public CommonResult deleteConversation(@PathVariable String convId) {
        aiChatService.deleteConversation(convId);
        return CommonResult.success();
    }
}
