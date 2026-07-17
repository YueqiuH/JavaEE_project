package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.WorkPlan;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/work-plan")
@Tag(name = "教职工工作计划与协同")
public class WorkPlanController {
    @Autowired private IWorkPlanService workPlanService;

    @GetMapping("/mine")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "查询教职工个人工作计划")
    public CommonResult<List<WorkPlan>> myPlans() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(workPlanService.list(new LambdaQueryWrapper<WorkPlan>()
                .eq(WorkPlan::getUserId, userId).orderByDesc(WorkPlan::getCreateTime)));
    }

    @GetMapping("/list")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Operation(summary = "负责人查询下属工作计划")
    public CommonResult<List<WorkPlan>> list() {
        return CommonResult.success(workPlanService.list(new LambdaQueryWrapper<WorkPlan>().orderByDesc(WorkPlan::getCreateTime)));
    }

    @PostMapping("/save")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "制定或修改周月工作计划")
    public CommonResult<WorkPlan> save(@RequestBody WorkPlan plan) {
        if (plan.getContent() == null || plan.getContent().isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "计划内容不能为空");
        }
        Long currentUserId = CurrentUserContext.require().userId();
        if (plan.getPlanId() == null) {
            plan.setUserId(currentUserId);
            if (plan.getStatus() == null) plan.setStatus(1);
            plan.setCreateTime(new Date());
        } else {
            WorkPlan existing = workPlanService.getById(plan.getPlanId());
            if (existing == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "工作计划不存在");
            if (!currentUserId.equals(existing.getUserId())) {
                throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能修改他人的工作计划");
            }
            plan.setUserId(existing.getUserId());
            plan.setCreateTime(existing.getCreateTime());
            plan.setSupervisorComment(existing.getSupervisorComment());
        }
        workPlanService.saveOrUpdate(plan);
        return CommonResult.success(plan);
    }

    @PostMapping("/comment/{planId}")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Operation(summary = "负责人点评下属工作计划")
    public CommonResult<WorkPlan> comment(@PathVariable Long planId, @RequestBody String comment) {
        WorkPlan plan = workPlanService.getById(planId);
        if (plan == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "工作计划不存在");
        if (comment == null || comment.isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "点评内容不能为空");
        }
        plan.setSupervisorComment(comment);
        workPlanService.updateById(plan);
        return CommonResult.success(plan);
    }

    @DeleteMapping("/{planId}")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "删除工作计划")
    public CommonResult<Boolean> delete(@PathVariable Long planId) {
        WorkPlan plan = workPlanService.getById(planId);
        if (plan == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "工作计划不存在");
        AuthSession session = CurrentUserContext.require();
        if (!session.userId().equals(plan.getUserId()) && !session.hasPermission(OfficePermissions.WORK_PLAN_MANAGE)) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能删除他人的工作计划");
        }
        return CommonResult.success(workPlanService.removeById(planId));
    }
}
