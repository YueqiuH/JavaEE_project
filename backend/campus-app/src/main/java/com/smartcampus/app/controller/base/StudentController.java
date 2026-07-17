package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.StudentService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.StudentQuery;
import com.smartcampus.contract.dto.StudentSaveRequest;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.StudentStatsVo;
import com.smartcampus.contract.vo.StudentVo;
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
@RequestMapping("/api/v1/base/students")
@Tag(name = "基础数据 - 学生信息库")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "多条件分页检索学生档案",
            description = "支持姓名/学号关键字、院系、专业、入学年份、学籍状态组合检索；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<PageResult<StudentVo>> page(@Valid @ParameterObject StudentQuery query) {
        return CommonResult.successPageData(studentService.pageVo(query));
    }

    @GetMapping("/stats")
    @Operation(summary = "学生特征多维统计",
            description = "穿透规则：不传院系按院系分组，传院系按专业分组，传专业按班级分组；"
                    + "同时返回年级/性别/生源地/学籍状态分布与选课偏好 Top10；错误示例：401001 账号未登录")
    @RequirePermission("base:read")
    public CommonResult<StudentStatsVo> stats(
            @Parameter(description = "院系ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "专业ID") @RequestParam(required = false) Long majorId,
            @Parameter(description = "入学年份") @RequestParam(required = false) Integer enrollYear) {
        return CommonResult.success(studentService.stats(deptId, majorId, enrollYear));
    }

    @PostMapping
    @Operation(summary = "新增学生档案", description = "错误示例：409002 学号重复 / 409106 所选专业不属于所选院系")
    @RequirePermission("base:write")
    public CommonResult<StudentEntity> create(@Valid @RequestBody StudentSaveRequest request) {
        return CommonResult.success(studentService.create(request));
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "修改学生档案", description = "错误示例：404103 学生不存在 / 409002 学号重复")
    @RequirePermission("base:write")
    public CommonResult<StudentEntity> update(@PathVariable Long studentId,
                                              @Valid @RequestBody StudentSaveRequest request) {
        return CommonResult.success(studentService.update(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "删除学生档案", description = "错误示例：404103 学生不存在")
    @RequirePermission("base:write")
    public CommonResult<Void> delete(@PathVariable Long studentId) {
        studentService.delete(studentId);
        return CommonResult.success();
    }
}
