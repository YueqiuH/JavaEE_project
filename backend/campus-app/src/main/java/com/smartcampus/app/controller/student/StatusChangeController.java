package com.smartcampus.app.controller.student;

import com.smartcampus.app.service.student.StatusChangeService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.StatusChangeApplicationRequest;
import com.smartcampus.contract.dto.student.StatusChangeReviewRequest;
import com.smartcampus.contract.dto.student.StudentProfileUpdateRequest;
import com.smartcampus.contract.vo.student.MajorOptionVo;
import com.smartcampus.contract.vo.student.StatusChangeApplicationVo;
import com.smartcampus.contract.vo.student.StudentProfileVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@Tag(name = "学籍变动与信息审核")
public class StatusChangeController {

    private final StatusChangeService statusChangeService;

    public StatusChangeController(StatusChangeService statusChangeService) {
        this.statusChangeService = statusChangeService;
    }

    @GetMapping("/student-profile")
    @RequirePermission("status:profile:read-self")
    @Operation(summary = "查询当前学生个人资料")
    public CommonResult<StudentProfileVo> getProfile() {
        return CommonResult.success(statusChangeService.getProfile());
    }

    @PutMapping("/student-profile")
    @RequirePermission("status:profile:update-self")
    @Operation(summary = "修改当前学生非核心信息")
    public CommonResult<StudentProfileVo> updateProfile(@Valid @RequestBody StudentProfileUpdateRequest request) {
        return CommonResult.success(statusChangeService.updateProfile(request));
    }

    @GetMapping("/status-change-majors")
    @Operation(summary = "查询转专业可选专业")
    public CommonResult<List<MajorOptionVo>> listMajors() {
        return CommonResult.success(statusChangeService.listMajors());
    }

    @GetMapping("/status-changes/mine")
    @RequirePermission("status:change:read-self")
    @Operation(summary = "查询当前学生的学籍异动申请")
    public CommonResult<PageResult<StatusChangeApplicationVo>> listMine(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(statusChangeService.listMine(page, size, status));
    }

    @GetMapping("/status-change-reviews")
    @RequirePermission("status:review:read")
    @Operation(summary = "查询教师的学籍异动审核队列")
    public CommonResult<PageResult<StatusChangeApplicationVo>> listForReview(
            @RequestParam @Pattern(regexp = "COUNSELOR|ACADEMIC") String stage,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size) {
        return CommonResult.success(statusChangeService.listForReview(page, size, stage));
    }

    @GetMapping("/status-changes/{id}")
    @Operation(summary = "查询学籍异动申请详情")
    public CommonResult<StatusChangeApplicationVo> get(@PathVariable Long id) {
        return CommonResult.success(statusChangeService.getApplication(id));
    }

    @PostMapping("/status-changes")
    @RequirePermission("status:change:create")
    @Operation(summary = "新建学籍异动申请草稿")
    public CommonResult<StatusChangeApplicationVo> create(@Valid @RequestBody StatusChangeApplicationRequest request) {
        return CommonResult.success(statusChangeService.create(request));
    }

    @PutMapping("/status-changes/{id}")
    @RequirePermission("status:change:update-self")
    @Operation(summary = "修改学籍异动申请草稿")
    public CommonResult<StatusChangeApplicationVo> update(
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeApplicationRequest request) {
        return CommonResult.success(statusChangeService.update(id, request));
    }

    @PostMapping("/status-changes/{id}/submissions")
    @RequirePermission("status:change:submit-self")
    @Operation(summary = "提交学籍异动申请")
    public CommonResult<StatusChangeApplicationVo> submit(@PathVariable Long id) {
        return CommonResult.success(statusChangeService.submit(id));
    }

    @DeleteMapping("/status-changes/{id}")
    @RequirePermission("status:change:withdraw-self")
    @Operation(summary = "撤回学籍异动申请")
    public CommonResult<StatusChangeApplicationVo> withdraw(@PathVariable Long id) {
        return CommonResult.success(statusChangeService.withdraw(id));
    }

    @PostMapping("/status-changes/{id}/reviews")
    @RequirePermission("status:review:submit")
    @Operation(summary = "提交辅导员初审或教务复审结论")
    public CommonResult<StatusChangeApplicationVo> review(
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeReviewRequest request) {
        return CommonResult.success(statusChangeService.review(id, request));
    }
}
