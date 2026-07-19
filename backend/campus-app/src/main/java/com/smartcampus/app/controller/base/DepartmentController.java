package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.DepartmentService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.DepartmentSaveRequest;
import com.smartcampus.contract.entity.Department;
import com.smartcampus.contract.vo.DepartmentSummaryVo;
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

@RestController
@RequestMapping("/api/v1/base/departments")
@Tag(name = "基础数据 - 院系管理")
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "分页查询院系", description = "支持按名称/编号关键字模糊检索，返回各院系专业数与在读学生数；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<DepartmentSummaryVo>> page(
            @Valid @ParameterObject PageParam pageParam,
            @Parameter(description = "名称/编号关键字") @RequestParam(required = false) String keyword) {
        return CommonResult.successPageData(departmentService.pageSummary(pageParam, keyword));
    }

    @GetMapping("/{deptId}")
    @Operation(summary = "查询院系详情", description = "错误示例：404101 院系不存在")
    @RequirePermission("base:read")
    public CommonResult<Department> get(@PathVariable Long deptId) {
        return CommonResult.success(departmentService.getById(deptId));
    }

    @PostMapping
    @Operation(summary = "新增院系", description = "错误示例：409101 院系编号已存在")
    @RequirePermission("base:write")
    public CommonResult<Department> create(@Valid @RequestBody DepartmentSaveRequest request) {
        return CommonResult.success(departmentService.create(request));
    }

    @PutMapping("/{deptId}")
    @Operation(summary = "修改院系", description = "错误示例：404101 院系不存在 / 409101 院系编号已存在")
    @RequirePermission("base:write")
    public CommonResult<Department> update(@PathVariable Long deptId,
                                           @Valid @RequestBody DepartmentSaveRequest request) {
        return CommonResult.success(departmentService.update(deptId, request));
    }

    @DeleteMapping("/{deptId}")
    @Operation(summary = "删除院系", description = "院系下仍有专业时拒绝删除；错误示例：409102 该院系下仍有专业，无法删除")
    @RequirePermission("base:write")
    public CommonResult<Void> delete(@PathVariable Long deptId) {
        departmentService.delete(deptId);
        return CommonResult.success();
    }
}
