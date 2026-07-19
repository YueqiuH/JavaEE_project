package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.DepartmentMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.DepartmentService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.DepartmentSaveRequest;
import com.smartcampus.contract.entity.Department;
import com.smartcampus.contract.vo.DepartmentSummaryVo;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public IPage<DepartmentSummaryVo> pageSummary(PageParam pageParam, String keyword) {
        Page<DepartmentSummaryVo> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        return departmentMapper.selectSummaryPage(page, keyword);
    }

    @Override
    public Department getById(Long deptId) {
        Department department = departmentMapper.selectById(deptId);
        if (department == null) {
            throw new BusinessException(BaseErrorCodes.DEPT_NOT_FOUND);
        }
        return department;
    }

    @Override
    public Department create(DepartmentSaveRequest request) {
        assertCodeAvailable(request.getDeptCode(), null);
        Department department = new Department();
        department.setDeptName(request.getDeptName());
        department.setDeptCode(request.getDeptCode());
        department.setDescription(request.getDescription());
        departmentMapper.insert(department);
        return department;
    }

    @Override
    public Department update(Long deptId, DepartmentSaveRequest request) {
        Department department = getById(deptId);
        assertCodeAvailable(request.getDeptCode(), deptId);
        department.setDeptName(request.getDeptName());
        department.setDeptCode(request.getDeptCode());
        department.setDescription(request.getDescription());
        departmentMapper.updateById(department);
        return department;
    }

    @Override
    public void delete(Long deptId) {
        getById(deptId);
        if (departmentMapper.countMajors(deptId) > 0) {
            throw new BusinessException(BaseErrorCodes.DEPT_HAS_MAJORS);
        }
        departmentMapper.deleteById(deptId);
    }

    /** 校验院系编号未被其他院系占用 */
    private void assertCodeAvailable(String deptCode, Long excludeDeptId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptCode, deptCode);
        if (excludeDeptId != null) {
            wrapper.ne(Department::getDeptId, excludeDeptId);
        }
        if (departmentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCodes.DEPT_CODE_DUPLICATE);
        }
    }
}
