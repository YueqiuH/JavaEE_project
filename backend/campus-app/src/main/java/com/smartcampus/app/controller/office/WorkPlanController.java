package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.WorkPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/office/work-plan")
@Tag(name = "教职工工作计划与协同")
public class WorkPlanController {
    @Autowired private IWorkPlanService workPlanService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询教职工个人工作计划")
    public CommonResult<List<WorkPlan>> userPlans(@PathVariable Long userId) {
        return CommonResult.success(workPlanService.list(new LambdaQueryWrapper<WorkPlan>()
                .eq(WorkPlan::getUserId, userId).orderByDesc(WorkPlan::getCreateTime)));
    }

    @GetMapping("/list")
    @Operation(summary = "负责人查询下属工作计划")
    public CommonResult<List<WorkPlan>> list() {
        return CommonResult.success(workPlanService.list(new LambdaQueryWrapper<WorkPlan>().orderByDesc(WorkPlan::getCreateTime)));
    }

    @PostMapping("/save")
    @Operation(summary = "制定或修改周月工作计划")
    public CommonResult<WorkPlan> save(@RequestBody WorkPlan plan) {
        if (plan.getUserId() == null || plan.getContent() == null || plan.getContent().isBlank())
            return CommonResult.error(1201, "用户和计划内容不能为空");
        if (plan.getPlanId() == null) {
            if (plan.getStatus() == null) plan.setStatus(1);
            plan.setCreateTime(new Date());
        }
        workPlanService.saveOrUpdate(plan);
        return CommonResult.success(plan);
    }

    @PostMapping("/comment/{planId}")
    @Operation(summary = "负责人点评下属工作计划")
    public CommonResult<WorkPlan> comment(@PathVariable Long planId, @RequestBody String comment) {
        WorkPlan plan = workPlanService.getById(planId);
        if (plan == null) return CommonResult.error(1202, "工作计划不存在");
        plan.setSupervisorComment(comment);
        workPlanService.updateById(plan);
        return CommonResult.success(plan);
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "删除工作计划")
    public CommonResult<Boolean> delete(@PathVariable Long planId) {
        return CommonResult.success(workPlanService.removeById(planId));
    }
}
