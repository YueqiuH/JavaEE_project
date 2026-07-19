package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.WorkPlan;
import com.smartcampus.contract.vo.WorkPlanAssigneeVo;

import java.util.List;

public interface IWorkPlanService extends IService<WorkPlan> {

    List<WorkPlanAssigneeVo> listAssignableUsers(Long excludeUserId);

    boolean isAssignableUser(Long userId);
}
