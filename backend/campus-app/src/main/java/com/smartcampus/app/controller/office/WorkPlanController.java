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
import com.smartcampus.contract.vo.WorkPlanAssigneeVo;
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
    private static final String ASSIGNED_TASK = "指派任务";
    private static final List<String> SELF_PLAN_TYPES = List.of("周计划", "月计划");

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

    @GetMapping("/assignees")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Operation(summary = "查询可接收指派任务的教职工")
    public CommonResult<List<WorkPlanAssigneeVo>> assignees() {
        return CommonResult.success(workPlanService.listAssignableUsers(CurrentUserContext.require().userId()));
    }

    @PostMapping("/assign")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Operation(summary = "负责人直接创建并指派任务")
    public CommonResult<WorkPlan> assign(@RequestBody AssignRequest request) {
        validateContent(request == null ? null : request.getContent());
        Long assignerId = CurrentUserContext.require().userId();
        if (request.getAssigneeId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "请选择任务接收人");
        }
        if (assignerId.equals(request.getAssigneeId())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "如需安排本人工作，请创建个人工作计划");
        }
        if (!workPlanService.isAssignableUser(request.getAssigneeId())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "所选用户不能接收工作任务");
        }
        validateDateRange(request.getStartDate(), request.getEndDate());

        WorkPlan task = new WorkPlan();
        task.setUserId(request.getAssigneeId());
        task.setPlanType(ASSIGNED_TASK);
        task.setContent(request.getContent().trim());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setStatus(1);
        task.setCreateTime(new Date());
        workPlanService.save(task);
        return CommonResult.success(task);
    }

    @PostMapping("/save")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "制定或修改周月工作计划")
    public CommonResult<WorkPlan> save(@RequestBody WorkPlan plan) {
        if (plan == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "工作计划不能为空");
        }
        Long currentUserId = CurrentUserContext.require().userId();
        if (plan.getPlanId() == null) {
            validateContent(plan.getContent());
            validateSelfPlanType(plan.getPlanType());
            validateDateRange(plan.getStartDate(), plan.getEndDate());
            plan.setUserId(currentUserId);
            plan.setContent(plan.getContent().trim());
            plan.setStatus(validateStatus(plan.getStatus()));
            plan.setCreateTime(new Date());
        } else {
            WorkPlan existing = workPlanService.getById(plan.getPlanId());
            if (existing == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "工作计划不存在");
            if (!currentUserId.equals(existing.getUserId())) {
                throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能修改他人的工作计划");
            }
            if (ASSIGNED_TASK.equals(existing.getPlanType())) {
                existing.setStatus(validateStatus(plan.getStatus()));
                workPlanService.updateById(existing);
                return CommonResult.success(existing);
            }
            validateContent(plan.getContent());
            validateSelfPlanType(plan.getPlanType());
            validateDateRange(plan.getStartDate(), plan.getEndDate());
            plan.setUserId(existing.getUserId());
            plan.setContent(plan.getContent().trim());
            plan.setStatus(validateStatus(plan.getStatus()));
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

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "计划或任务内容不能为空");
        }
    }

    private void validateSelfPlanType(String planType) {
        if (!SELF_PLAN_TYPES.contains(planType)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "个人计划类型必须为周计划或月计划");
        }
    }

    private int validateStatus(Integer status) {
        if (status == null) {
            return 1;
        }
        if (status != 1 && status != 2) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "状态必须为进行中或已完成");
        }
        return status;
    }

    private void validateDateRange(Date startDate, Date endDate) {
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "开始日期不能晚于结束日期");
        }
    }

    public static class AssignRequest {
        private Long assigneeId;
        private String content;
        private Date startDate;
        private Date endDate;

        public Long getAssigneeId() {
            return assigneeId;
        }

        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }
    }
}
