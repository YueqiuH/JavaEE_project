package com.smartcampus.app.service.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.dto.StudentQuery;
import com.smartcampus.contract.dto.StudentSaveRequest;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.StudentStatsVo;
import com.smartcampus.contract.vo.StudentVo;

public interface StudentService {

    IPage<StudentVo> pageVo(StudentQuery query);

    StudentVo create(StudentSaveRequest request);

    StudentVo update(Long studentId, StudentSaveRequest request);

    void delete(Long studentId);

    /**
     * D3 多维统计。穿透规则：未传院系按院系分组；传院系按专业分组；传专业按班级分组。
     */
    StudentStatsVo stats(Long deptId, Long majorId, Integer enrollYear);

    /** 按学号查学生详情（含院系专业班级） */
    StudentVo getByStudentNo(Long studentNo);
}
