package com.smartcampus.app.service.teaching;

import com.smartcampus.contract.vo.AiChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface IAiChatService {

    SseEmitter chat(String message, String conversationId, Long userId, String semester);

    List<Map<String, Object>> getHistory(Long userId);

    List<Map<String, Object>> getConversation(String convId);

    void deleteConversation(String convId);
}
