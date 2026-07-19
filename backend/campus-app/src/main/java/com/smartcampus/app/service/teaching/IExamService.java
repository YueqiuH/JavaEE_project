package com.smartcampus.app.service.teaching;

import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Exam;

public interface IExamService {
    /** 教务发布考试 */
    CommonResult createExam(Exam exam);

    /** 第16周：提取选课数据，按 course_code 统一编排考试 */
    CommonResult scheduleExams(String semester);

    /** 为考试分配考场并按学号递增编排座位（容量折半） */
    CommonResult assignRoomsAndSeats(Long examId);

    /** 一键为所有考试自动分配考场 */
    CommonResult assignAllRooms(String semester);

    /** 指派监考教师（含冲突校验） */
    CommonResult assignInvigilators(Long examId, Long classroomId, Long mainTeacherId, Long subTeacherId);

    /** 一键为所有考试自动指派监考教师 */
    CommonResult assignAllInvigilators(String semester);

    /** 查询考试列表 */
    CommonResult listExams(String semester, String examType);

    /** 查看某考试的学生名单（按学号升序） */
    CommonResult getExamStudents(Long examId);

    /** 查看教师监考安排 */
    CommonResult getTeacherInvigilations(Long teacherId, String semester);

    /** 补考/缓考预报名 */
    CommonResult applyResit(Long studentId, Long courseId, String semester, String applyType);

    /** 成绩达标自动撤销补考报名（第20周内，score>=60则撤销） */
    CommonResult autoRevokeIfPassed(Long studentId, Long courseId, String semester);

    /** 第20周日24:00 冻结所有补考名单 */
    CommonResult freezeResitList(String semester);

    /** 查询学生补考报名状态 */
    CommonResult getStudentResitStatus(Long studentId, String semester);

    /** 冲突校验：教师此处是否可监考 */
    CommonResult validateInvigilation(Long examId, Long teacherId);

    /** 查询学生的考试安排 */
    CommonResult getStudentExams(Long studentId, String semester);
}
