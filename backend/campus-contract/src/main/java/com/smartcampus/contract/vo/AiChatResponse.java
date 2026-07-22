package com.smartcampus.contract.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse implements Serializable {

    private String type;
    private String content;
    private String conversationId;
    private String intent;
    private String action;
    private Map<String, Object> actionParams;
    private String actionConfirm;
    private List<String> suggestions;
    private String title;
    private String chartType;
    private List<String> columns;
    private List<Map<String, Object>> rows;
    private Map<String, Object> progress;
    private boolean demoMode;
}
