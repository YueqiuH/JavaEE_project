package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.WorkPlanMapper;
import com.smartcampus.app.service.office.IWorkPlanService;
import com.smartcampus.contract.entity.WorkPlan;
import org.springframework.stereotype.Service;

@Service
public class WorkPlanServiceImpl extends ServiceImpl<WorkPlanMapper, WorkPlan> implements IWorkPlanService {
}
