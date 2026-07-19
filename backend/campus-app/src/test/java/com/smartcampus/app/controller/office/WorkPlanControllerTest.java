package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.Payment;
import com.smartcampus.contract.entity.WorkPlan;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkPlanControllerTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), WorkPlan.class);
    }

    @Mock
    private IWorkPlanService workPlanService;
    @Mock
    private IPaymentService paymentService;

    @InjectMocks
    private WorkPlanController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void managerCanAssignTaskToEligibleUser() {
        setCurrentUser(3L, "TEACHER", Set.of("work-plan:manage"));
        when(workPlanService.isAssignableUser(2L)).thenReturn(true);
        when(workPlanService.save(any(WorkPlan.class))).thenReturn(true);

        WorkPlanController.AssignRequest request = assignRequest(2L, "完成本周教学材料整理");
        WorkPlan task = controller.assign(request).getData();

        ArgumentCaptor<WorkPlan> captor = ArgumentCaptor.forClass(WorkPlan.class);
        verify(workPlanService).save(captor.capture());
        assertEquals(2L, task.getUserId());
        assertEquals("勤工俭学", task.getPlanType());
        assertEquals("完成本周教学材料整理", task.getContent());
        assertEquals(1, task.getStatus());
        assertEquals(3L, task.getAssignerId());
        assertEquals(new BigDecimal("100.00"), task.getWageAmount());
        assertEquals(0, task.getWagePaid());
        assertEquals(task, captor.getValue());
    }

    @Test
    void managerCannotAssignTaskToSelf() {
        setCurrentUser(3L, "STAFF", Set.of("work-plan:manage"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.assign(assignRequest(3L, "本人任务")));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void personalPlanCannotSpoofAssignedTask() {
        setCurrentUser(2L, "STAFF", Set.of("work-plan:self"));
        WorkPlan plan = new WorkPlan();
        plan.setPlanType("勤工俭学");
        plan.setContent("伪造指派任务");

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(plan));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void assigneeCanOnlyUpdateAssignedTaskStatus() {
        setCurrentUser(2L, "STUDENT", Set.of("work-plan:self"));
        WorkPlan existing = new WorkPlan();
        existing.setPlanId(8L);
        existing.setUserId(2L);
        existing.setPlanType("勤工俭学");
        existing.setContent("负责人指派的原始任务");
        existing.setStatus(1);
        existing.setWageAmount(new BigDecimal("80.00"));
        existing.setWagePaid(0);
        when(workPlanService.getById(8L)).thenReturn(existing);
        when(workPlanService.updateById(existing)).thenReturn(true);

        WorkPlan request = new WorkPlan();
        request.setPlanId(8L);
        request.setPlanType("周计划");
        request.setStatus(2);
        WorkPlan updated = controller.save(request).getData();

        assertEquals("勤工俭学", updated.getPlanType());
        assertEquals("负责人指派的原始任务", updated.getContent());
        assertEquals(2, updated.getStatus());
        verify(workPlanService).updateById(existing);
    }

    @Test
    void studentCannotCreatePersonalWorkPlan() {
        setCurrentUser(2L, "STUDENT", Set.of("work-plan:self"));
        WorkPlan plan = new WorkPlan();
        plan.setPlanType("周计划");
        plan.setContent("学生伪造的教职工计划");

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(plan));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    @Test
    void originalAssignerSettlesWageExactlyOnceIntoPaymentLedger() {
        setCurrentUser(3L, "TEACHER", Set.of("work-plan:manage"));
        WorkPlan task = new WorkPlan();
        task.setPlanId(9L);
        task.setUserId(2L);
        task.setAssignerId(3L);
        task.setPlanType("勤工俭学");
        task.setContent("图书整理");
        task.setStatus(2);
        task.setWageAmount(new BigDecimal("120.50"));
        task.setWagePaid(0);
        when(workPlanService.getById(9L)).thenReturn(task);
        when(workPlanService.update(any())).thenReturn(true);
        when(paymentService.save(any(Payment.class))).thenReturn(true);

        WorkPlan settled = controller.settle(9L).getData();

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentService).save(paymentCaptor.capture());
        assertEquals(2L, paymentCaptor.getValue().getStudentId());
        assertEquals(9L, paymentCaptor.getValue().getWorkPlanId());
        assertEquals(new BigDecimal("120.50"), paymentCaptor.getValue().getAmount());
        assertEquals("勤工俭学工资", paymentCaptor.getValue().getPaymentType());
        assertEquals(3, settled.getStatus());
        assertEquals(1, settled.getWagePaid());
    }

    @Test
    void adminCannotAssignWorkStudyTask() {
        setCurrentUser(4L, "ADMIN", Set.of("work-plan:manage"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.assign(assignRequest(2L, "越权任务")));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    @Test
    void assignRejectsWageWithMoreThanTwoDecimals() {
        setCurrentUser(3L, "TEACHER", Set.of("work-plan:manage"));
        when(workPlanService.isAssignableUser(2L)).thenReturn(true);
        WorkPlanController.AssignRequest request = assignRequest(2L, "整理资料");
        request.setWageAmount(new BigDecimal("10.001"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.assign(request));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void differentManagerCannotSettleAnotherAssignersTask() {
        setCurrentUser(4L, "STAFF", Set.of("work-plan:manage"));
        WorkPlan task = new WorkPlan();
        task.setPlanId(10L);
        task.setUserId(2L);
        task.setAssignerId(3L);
        task.setPlanType("勤工俭学");
        task.setStatus(2);
        task.setWageAmount(new BigDecimal("60.00"));
        task.setWagePaid(0);
        when(workPlanService.getById(10L)).thenReturn(task);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.settle(10L));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    private WorkPlanController.AssignRequest assignRequest(Long assigneeId, String content) {
        WorkPlanController.AssignRequest request = new WorkPlanController.AssignRequest();
        request.setAssigneeId(assigneeId);
        request.setContent(content);
        request.setWageAmount(new BigDecimal("100.00"));
        return request;
    }

    private void setCurrentUser(Long userId, String role, Set<String> permissions) {
        CurrentUserContext.set(new AuthSession(userId, "user" + userId, 3,
                Set.of(role), permissions, 1L));
    }
}
