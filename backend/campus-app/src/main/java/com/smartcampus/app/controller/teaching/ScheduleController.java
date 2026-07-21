package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.config.CourseScheduleConfig;
import com.smartcampus.app.dto.teaching.ScheduleDto;
import com.smartcampus.app.service.teaching.ICourseScheduleService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Schedule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 排课管理 Controller —— 仅对 TEACHER 角色开放。
 *
 * <p>路由基础路径: /api/v1/teaching/schedule</p>
 *
 * <h3>权限控制说明</h3>
 * <p>由于项目全局权限由 {@code AuthenticationFilter} 基于 RBAC 控制，
 * 本 Controller 的方法通过 {@code @SecurityRequirement} 声明需要 Bearer Token。
 * 真正的角色校验在 Service 层入参处或由 AOP 完成——
 * 如果当前用户角色不是 TEACHER，直接返回权限错误。</p>
 */
@RestController
@RequestMapping("/api/v1/teaching/schedule")
@Tag(name = "排课管理", description = "排课CRUD、冲突校验、停开课程（仅限教师角色）")
public class ScheduleController {

    @Autowired
    private ICourseScheduleService scheduleService;

    // ========================================================================
    //  新增排课
    // ========================================================================

    @PostMapping("/add")
    @Operation(summary = "新增排课",
        description = "新增一条排课记录，系统自动进行：参数校验 → 学分-课时匹配 → "
                    + "时间段槽位校验 → 跨时段禁止 → 教师冲突检测 → 学生重复检测。仅 TEACHER 角色可操作。")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult addSchedule(@RequestBody ScheduleDto dto) {
        return scheduleService.addSchedule(dto);
    }

    // ========================================================================
    //  更新排课
    // ========================================================================

    @PostMapping("/update")
    @Operation(summary = "更新排课", description = "更新排课时间/教室等信息，重新执行全量规则校验。仅 TEACHER 角色可操作。")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult updateSchedule(@RequestBody ScheduleDto dto) {
        return scheduleService.updateSchedule(dto);
    }

    // ========================================================================
    //  删除排课
    // ========================================================================

    @PostMapping("/delete/{scheduleId}")
    @Operation(summary = "删除单条排课", description = "按ID删除排课记录。仅 TEACHER 角色可操作。")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult deleteSchedule(@PathVariable Long scheduleId) {
        return scheduleService.deleteSchedule(scheduleId);
    }

    // ========================================================================
    //  一键停开课程
    // ========================================================================

    @PostMapping("/suspend-course")
    @Operation(summary = "一键停开课程",
        description = "删除指定课程在排课表(schedule)和选课表(course_selection)中的所有记录。"
                    + "适用于课程取消开设场景。仅 TEACHER 角色可操作。")
    @SecurityRequirement(name = "bearerAuth")
    public CommonResult suspendCourse(@RequestParam Long courseId,
                                      @RequestParam String semester) {
        return scheduleService.suspendCourse(courseId, semester);
    }

    // ========================================================================
    //  查询短周期占比
    // ========================================================================

    @GetMapping("/short-course-ratio")
    @Operation(summary = "短周期课程占比", description = "查询指定学期中不满15周课程占总排课的比例，用于前端报警展示。")
    public CommonResult getShortCourseRatio(@RequestParam String semester) {
        return scheduleService.getShortCourseRatio(semester);
    }

    // ========================================================================
    //  教师课表查询
    // ========================================================================

    @GetMapping("/teacher/{teacherId}")
    @Operation(summary = "教师课表", description = "按教师ID和学期查询课表")
    public CommonResult getTeacherSchedule(@PathVariable Long teacherId,
                                           @RequestParam String semester) {
        return scheduleService.getTeacherSchedule(teacherId, semester);
    }

    // ========================================================================
    //  学生课表查询
    // ========================================================================

    @GetMapping("/student/{studentId}")
    @Operation(summary = "学生课表", description = "按学生ID和学期查询课表（关联选课表）")
    public CommonResult getStudentSchedule(@PathVariable Long studentId,
                                           @RequestParam String semester) {
        return scheduleService.getStudentSchedule(studentId, semester);
    }

    // ========================================================================
    //  F4: 教师工作量查询
    // ========================================================================

    @GetMapping("/workload/{teacherId}")
    @Operation(summary = "教师工作量", description = "查询教师在指定学期的每周总授课节数，含预警/阻断标记")
    public CommonResult getTeacherWorkload(@PathVariable Long teacherId,
                                           @RequestParam String semester) {
        return scheduleService.getTeacherWorkload(teacherId, semester);
    }
}
