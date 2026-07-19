package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.MajorService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageParam;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.MajorSaveRequest;
import com.smartcampus.contract.entity.Major;
import com.smartcampus.contract.vo.MajorVo;
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
@RequestMapping("/api/v1/base/majors")
@Tag(name = "基础数据 - 专业管理")
@SecurityRequirement(name = "bearerAuth")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    @Operation(summary = "分页查询专业", description = "支持按院系过滤与名称/编号关键字检索，返回所属院系与在读学生数；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<MajorVo>> page(
            @Valid @ParameterObject PageParam pageParam,
            @Parameter(description = "院系ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "名称/编号关键字") @RequestParam(required = false) String keyword) {
        return CommonResult.successPageData(majorService.pageVo(pageParam, deptId, keyword));
    }

    @PostMapping
    @Operation(summary = "新增专业", description = "错误示例：404101 院系不存在 / 409103 专业编号已存在")
    @RequirePermission("base:write")
    public CommonResult<Major> create(@Valid @RequestBody MajorSaveRequest request) {
        return CommonResult.success(majorService.create(request));
    }

    @PutMapping("/{majorId}")
    @Operation(summary = "修改专业", description = "错误示例：404102 专业不存在 / 409103 专业编号已存在")
    @RequirePermission("base:write")
    public CommonResult<Major> update(@PathVariable Long majorId,
                                      @Valid @RequestBody MajorSaveRequest request) {
        return CommonResult.success(majorService.update(majorId, request));
    }

    @DeleteMapping("/{majorId}")
    @Operation(summary = "删除专业", description = "专业下仍有学生时拒绝删除；错误示例：409104 该专业下仍有学生，无法删除")
    @RequirePermission("base:write")
    public CommonResult<Void> delete(@PathVariable Long majorId) {
        majorService.delete(majorId);
        return CommonResult.success();
    }
}
