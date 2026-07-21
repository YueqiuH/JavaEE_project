package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.dto.teaching.AutoScheduleConfigDto;
import com.smartcampus.app.service.teaching.IAutoScheduleService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.auth.permission.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teaching/auto-schedule")
@Tag(name = "自动排课引擎", description = "一键自动排课、约束配置、进度监控、AI辅助诊断")
public class AutoScheduleController {

    private final IAutoScheduleService autoScheduleService;
public AutoScheduleController(IAutoScheduleService autoScheduleService) {        this.autoScheduleService = autoScheduleService;    }

    @PostMapping("/start")
    @RequirePermission("teaching:write")
    @Operation(summary = "启动一键自动排课")
    public CommonResult start(@RequestBody AutoScheduleConfigDto config) {
        return autoScheduleService.startAutoSchedule(config);
    }

    @GetMapping("/progress/{taskId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "查询排课进度")
    public CommonResult progress(@PathVariable String taskId) {
        return autoScheduleService.getProgress(taskId);
    }

    @PostMapping("/cancel/{taskId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "终止排课")
    public CommonResult cancel(@PathVariable String taskId) {
        return autoScheduleService.cancelScheduling(taskId);
    }

    @GetMapping("/diagnostic/{taskId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "查看排课诊断报告")
    public CommonResult diagnostic(@PathVariable String taskId) {
        return autoScheduleService.getDiagnosticReport(taskId);
    }

    @GetMapping("/heatmap/{scheduleId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "获取槽位可用性热力图")
    public CommonResult heatmap(@PathVariable Long scheduleId, @RequestParam String semester) {
        return autoScheduleService.getSlotHeatmap(scheduleId, semester);
    }

    @GetMapping("/recommend/{scheduleId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "AI推荐微调方案")
    public CommonResult recommend(@PathVariable Long scheduleId, @RequestParam String semester) {
        return autoScheduleService.getAiRecommendations(scheduleId, semester);
    }

    @GetMapping("/locked")
    @RequirePermission("teaching:write")
    @Operation(summary = "查看已锁定排课列表")
    public CommonResult locked(@RequestParam String semester) {
        return autoScheduleService.listLockedSchedules(semester);
    }

    @PostMapping("/toggle-lock/{scheduleId}")
    @RequirePermission("teaching:write")
    @Operation(summary = "锁定/解锁单条排课")
    public CommonResult toggleLock(@PathVariable Long scheduleId, @RequestParam boolean locked) {
        return autoScheduleService.toggleLock(scheduleId, locked);
    }

    @GetMapping("/quality-score")
    @RequirePermission("teaching:write")
    @Operation(summary = "获取排课质量评分")
    public CommonResult qualityScore(@RequestParam String semester) {
        return autoScheduleService.getQualityScore(semester);
    }
}
