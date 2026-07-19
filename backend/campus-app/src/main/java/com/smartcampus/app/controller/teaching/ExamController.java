package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.IExamService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Exam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teaching/exam")
@Tag(name = "考试管理", description = "统一排考、考场编排、监考指派、补考/缓考报名")
public class ExamController {

    @Autowired private IExamService examService;

    // ==================== 统一排考 ====================

    @PostMapping("/schedule")
    @Operation(summary = "第16周：按course_code统一编排考试")
    public CommonResult schedule(@RequestParam String semester) {
        return examService.scheduleExams(semester);
    }

    @PostMapping("/assign-all-rooms")
    @Operation(summary = "一键为所有考试自动分配考场")
    public CommonResult assignAllRooms(@RequestParam String semester) {
        return examService.assignAllRooms(semester);
    }

    @PostMapping("/assign-all-invigilators")
    @Operation(summary = "一键为所有考试自动指派监考")
    public CommonResult assignAllInvigilators(@RequestParam String semester) {
        return examService.assignAllInvigilators(semester);
    }

    @PostMapping("/assign-rooms/{examId}")
    @Operation(summary = "为考试分配考场并按学号编排座位（容量折半）")
    public CommonResult assignRooms(@PathVariable Long examId) {
        return examService.assignRoomsAndSeats(examId);
    }

    @PostMapping("/assign-invigilators")
    @Operation(summary = "指派监考教师（含冲突校验）")
    public CommonResult assignInvigilators(@RequestBody Map<String, Long> body) {
        return examService.assignInvigilators(
            body.get("examId"), body.get("classroomId"),
            body.get("mainTeacherId"), body.get("subTeacherId"));
    }

    // ==================== 查询 ====================

    @GetMapping("/list")
    @Operation(summary = "考试列表")
    public CommonResult list(@RequestParam(required = false) String semester,
                             @RequestParam(required = false) String examType) {
        return examService.listExams(semester, examType);
    }

    @GetMapping("/students/{examId}")
    @Operation(summary = "考试学生名单（按学号升序）")
    public CommonResult students(@PathVariable Long examId) {
        return examService.getExamStudents(examId);
    }

    @GetMapping("/invigilations/{teacherId}")
    @Operation(summary = "教师监考安排")
    public CommonResult invigilations(@PathVariable Long teacherId,
                                       @RequestParam String semester) {
        return examService.getTeacherInvigilations(teacherId, semester);
    }

    @PostMapping("/create")
    @Operation(summary = "教务手动创建考试")
    public CommonResult create(@RequestBody Exam exam) {
        return examService.createExam(exam);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "学生考试")
    public CommonResult studentExams(@PathVariable Long studentId,
                                      @RequestParam String semester) {
        return examService.getStudentExams(studentId, semester);
    }

    // ==================== 补考/缓考 ====================

    @PostMapping("/resit/apply")
    @Operation(summary = "学生补考/缓考预报名（第20周）")
    public CommonResult applyResit(@RequestBody Map<String, Object> body) {
        return examService.applyResit(
            Long.valueOf(body.get("studentId").toString()),
            Long.valueOf(body.get("courseId").toString()),
            (String) body.get("semester"),
            (String) body.get("applyType"));
    }

    @PostMapping("/resit/auto-revoke")
    @Operation(summary = "成绩达标自动撤销补考报名")
    public CommonResult autoRevoke(@RequestParam Long studentId,
                                    @RequestParam Long courseId,
                                    @RequestParam String semester) {
        return examService.autoRevokeIfPassed(studentId, courseId, semester);
    }

    @PostMapping("/resit/freeze")
    @Operation(summary = "第20周日24:00冻结补考名单")
    public CommonResult freezeResit(@RequestParam String semester) {
        return examService.freezeResitList(semester);
    }

    @GetMapping("/resit/status/{studentId}")
    @Operation(summary = "查询学生补考报名状态")
    public CommonResult resitStatus(@PathVariable Long studentId,
                                     @RequestParam String semester) {
        return examService.getStudentResitStatus(studentId, semester);
    }
}
