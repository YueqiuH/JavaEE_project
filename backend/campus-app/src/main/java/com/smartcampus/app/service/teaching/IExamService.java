package com.smartcampus.app.service.teaching;

import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Exam;
import com.smartcampus.contract.entity.ResitApply;

public interface IExamService {
    /** 教务发布考试 */
    CommonResult createExam(Exam exam);
    /** 查询考试列表 */
    CommonResult listExams(String semester, Long courseId);
    /** 补考/重修报名 */
    CommonResult applyResit(ResitApply apply);
    /** 查询学生考试 */
    CommonResult getStudentExams(Long studentId, String semester);
}
