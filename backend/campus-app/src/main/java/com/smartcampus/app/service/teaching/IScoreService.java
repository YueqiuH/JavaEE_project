package com.smartcampus.app.service.teaching;

import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.ScoreEntity;

import java.util.Map;

public interface IScoreService {
    /** 教师录入/暂存/发布成绩 */
    CommonResult inputScore(ScoreEntity score);

    /** 教师暂存草稿（不发布） */
    CommonResult saveDraft(ScoreEntity score);

    /** 教师一键发布某教学班全部草稿成绩 */
    CommonResult publishScores(Long scheduleId, Long teacherId, String semester);

    /** 学生成绩单（含GPA汇总、历史重修覆盖） */
    CommonResult getStudentReport(Long studentId, String semester);

    /** 教师查看某教学班成绩列表 */
    CommonResult getCourseScores(Long courseId, String semester);

    /** 教师查看自己执教的教学班列表 */
    CommonResult getTeacherClasses(Long teacherId, String semester);

    /** 辅导员查看所辖班级学生成绩预警 */
    CommonResult getCounselorWarnings(Long counselorId, String semester);

    /** 辅导员查看某学生完整成绩画像 */
    CommonResult getStudentFullProfile(Long studentId);

    /** 管理员例外修改（21周后） */
    CommonResult adminModifyScore(Long scoreId, Integer newScore, Long adminId, String reason, String docNo);

    /** 管理员查看修改日志 */
    CommonResult getModificationLogs(Long scoreId);

    /** 获取当前教学周 */
    int getCurrentWeek();

    /** 获取时间窗口状态 */
    Map<String, Object> getTimeWindowStatus();
}
