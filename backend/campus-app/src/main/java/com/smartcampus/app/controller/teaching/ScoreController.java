package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.IScoreService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Score;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/teaching/score")
@Tag(name = "成绩管理", description = "成绩录入、查询、绩点计算与学业预警（含角色权限控制）")
public class ScoreController {

    @Autowired private IScoreService scoreService;

    // ==================== 教师端 ====================

    @PostMapping("/input")
    @Operation(summary = "教师录入/修改成绩（含平时+期末分项）")
    public CommonResult input(@RequestBody Score score) {
        return scoreService.inputScore(score);
    }

    @PostMapping("/save-draft")
    @Operation(summary = "教师暂存草稿（学生不可见）")
    public CommonResult saveDraft(@RequestBody Score score) {
        return scoreService.saveDraft(score);
    }

    @PostMapping("/publish")
    @Operation(summary = "教师一键发布教学班全部草稿成绩")
    public CommonResult publish(@RequestParam Long scheduleId,
                                @RequestParam Long teacherId,
                                @RequestParam String semester) {
        return scoreService.publishScores(scheduleId, teacherId, semester);
    }

    @GetMapping("/teacher-classes")
    @Operation(summary = "教师查看自己执教的教学班列表")
    public CommonResult teacherClasses(@RequestParam Long teacherId,
                                       @RequestParam String semester) {
        return scoreService.getTeacherClasses(teacherId, semester);
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "教师查看某教学班成绩列表")
    public CommonResult courseScores(@PathVariable Long courseId,
                                     @RequestParam(required = false) String semester) {
        return scoreService.getCourseScores(courseId, semester);
    }

    // ==================== 学生端 ====================

    @GetMapping("/student/{studentId}")
    @Operation(summary = "学生成绩单（含GPA、重修覆盖）")
    public CommonResult studentReport(@PathVariable Long studentId,
                                      @RequestParam(required = false) String semester) {
        return scoreService.getStudentReport(studentId, semester);
    }

    // ==================== 辅导员端 ====================

    @GetMapping("/counselor/warnings")
    @Operation(summary = "辅导员查看所辖班级学业预警")
    public CommonResult counselorWarnings(@RequestParam Long counselorId,
                                          @RequestParam String semester) {
        return scoreService.getCounselorWarnings(counselorId, semester);
    }

    @GetMapping("/counselor/student-profile/{studentId}")
    @Operation(summary = "辅导员查看某学生完整成绩画像")
    public CommonResult studentProfile(@PathVariable Long studentId) {
        return scoreService.getStudentFullProfile(studentId);
    }

    // ==================== 管理员端 ====================

    @PostMapping("/admin/modify")
    @Operation(summary = "管理员例外修改成绩（21周后）")
    public CommonResult adminModify(@RequestBody Map<String, Object> body) {
        Long scoreId = Long.valueOf(body.get("scoreId").toString());
        Integer newScore = Integer.valueOf(body.get("newScore").toString());
        Long adminId = Long.valueOf(body.get("adminId").toString());
        String reason = (String) body.getOrDefault("reason", "");
        String docNo = (String) body.getOrDefault("docNo", "");
        return scoreService.adminModifyScore(scoreId, newScore, adminId, reason, docNo);
    }

    @GetMapping("/admin/logs/{scoreId}")
    @Operation(summary = "查看成绩修改日志")
    public CommonResult modificationLogs(@PathVariable Long scoreId) {
        return scoreService.getModificationLogs(scoreId);
    }

    // ==================== 公共 ====================

    @GetMapping("/time-window")
    @Operation(summary = "获取当前时间窗口状态")
    public CommonResult timeWindow() {
        return CommonResult.success(scoreService.getTimeWindowStatus());
    }

    @GetMapping("/current-week")
    @Operation(summary = "获取当前教学周")
    public CommonResult currentWeek() {
        return CommonResult.success(Map.of("week", scoreService.getCurrentWeek()));
    }
}
