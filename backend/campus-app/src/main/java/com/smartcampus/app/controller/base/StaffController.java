package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.StaffService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.StaffQuery;
import com.smartcampus.contract.dto.StaffSaveRequest;
import com.smartcampus.contract.vo.StaffVo;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/base/staffs")
@Tag(name = "基础数据 - 教职工信息库")
@SecurityRequirement(name = "bearerAuth")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    @Operation(summary = "多条件分页检索教职工档案",
            description = "支持工号/姓名/电话关键字、院系、人员类别组合检索；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<StaffVo>> page(@Valid @ParameterObject StaffQuery query) {
        return CommonResult.successPageData(staffService.pageVo(query));
    }

    @PostMapping
    @Operation(summary = "新增教职工档案",
            description = "同时开通登录账号（默认密码 123321）并按类别赋予角色；错误示例：409105 工号/账号已存在")
    @RequirePermission("base:write")
    public CommonResult<StaffVo> create(@Valid @RequestBody StaffSaveRequest request) {
        return CommonResult.success(staffService.create(request));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "修改教职工档案", description = "不修改工号与密码；错误示例：404104 教职工不存在")
    @RequirePermission("base:write")
    public CommonResult<StaffVo> update(@PathVariable Long userId,
                                        @Valid @RequestBody StaffSaveRequest request) {
        return CommonResult.success(staffService.update(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "停用教职工账号", description = "软删除：将账号置为停用状态，档案保留；错误示例：404104 教职工不存在")
    @RequirePermission("base:write")
    public CommonResult<Void> disable(@PathVariable Long userId) {
        staffService.disable(userId);
        return CommonResult.success();
    }
}
