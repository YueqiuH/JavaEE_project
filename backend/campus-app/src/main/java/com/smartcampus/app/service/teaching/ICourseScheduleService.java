package com.smartcampus.app.service.teaching;

import com.smartcampus.app.dto.teaching.ScheduleDto;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Schedule;

/**
 * 排课管理 Service —— 封装排课CRUD + 冲突校验 + 规则校验。
 * 仅 TEACHER 角色可调用。
 */
public interface ICourseScheduleService {

    /**
     * 新增排课（含全量规则校验 + 数据库冲突检测）
     */
    CommonResult addSchedule(ScheduleDto dto);

    /**
     * 更新排课
     */
    CommonResult updateSchedule(ScheduleDto dto);

    /**
     * 删除单条排课
     */
    CommonResult deleteSchedule(Long scheduleId);

    /**
     * 一键停开课程：删除该课程在 schedule 和 course_selection 中的所有记录
     *
     * @param courseId 要停开的课程ID
     * @param semester 学期
     */
    CommonResult suspendCourse(Long courseId, String semester);

    /**
     * 查询指定学期的短周期课程占比（用于前端仪表盘展示）
     */
    CommonResult getShortCourseRatio(String semester);

    /**
     * 按教师查询课表
     */
    CommonResult getTeacherSchedule(Long teacherId, String semester);

    /**
     * 按学生查询课表（关联选课表）
     */
    CommonResult getStudentSchedule(Long studentId, String semester);

    /**
     * F4: 教师工作量查询——汇总指定学期每周总授课节数。
     */
    CommonResult getTeacherWorkload(Long teacherId, String semester);
}
