package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.EnrollmentService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.EnrollmentSaveRequest;
import com.smartcampus.contract.entity.Enrollment;
import com.smartcampus.contract.vo.EnrollmentStatsVo;
import com.smartcampus.contract.vo.EnrollmentVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@RequestMapping("/api/v1/base/enrollments")
@Tag(name = "基础数据 - 招生与迎新统计")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @Operation(summary = "分页查询招生计划", description = "支持按年度/院系过滤；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<EnrollmentVo>> page(
            @Valid @ParameterObject PageParam pageParam,
            @Parameter(description = "年度") @RequestParam(required = false) Integer year,
            @Parameter(description = "院系ID") @RequestParam(required = false) Long deptId) {
        return CommonResult.successPageData(enrollmentService.pageVo(pageParam, year, deptId));
    }

    @GetMapping("/stats")
    @Operation(summary = "招生迎新统计",
            description = "返回指定年度（默认当年）的全校计划/报到汇总、各院系对比、历年趋势与新生生源地分布；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<EnrollmentStatsVo> stats(
            @Parameter(description = "统计年度，默认当年") @RequestParam(required = false) Integer year) {
        return CommonResult.success(enrollmentService.stats(year == null ? Year.now().getValue() : year));
    }

    @PostMapping
    @Operation(summary = "录入招生计划", description = "错误示例：409109 该专业该年度的招生计划已存在")
    @RequirePermission("base:write")
    public CommonResult<Enrollment> create(@Valid @RequestBody EnrollmentSaveRequest request) {
        return CommonResult.success(enrollmentService.create(request));
    }

    @PutMapping("/{enrollmentId}")
    @Operation(summary = "修改招生计划/更新报到人数", description = "报到率自动重新计算；错误示例：404108 招生计划不存在")
    @RequirePermission("base:write")
    public CommonResult<Enrollment> update(@PathVariable Long enrollmentId,
                                           @Valid @RequestBody EnrollmentSaveRequest request) {
        return CommonResult.success(enrollmentService.update(enrollmentId, request));
    }

    @DeleteMapping("/{enrollmentId}")
    @Operation(summary = "删除招生计划", description = "错误示例：404108 招生计划不存在")
    @RequirePermission("base:write")
    public CommonResult<Void> delete(@PathVariable Long enrollmentId) {
        enrollmentService.delete(enrollmentId);
        return CommonResult.success();
    }
}
