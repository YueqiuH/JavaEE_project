package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.DepartmentMapper;
import com.smartcampus.app.dao.base.StaffMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.StaffService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.StaffQuery;
import com.smartcampus.contract.dto.StaffSaveRequest;
import com.smartcampus.contract.entity.Department;
import com.smartcampus.contract.entity.UserEntity;
import com.smartcampus.contract.vo.StaffVo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffServiceImpl implements StaffService {

    /** 新开账号的默认登录密码 */
    private static final String DEFAULT_PASSWORD = "123321";

    private static final int USER_TYPE_TEACHER = 2;

    private final StaffMapper staffMapper;
    private final DepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;

    public StaffServiceImpl(StaffMapper staffMapper,
                            DepartmentMapper departmentMapper,
                            PasswordEncoder passwordEncoder) {
        this.staffMapper = staffMapper;
        this.departmentMapper = departmentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public IPage<StaffVo> pageVo(StaffQuery query) {
        Page<StaffVo> page = new Page<>(query.getPage(), query.getSize());
        return staffMapper.selectVoPage(page, query);
    }

    @Override
    @Transactional
    public StaffVo create(StaffSaveRequest request) {
        assertUsernameAvailable(request.getUsername());
        assertDeptExists(request.getDeptId());
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setUserType(request.getUserType());
        applyProfile(user, request);
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        staffMapper.insert(user);
        staffMapper.assignRole(user.getUserId(),
                request.getUserType() == USER_TYPE_TEACHER ? "TEACHER" : "STAFF");
        return toVo(user);
    }

    @Override
    public StaffVo update(Long userId, StaffSaveRequest request) {
        UserEntity user = requireStaff(userId);
        assertDeptExists(request.getDeptId());
        user.setUserType(request.getUserType());
        applyProfile(user, request);
        staffMapper.updateById(user);
        return toVo(user);
    }

    @Override
    public void disable(Long userId) {
        UserEntity user = requireStaff(userId);
        user.setStatus(0);
        staffMapper.updateById(user);
    }

    private UserEntity requireStaff(Long userId) {
        UserEntity user = staffMapper.selectById(userId);
        if (user == null || user.getUserType() == null || user.getUserType() < 2 || user.getUserType() > 3) {
            throw new BusinessException(BaseErrorCodes.STAFF_NOT_FOUND);
        }
        return user;
    }

    private void applyProfile(UserEntity user, StaffSaveRequest request) {
        user.setRealName(request.getRealName());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setTitle(request.getTitle());
        user.setPosition(request.getPosition());
        user.setDeptId(request.getDeptId());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
    }

    private void assertUsernameAvailable(String username) {
        long count = staffMapper.selectCount(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (count > 0) {
            throw new BusinessException(BaseErrorCodes.USERNAME_DUPLICATE);
        }
    }

    private void assertDeptExists(Long deptId) {
        if (deptId != null && departmentMapper.selectById(deptId) == null) {
            throw new BusinessException(BaseErrorCodes.DEPT_NOT_FOUND);
        }
    }

    private StaffVo toVo(UserEntity user) {
        StaffVo vo = new StaffVo();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setUserType(user.getUserType());
        vo.setRealName(user.getRealName());
        vo.setGender(user.getGender());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setTitle(user.getTitle());
        vo.setPosition(user.getPosition());
        vo.setDeptId(user.getDeptId());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        if (user.getDeptId() != null) {
            Department department = departmentMapper.selectById(user.getDeptId());
            vo.setDeptName(department == null ? null : department.getDeptName());
        }
        return vo;
    }
}
