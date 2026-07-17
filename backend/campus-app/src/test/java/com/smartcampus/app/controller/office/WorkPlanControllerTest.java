package com.smartcampus.app.controller.office;

import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.WorkPlan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkPlanControllerTest {

    @Mock
    private IWorkPlanService workPlanService;

    @InjectMocks
    private WorkPlanController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void managerCanAssignTaskToEligibleUser() {
        setCurrentUser(3L, Set.of("work-plan:manage"));
        when(workPlanService.isAssignableUser(2L)).thenReturn(true);
        when(workPlanService.save(any(WorkPlan.class))).thenReturn(true);

        WorkPlanController.AssignRequest request = assignRequest(2L, "完成本周教学材料整理");
        WorkPlan task = controller.assign(request).getData();

        ArgumentCaptor<WorkPlan> captor = ArgumentCaptor.forClass(WorkPlan.class);
        verify(workPlanService).save(captor.capture());
        assertEquals(2L, task.getUserId());
        assertEquals("指派任务", task.getPlanType());
        assertEquals("完成本周教学材料整理", task.getContent());
        assertEquals(1, task.getStatus());
        assertEquals(task, captor.getValue());
    }

    @Test
    void managerCannotAssignTaskToSelf() {
        setCurrentUser(3L, Set.of("work-plan:manage"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.assign(assignRequest(3L, "本人任务")));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void personalPlanCannotSpoofAssignedTask() {
        setCurrentUser(2L, Set.of("work-plan:self"));
        WorkPlan plan = new WorkPlan();
        plan.setPlanType("指派任务");
        plan.setContent("伪造指派任务");

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(plan));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void assigneeCanOnlyUpdateAssignedTaskStatus() {
        setCurrentUser(2L, Set.of("work-plan:self"));
        WorkPlan existing = new WorkPlan();
        existing.setPlanId(8L);
        existing.setUserId(2L);
        existing.setPlanType("指派任务");
        existing.setContent("负责人指派的原始任务");
        existing.setStatus(1);
        when(workPlanService.getById(8L)).thenReturn(existing);
        when(workPlanService.updateById(existing)).thenReturn(true);

        WorkPlan request = new WorkPlan();
        request.setPlanId(8L);
        request.setPlanType("周计划");
        request.setStatus(2);
        WorkPlan updated = controller.save(request).getData();

        assertEquals("指派任务", updated.getPlanType());
        assertEquals("负责人指派的原始任务", updated.getContent());
        assertEquals(2, updated.getStatus());
        verify(workPlanService).updateById(existing);
    }

    private WorkPlanController.AssignRequest assignRequest(Long assigneeId, String content) {
        WorkPlanController.AssignRequest request = new WorkPlanController.AssignRequest();
        request.setAssigneeId(assigneeId);
        request.setContent(content);
        return request;
    }

    private void setCurrentUser(Long userId, Set<String> permissions) {
        CurrentUserContext.set(new AuthSession(userId, "user" + userId, 3,
                Set.of("STAFF"), permissions, 1L));
    }
}
