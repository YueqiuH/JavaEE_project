package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.WorkPlanMapper;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.contract.entity.WorkPlan;
import com.smartcampus.contract.vo.WorkPlanAssigneeVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkPlanServiceImpl extends ServiceImpl<WorkPlanMapper, WorkPlan> implements IWorkPlanService {

    @Override
    public List<WorkPlanAssigneeVo> listAssignableUsers(Long excludeUserId) {
        return baseMapper.findAssignableUsers(excludeUserId);
    }

    @Override
    public boolean isAssignableUser(Long userId) {
        return userId != null && baseMapper.countAssignableUser(userId) > 0;
    }
}
