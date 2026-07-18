package com.smartcampus.app.service.teaching;

import com.smartcampus.app.dto.teaching.AutoScheduleConfigDto;
import com.smartcampus.common.result.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * 一键自动排课服务 —— 双层约束评估 + 多轮次逐步求解。
 */
public interface IAutoScheduleService {

    /** 启动一键自动排课（异步执行） */
    CommonResult startAutoSchedule(AutoScheduleConfigDto config);

    /** 获取排课进度 */
    CommonResult getProgress(String taskId);

    /** 终止排课 */
    CommonResult cancelScheduling(String taskId);

    /** 获取排课诊断报告 */
    CommonResult getDiagnosticReport(String taskId);

    /** 获取指定任务在所有槽位的可用性热力图 */
    CommonResult getSlotHeatmap(Long scheduleId, String semester);

    /** AI 推荐：为单个冲突任务生成 3 个微调方案 */
    CommonResult getAiRecommendations(Long scheduleId, String semester);

    /** 列出所有已锁定的排课记录 */
    CommonResult listLockedSchedules(String semester);

    /** 锁定/解锁单条排课 */
    CommonResult toggleLock(Long scheduleId, boolean locked);

    /** 获取排课质量评分 */
    CommonResult getQualityScore(String semester);
}
