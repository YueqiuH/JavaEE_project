package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.IExamService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Exam;
import com.smartcampus.contract.entity.ResitApply;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teaching/exam")
@Tag(name = "考试管理", description = "考试发布、查询与补考报名")
public class ExamController {

    @Autowired private IExamService examService;

    @PostMapping("/create")
    @Operation(summary = "发布考试")
    public CommonResult create(@RequestBody Exam exam) {
        return examService.createExam(exam);
    }

    @GetMapping("/list")
    @Operation(summary = "考试列表")
    public CommonResult list(@RequestParam(required = false) String semester,
                             @RequestParam(required = false) Long courseId) {
        return examService.listExams(semester, courseId);
    }

    @PostMapping("/resit/apply")
    @Operation(summary = "补考报名")
    public CommonResult applyResit(@RequestBody ResitApply apply) {
        return examService.applyResit(apply);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "学生考试")
    public CommonResult studentExams(@PathVariable Long studentId, @RequestParam String semester) {
        return examService.getStudentExams(studentId, semester);
    }
}
