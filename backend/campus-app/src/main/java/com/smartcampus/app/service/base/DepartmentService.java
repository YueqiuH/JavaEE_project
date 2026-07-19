package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.DepartmentSaveRequest;
import com.smartcampus.contract.entity.Department;
import com.smartcampus.contract.vo.DepartmentSummaryVo;

public interface DepartmentService {

    IPage<DepartmentSummaryVo> pageSummary(PageParam pageParam, String keyword);

    Department getById(Long deptId);

    Department create(DepartmentSaveRequest request);

    Department update(Long deptId, DepartmentSaveRequest request);

    void delete(Long deptId);
}
