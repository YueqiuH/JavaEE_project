package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.AiReportService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.dto.AiReportRequest;
import com.smartcampus.contract.vo.AiReportVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/base/ai/reports")
@Tag(name = "基础数据 - AI 自然语言报表")
@SecurityRequirement(name = "bearerAuth")
public class AiReportController {

    private final AiReportService aiReportService;

    public AiReportController(AiReportService aiReportService) {
        this.aiReportService = aiReportService;
    }

    @PostMapping
    @Operation(summary = "自然语言生成报表",
            description = "输入中文问题（如：统计今年计算机专业新生的报到率并对比去年），"
                    + "AI 生成只读 SQL 并执行，返回分析文字、图表定义与明细数据；"
                    + "未配置 DEEPSEEK_API_KEY 时返回演示报表；"
                    + "错误示例：409110 AI 生成的查询未通过安全校验 / 502101 AI 服务调用失败")
    @RequirePermission("base:read")
    public CommonResult<AiReportVo> generate(@Valid @RequestBody AiReportRequest request) {
        return CommonResult.success(aiReportService.generate(request.getQuestion()));
    }
}
