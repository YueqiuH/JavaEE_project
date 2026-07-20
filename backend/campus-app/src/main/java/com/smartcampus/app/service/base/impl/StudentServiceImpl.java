package com.smartcampus.app.service.base.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.base.DepartmentMapper;
import com.smartcampus.app.dao.base.MajorMapper;
import com.smartcampus.app.dao.base.StudentMapper;
import com.smartcampus.app.service.base.BaseErrorCodes;
import com.smartcampus.app.service.base.StudentService;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.StudentQuery;
import com.smartcampus.contract.dto.StudentSaveRequest;
import com.smartcampus.contract.entity.Major;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.StudentStatsVo;
import com.smartcampus.contract.vo.StudentVo;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final DepartmentMapper departmentMapper;
    private final MajorMapper majorMapper;

    public StudentServiceImpl(StudentMapper studentMapper,
                              DepartmentMapper departmentMapper,
                              MajorMapper majorMapper) {
        this.studentMapper = studentMapper;
        this.departmentMapper = departmentMapper;
        this.majorMapper = majorMapper;
    }

    @Override
    public IPage<StudentVo> pageVo(StudentQuery query) {
        Page<StudentVo> page = new Page<>(query.getPage(), query.getSize());
        return studentMapper.selectVoPage(page, query);
    }

    @Override
    public StudentVo create(StudentSaveRequest request) {
        assertStudentNoAvailable(request.getStudentNo(), null);
        assertDeptMajorConsistent(request.getDeptId(), request.getMajorId());
        Student student = new Student();
        applyRequest(student, request);
        if (student.getStatus() == null) {
            student.setStatus(1);
        }
        studentMapper.insert(student);
        return toVo(student);
    }

    @Override
    public StudentVo update(Long studentId, StudentSaveRequest request) {
        Student student = requireStudent(studentId);
        assertStudentNoAvailable(request.getStudentNo(), studentId);
        assertDeptMajorConsistent(request.getDeptId(), request.getMajorId());
        applyRequest(student, request);
        studentMapper.updateById(student);
        return toVo(student);
    }

    @Override
    public void delete(Long studentId) {
        requireStudent(studentId);
        studentMapper.deleteById(studentId);
    }

    @Override
    public StudentStatsVo stats(Long deptId, Long majorId, Integer enrollYear) {
        StudentStatsVo stats = new StudentStatsVo();
        stats.setTotal(studentMapper.countActive(deptId, majorId, enrollYear));
        // 穿透分组：全校→院系，选院系→专业，选专业→班级
        if (majorId != null) {
            stats.setGroupBy("class");
            stats.setByGroup(studentMapper.statsByClass(majorId, enrollYear));
        } else if (deptId != null) {
            stats.setGroupBy("major");
            stats.setByGroup(studentMapper.statsByMajor(deptId, enrollYear));
        } else {
            stats.setGroupBy("dept");
            stats.setByGroup(studentMapper.statsByDept(enrollYear));
        }
        stats.setByEnrollYear(studentMapper.statsByEnrollYear(deptId, majorId));
        stats.setByGender(studentMapper.statsByGender(deptId, majorId, enrollYear));
        stats.setByOrigin(studentMapper.statsByOrigin(deptId, majorId, enrollYear));
        stats.setByStatus(studentMapper.statsByStatus(deptId, majorId, enrollYear));
        stats.setCoursePreference(studentMapper.statsCoursePreference(deptId, majorId, enrollYear));
        return stats;
    }

    @Override
    public StudentVo getByStudentNo(Long studentNo) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo));
        if (student == null) throw new BusinessException(BaseErrorCodes.STUDENT_NOT_FOUND);
        return toVo(student);
    }

    private Student requireStudent(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(BaseErrorCodes.STUDENT_NOT_FOUND);
        }
        return student;
    }

    private void applyRequest(Student student, StudentSaveRequest request) {
        student.setStudentNo(request.getStudentNo());
        student.setStudentName(request.getStudentName());
        student.setGender(request.getGender());
        student.setStudentBirth(request.getStudentBirth());
        student.setStudentAge(request.getStudentAge());
        student.setStudentAddress(request.getStudentAddress());
        student.setOriginPlace(request.getOriginPlace());
        student.setClassName(request.getClassName());
        student.setEnrollYear(request.getEnrollYear());
        student.setDeptId(request.getDeptId());
        student.setMajorId(request.getMajorId());
        if (request.getStatus() != null) {
            student.setStatus(request.getStatus());
        }
    }

    private StudentVo toVo(Student student) {
        StudentVo vo = new StudentVo();
        vo.setStudentId(student.getStudentId());
        vo.setStudentNo(student.getStudentNo());
        vo.setStudentName(student.getStudentName());
        vo.setGender(student.getGender());
        vo.setStudentBirth(student.getStudentBirth());
        vo.setStudentAge(student.getStudentAge());
        vo.setStudentAddress(student.getStudentAddress());
        vo.setOriginPlace(student.getOriginPlace());
        vo.setClassName(student.getClassName());
        vo.setEnrollYear(student.getEnrollYear());
        vo.setStatus(student.getStatus());
        vo.setDeptId(student.getDeptId());
        vo.setMajorId(student.getMajorId());
        if (student.getDeptId() != null) {
            var dept = departmentMapper.selectById(student.getDeptId());
            vo.setDeptName(dept != null ? dept.getDeptName() : null);
        }
        if (student.getMajorId() != null) {
            var major = majorMapper.selectById(student.getMajorId());
            vo.setMajorName(major != null ? major.getMajorName() : null);
        }
        return vo;
    }

    /** 校验学号未被其他学生占用 */
    private void assertStudentNoAvailable(Long studentNo, Long excludeStudentId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo);
        if (excludeStudentId != null) {
            wrapper.ne(Student::getStudentId, excludeStudentId);
        }
        if (studentMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(GlobalErrorCodeConstants.STUDENT_NO_ERROR);
        }
    }

    /** 校验院系存在、专业存在且属于该院系 */
    private void assertDeptMajorConsistent(Long deptId, Long majorId) {
        if (deptId != null && departmentMapper.selectById(deptId) == null) {
            throw new BusinessException(BaseErrorCodes.DEPT_NOT_FOUND);
        }
        if (majorId != null) {
            Major major = majorMapper.selectById(majorId);
            if (major == null) {
                throw new BusinessException(BaseErrorCodes.MAJOR_NOT_FOUND);
            }
            if (deptId != null && !deptId.equals(major.getDeptId())) {
                throw new BusinessException(BaseErrorCodes.MAJOR_DEPT_MISMATCH);
            }
        }
    }
}
