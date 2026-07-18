package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Payment;
import com.smartcampus.contract.entity.WorkPlan;
import com.smartcampus.contract.vo.WorkPlanAssigneeVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/work-plan")
@Tag(name = "工作计划与勤工俭学")
public class WorkPlanController {
    private static final String WORK_STUDY_TASK = "勤工俭学";
    private static final String LEGACY_ASSIGNED_TASK = "指派任务";
    private static final String WORK_STUDY_WAGE = "勤工俭学工资";
    private static final BigDecimal MAX_WAGE_AMOUNT = new BigDecimal("10000.00");
    private static final List<String> SELF_PLAN_TYPES = List.of("周计划", "月计划");

    @Autowired private IWorkPlanService workPlanService;
    @Autowired private IPaymentService paymentService;

    @GetMapping("/mine")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "查询本人工作计划或勤工俭学任务")
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
    @Operation(summary = "查询可接收勤工俭学任务的学生")
    public CommonResult<List<WorkPlanAssigneeVo>> assignees() {
        return CommonResult.success(workPlanService.listAssignableUsers(CurrentUserContext.require().userId()));
    }

    @PostMapping("/assign")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Operation(summary = "教师或教职工向学生指派勤工俭学任务")
    public CommonResult<WorkPlan> assign(@RequestBody AssignRequest request) {
        AuthSession assigner = requireWorkStudyAssigner();
        validateContent(request == null ? null : request.getContent());
        Long assignerId = assigner.userId();
        if (request.getAssigneeId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "请选择勤工俭学学生");
        }
        if (!workPlanService.isAssignableUser(request.getAssigneeId())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "所选用户不是可用学生账号");
        }
        validateDateRange(request.getStartDate(), request.getEndDate());
        BigDecimal wageAmount = validateWageAmount(request.getWageAmount());

        WorkPlan task = new WorkPlan();
        task.setUserId(request.getAssigneeId());
        task.setAssignerId(assignerId);
        task.setPlanType(WORK_STUDY_TASK);
        task.setContent(request.getContent().trim());
        task.setStartDate(request.getStartDate());
        task.setEndDate(request.getEndDate());
        task.setStatus(1);
        task.setWageAmount(wageAmount);
        task.setWagePaid(0);
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
            if (CurrentUserContext.require().roles().contains("STUDENT")) {
                throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "学生不能创建教职工周月计划");
            }
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
            if (WORK_STUDY_TASK.equals(existing.getPlanType())) {
                if (Integer.valueOf(1).equals(existing.getWagePaid()) || Integer.valueOf(3).equals(existing.getStatus())) {
                    throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "已结算任务不能修改");
                }
                existing.setStatus(validateWorkStudyStatus(plan.getStatus()));
                workPlanService.updateById(existing);
                return CommonResult.success(existing);
            }
            if (LEGACY_ASSIGNED_TASK.equals(existing.getPlanType())) {
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

    @PostMapping("/{planId}/settle")
    @RequirePermission(OfficePermissions.WORK_PLAN_MANAGE)
    @Transactional
    @Operation(summary = "原指派人确认勤工俭学完成并将工资计入学生余额")
    public CommonResult<WorkPlan> settle(@PathVariable Long planId) {
        AuthSession assigner = requireWorkStudyAssigner();
        WorkPlan task = workPlanService.getById(planId);
        if (task == null) {
            throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "勤工俭学任务不存在");
        }
        if (!WORK_STUDY_TASK.equals(task.getPlanType())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "该记录不是勤工俭学任务");
        }
        if (!assigner.userId().equals(task.getAssignerId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只有原指派人可以确认并发放工资");
        }
        if (!Integer.valueOf(2).equals(task.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "学生尚未提交任务完成");
        }
        if (Integer.valueOf(1).equals(task.getWagePaid())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该任务工资已经发放");
        }
        BigDecimal wageAmount = validateWageAmount(task.getWageAmount());
        Date paidTime = new Date();
        boolean updated = workPlanService.update(new LambdaUpdateWrapper<WorkPlan>()
                .eq(WorkPlan::getPlanId, planId)
                .eq(WorkPlan::getAssignerId, assigner.userId())
                .eq(WorkPlan::getStatus, 2)
                .eq(WorkPlan::getWagePaid, 0)
                .set(WorkPlan::getStatus, 3)
                .set(WorkPlan::getWagePaid, 1)
                .set(WorkPlan::getWagePaidTime, paidTime));
        if (!updated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "任务状态已变化或工资已发放");
        }

        Payment wage = new Payment();
        wage.setStudentId(task.getUserId());
        wage.setWorkPlanId(task.getPlanId());
        wage.setAmount(wageAmount);
        wage.setPaymentType(WORK_STUDY_WAGE);
        wage.setDescription(wageDescription(task));
        wage.setPaymentTime(paidTime);
        if (!paymentService.save(wage)) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "工资流水写入失败");
        }
        task.setStatus(3);
        task.setWagePaid(1);
        task.setWagePaidTime(paidTime);
        return CommonResult.success(task);
    }

    @DeleteMapping("/{planId}")
    @RequirePermission(OfficePermissions.WORK_PLAN_SELF)
    @Operation(summary = "删除工作计划")
    public CommonResult<Boolean> delete(@PathVariable Long planId) {
        WorkPlan plan = workPlanService.getById(planId);
        if (plan == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "工作计划不存在");
        AuthSession session = CurrentUserContext.require();
        if (WORK_STUDY_TASK.equals(plan.getPlanType())) {
            if (Integer.valueOf(1).equals(plan.getWagePaid()) || Integer.valueOf(3).equals(plan.getStatus())) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "已结算勤工俭学任务必须保留，不能删除");
            }
            if (!session.userId().equals(plan.getAssignerId())
                    || !session.hasPermission(OfficePermissions.WORK_PLAN_MANAGE)) {
                throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只有原指派人可以删除未结算任务");
            }
            return CommonResult.success(workPlanService.removeById(planId));
        }
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

    private int validateWorkStudyStatus(Integer status) {
        if (status == null || (status != 1 && status != 2)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "勤工俭学状态必须为进行中或待确认");
        }
        return status;
    }

    private BigDecimal validateWageAmount(BigDecimal wageAmount) {
        if (wageAmount == null || wageAmount.signum() <= 0 || wageAmount.compareTo(MAX_WAGE_AMOUNT) > 0) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "任务工资必须在 0.01 至 10000.00 元之间");
        }
        try {
            return wageAmount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "任务工资最多保留两位小数");
        }
    }

    private AuthSession requireWorkStudyAssigner() {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains("TEACHER") && !session.roles().contains("STAFF")) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "只有教师或教职工可以指派和结算勤工俭学任务");
        }
        return session;
    }

    private String wageDescription(WorkPlan task) {
        String description = "勤工俭学任务#" + task.getPlanId() + "：" + task.getContent();
        return description.length() <= 128 ? description : description.substring(0, 128);
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
        private BigDecimal wageAmount;

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

        public BigDecimal getWageAmount() {
            return wageAmount;
        }

        public void setWageAmount(BigDecimal wageAmount) {
            this.wageAmount = wageAmount;
        }
    }
}
