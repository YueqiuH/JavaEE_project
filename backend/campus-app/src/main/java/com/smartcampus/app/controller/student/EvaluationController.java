package com.smartcampus.app.controller.student;

import com.smartcampus.app.service.student.EvaluationService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.dto.student.EvaluationSubmissionRequest;
import com.smartcampus.contract.vo.student.EvaluationCourseDetailVo;
import com.smartcampus.contract.vo.student.EvaluationTaskVo;
import com.smartcampus.contract.vo.student.EvaluationTeacherOverviewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@Tag(name = "评教反馈与教学质量评估")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping("/evaluation-tasks/mine")
    @RequirePermission("evaluation:task:read-self")
    @Operation(summary = "查询当前学生的评教任务")
    public CommonResult<List<EvaluationTaskVo>> listMine() {
        return CommonResult.success(evaluationService.listMyTasks());
    }

    @PostMapping("/evaluation-tasks/{selectionId}/submissions")
    @RequirePermission("evaluation:submit-self")
    @Operation(summary = "匿名提交课程评教")
    public CommonResult<EvaluationTaskVo> submit(
            @PathVariable Long selectionId,
            @Valid @RequestBody EvaluationSubmissionRequest request) {
        return CommonResult.success(evaluationService.submit(selectionId, request));
    }

    @PostMapping("/evaluation-counselor-task/mine/submissions")
    @RequirePermission("evaluation:submit-self")
    @Operation(summary = "匿名提交对本人辅导员的评教")
    public CommonResult<EvaluationTaskVo> submitCounselor(
            @Valid @RequestBody EvaluationSubmissionRequest request) {
        return CommonResult.success(evaluationService.submitCounselor(request));
    }

    @GetMapping("/evaluation-results/mine")
    @Operation(summary = "按当前教师、辅导员或教务处的数据范围查询评教汇总")
    public CommonResult<EvaluationTeacherOverviewVo> getMyOverview() {
        return CommonResult.success(evaluationService.getMyOverview());
    }

    @GetMapping("/evaluation-results/mine/courses/{courseId}")
    @Operation(summary = "按当前账号数据范围查询指定课程的匿名评教详情")
    public CommonResult<EvaluationCourseDetailVo> getMyCourseDetail(
            @PathVariable Long courseId,
            @RequestParam @NotBlank @Size(max = 32) String semester,
            @RequestParam(required = false) Long teacherId) {
        return CommonResult.success(evaluationService.getMyCourseDetail(courseId, semester, teacherId));
    }

    @GetMapping("/evaluation-results/mine/counselors/{counselorId}")
    @Operation(summary = "查询辅导员匿名评教详情")
    public CommonResult<EvaluationCourseDetailVo> getCounselorDetail(
            @PathVariable Long counselorId,
            @RequestParam @NotBlank @Size(max = 32) String semester) {
        return CommonResult.success(evaluationService.getCounselorDetail(counselorId, semester));
    }
}
