package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.ExamMapper;
import com.smartcampus.app.dao.teaching.ResitApplyMapper;
import com.smartcampus.app.dao.teaching.ScoreMapper;
import com.smartcampus.app.service.teaching.IExamService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Exam;
import com.smartcampus.contract.entity.ResitApply;
import com.smartcampus.contract.entity.ScoreEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired private ExamMapper examMapper;
    @Autowired private ResitApplyMapper resitApplyMapper;
    @Autowired private ScoreMapper scoreMapper;

    @Override
    public CommonResult createExam(Exam exam) {
        if (exam.getExamDate() != null && exam.getStartTime() != null && exam.getEndTime() != null) {
            if (exam.getEndTime().before(exam.getStartTime())) {
                return CommonResult.error(930, "结束时间不能早于开始时间");
            }
        }
        examMapper.insert(exam);
        return CommonResult.success();
    }

    @Override
    public CommonResult listExams(String semester, Long courseId) {
        LambdaQueryWrapper<Exam> w = new LambdaQueryWrapper<>();
        if (semester != null) w.eq(Exam::getSemester, semester);
        if (courseId != null) w.eq(Exam::getCourseId, courseId);
        return CommonResult.success(examMapper.selectList(w));
    }

    @Override
    public CommonResult applyResit(ResitApply apply) {
        // 校验该课程是否确实不及格
        LambdaQueryWrapper<ScoreEntity> sw = new LambdaQueryWrapper<>();
        sw.eq(ScoreEntity::getStudentId, apply.getStudentId())
          .eq(ScoreEntity::getCourseId, apply.getCourseId())
          .eq(ScoreEntity::getStatus, 0);
        if (scoreMapper.selectCount(sw) == 0) {
            return CommonResult.error(935, "该课程成绩已及格，无需补考");
        }
        // 是否已有进行中的申请
        LambdaQueryWrapper<ResitApply> aw = new LambdaQueryWrapper<>();
        aw.eq(ResitApply::getStudentId, apply.getStudentId())
          .eq(ResitApply::getCourseId, apply.getCourseId())
          .in(ResitApply::getStatus, 0, 1);
        if (resitApplyMapper.selectCount(aw) > 0) {
            return CommonResult.error(936, "已有该课程的补考申请，请勿重复提交");
        }
        apply.setStatus(0);
        apply.setApplyTime(new Date());
        resitApplyMapper.insert(apply);
        return CommonResult.success();
    }

    @Override
    public CommonResult getStudentExams(Long studentId, String semester) {
        // 通过选课表查询该学生的考试
        List<Exam> exams = examMapper.selectByStudent(studentId, semester);
        return CommonResult.success(exams);
    }
}
