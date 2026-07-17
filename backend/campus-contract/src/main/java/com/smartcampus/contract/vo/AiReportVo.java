package com.smartcampus.contract.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI 自然语言报表结果：分析文字 + 图表定义 + 明细数据。
 */
@Data
public class AiReportVo implements Serializable {

    /** 原始问题 */
    private String question;

    /** 报表标题 */
    private String title;

    /** 建议图表类型：bar / line / pie / table */
    private String chartType;

    /** AI 生成并通过安全校验的 SQL（仅展示用） */
    private String sql;

    /** AI 分析文字 */
    private String analysis;

    /** 结果列名（按查询顺序） */
    private List<String> columns;

    /** 结果数据行 */
    private List<Map<String, Object>> rows;

    /** 是否演示模式（未配置 API Key 时为 true） */
    private boolean demoMode;
}
