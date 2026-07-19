package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.DepartmentMapper;
import com.smartcampus.app.dao.base.MajorMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.MajorService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.contract.dto.MajorSaveRequest;
import com.smartcampus.contract.entity.Major;
import com.smartcampus.contract.vo.MajorVo;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl implements MajorService {

    private final MajorMapper majorMapper;
    private final DepartmentMapper departmentMapper;

    public MajorServiceImpl(MajorMapper majorMapper, DepartmentMapper departmentMapper) {
        this.majorMapper = majorMapper;
        this.departmentMapper = departmentMapper;
    }

    @Override
    public IPage<MajorVo> pageVo(PageParam pageParam, Long deptId, String keyword) {
        Page<MajorVo> page = new Page<>(pageParam.getPage(), pageParam.getSize());
        return majorMapper.selectVoPage(page, deptId, keyword);
    }

    @Override
    public Major create(MajorSaveRequest request) {
        assertDeptExists(request.getDeptId());
        assertCodeAvailable(request.getMajorCode(), null);
        Major major = new Major();
        major.setDeptId(request.getDeptId());
        major.setMajorName(request.getMajorName());
        major.setMajorCode(request.getMajorCode());
        major.setCultivationPlan(request.getCultivationPlan());
        majorMapper.insert(major);
        return major;
    }

    @Override
    public Major update(Long majorId, MajorSaveRequest request) {
        Major major = requireMajor(majorId);
        assertDeptExists(request.getDeptId());
        assertCodeAvailable(request.getMajorCode(), majorId);
        major.setDeptId(request.getDeptId());
        major.setMajorName(request.getMajorName());
        major.setMajorCode(request.getMajorCode());
        major.setCultivationPlan(request.getCultivationPlan());
        majorMapper.updateById(major);
        return major;
    }

    @Override
    public void delete(Long majorId) {
        requireMajor(majorId);
        if (majorMapper.countStudents(majorId) > 0) {
            throw new BusinessException(BaseErrorCodes.MAJOR_HAS_STUDENTS);
        }
        majorMapper.deleteById(majorId);
    }

    private Major requireMajor(Long majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(BaseErrorCodes.MAJOR_NOT_FOUND);
        }
        return major;
    }

    private void assertDeptExists(Long deptId) {
        if (departmentMapper.selectById(deptId) == null) {
            throw new BusinessException(BaseErrorCodes.DEPT_NOT_FOUND);
        }
    }

    /** 校验专业编号未被其他专业占用 */
    private void assertCodeAvailable(String majorCode, Long excludeMajorId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<Major>()
                .eq(Major::getMajorCode, majorCode);
        if (excludeMajorId != null) {
            wrapper.ne(Major::getMajorId, excludeMajorId);
        }
        if (majorMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(BaseErrorCodes.MAJOR_CODE_DUPLICATE);
        }
    }
}
