package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.dto.StaffQuery;
import com.smartcampus.contract.dto.StaffSaveRequest;
import com.smartcampus.contract.vo.StaffVo;

public interface StaffService {

    IPage<StaffVo> pageVo(StaffQuery query);

    /** 创建教职工档案并开通登录账号（随机密码），按类别赋予 TEACHER/STAFF 角色 */
    StaffVo create(StaffSaveRequest request);

    /** 更新档案信息（不修改工号与密码） */
    StaffVo update(Long userId, StaffSaveRequest request);

    /** 停用账号（软删除） */
    void disable(Long userId);
}
