package com.smartcampus.app.service.base;

import com.smartcampus.contract.vo.AiReportVo;

public interface AiReportService {

    /**
     * 自然语言 → SQL → 查询 → 分析文字与图表定义。
     * 未配置 DEEPSEEK_API_KEY 时进入演示模式，返回预置报表。
     */
    AiReportVo generate(String question);
}
