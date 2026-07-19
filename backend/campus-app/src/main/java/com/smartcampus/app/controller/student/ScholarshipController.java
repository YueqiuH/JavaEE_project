package com.smartcampus.app.controller.student;

import com.smartcampus.app.service.student.ScholarshipService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.ScholarshipApplicationRequest;
import com.smartcampus.contract.dto.student.ScholarshipResultRequest;
import com.smartcampus.contract.dto.student.ScholarshipReviewRequest;
import com.smartcampus.contract.vo.student.ScholarshipApplicationVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Tag(name = "奖助贷资助与评审")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    public ScholarshipController(ScholarshipService scholarshipService) {
        this.scholarshipService = scholarshipService;
    }

    @GetMapping("/scholarships/mine")
    @RequirePermission("scholarship:application:read-self")
    @Operation(summary = "查询当前学生的奖助贷申请")
    public CommonResult<PageResult<ScholarshipApplicationVo>> listMine(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(scholarshipService.listMine(page, size, status));
    }

    @GetMapping("/scholarship-reviews")
    @RequirePermission("scholarship:review:read")
    @Operation(summary = "按辅导员初审或教务终审阶段查询评审队列")
    public CommonResult<PageResult<ScholarshipApplicationVo>> listForReview(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String status) {
        return CommonResult.success(scholarshipService.listForReview(page, size, stage, status));
    }

    @GetMapping("/scholarships/{id}")
    @Operation(summary = "查询申请详情")
    public CommonResult<ScholarshipApplicationVo> get(@PathVariable Long id) {
        return CommonResult.success(scholarshipService.getApplication(id));
    }

    @PostMapping("/scholarships")
    @RequirePermission("scholarship:application:create")
    @Operation(summary = "新建奖助贷申请草稿")
    public CommonResult<ScholarshipApplicationVo> create(@Valid @RequestBody ScholarshipApplicationRequest request) {
        return CommonResult.success(scholarshipService.create(request));
    }

    @PutMapping("/scholarships/{id}")
    @RequirePermission("scholarship:application:update-self")
    @Operation(summary = "修改草稿或被退回的申请")
    public CommonResult<ScholarshipApplicationVo> update(
            @PathVariable Long id,
            @Valid @RequestBody ScholarshipApplicationRequest request) {
        return CommonResult.success(scholarshipService.update(id, request));
    }

    @PostMapping("/scholarships/{id}/submissions")
    @RequirePermission("scholarship:application:submit-self")
    @Operation(summary = "提交或重新提交申请")
    public CommonResult<ScholarshipApplicationVo> submit(@PathVariable Long id) {
        return CommonResult.success(scholarshipService.submit(id));
    }

    @DeleteMapping("/scholarships/{id}")
    @RequirePermission("scholarship:application:withdraw-self")
    @Operation(summary = "撤回申请")
    public CommonResult<ScholarshipApplicationVo> withdraw(@PathVariable Long id) {
        return CommonResult.success(scholarshipService.withdraw(id));
    }

    @PostMapping("/scholarships/{id}/reviews")
    @RequirePermission("scholarship:review:submit")
    @Operation(summary = "提交辅导员初审或教务终审结论")
    public CommonResult<ScholarshipApplicationVo> review(
            @PathVariable Long id,
            @Valid @RequestBody ScholarshipReviewRequest request) {
        return CommonResult.success(scholarshipService.review(id, request));
    }

    @GetMapping("/scholarship-results")
    @Operation(summary = "查询已生成的资助名单")
    public CommonResult<PageResult<ScholarshipApplicationVo>> listResults(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) long size) {
        return CommonResult.success(scholarshipService.listResults(page, size));
    }

    @PostMapping("/scholarship-results")
    @RequirePermission("scholarship:result:generate")
    @Operation(summary = "从已通过申请中生成资助名单")
    public CommonResult<List<ScholarshipApplicationVo>> generateResults(
            @Valid @RequestBody ScholarshipResultRequest request) {
        return CommonResult.success(scholarshipService.generateResults(request));
    }
}
