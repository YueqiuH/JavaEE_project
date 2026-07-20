package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.ExcelExportService;
import com.smartcampus.app.service.base.ExcelImportService;
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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    public StaffController(StaffService staffService, ExcelExportService excelExportService,
                           ExcelImportService excelImportService) {
        this.staffService = staffService;
        this.excelExportService = excelExportService;
        this.excelImportService = excelImportService;
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
            description = "同时开通登录账号（随机密码）并按类别赋予角色；错误示例：409105 工号/账号已存在")
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

    @GetMapping("/template")
    @Operation(summary = "下载教职工导入模板")
    @RequirePermission("base:read")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        excelImportService.writeTemplate(response, "教职工导入模板.xlsx",
                new String[]{"工号*", "姓名*", "类别(2教师3教职工)*", "性别(1男2女)", "电话", "邮箱", "职称", "职务", "院系ID"},
                new Object[]{"700010", "张三", 2, 1, "13800000001", "zhang@campus.edu", "讲师", "系主任", 1}
        );
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入教职工（Excel）", description = "上传 .xlsx，首行为表头，返回成功/失败数")
    @RequirePermission("base:write")
    public CommonResult<Map<String, Object>> importStaffs(@RequestParam("file") MultipartFile file) throws IOException {
        var rows = excelImportService.parse(file);
        int success = 0, fail = 0;
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            try {
                var r = rows.get(i);
                var req = new com.smartcampus.contract.dto.StaffSaveRequest();
                req.setUsername(String.valueOf(r.getOrDefault("工号*", "")));
                req.setRealName(String.valueOf(r.getOrDefault("姓名*", "")));
                req.setUserType(Integer.parseInt(String.valueOf(r.getOrDefault("类别(2教师3教职工)*", "2"))));
                String g = String.valueOf(r.getOrDefault("性别(1男2女)", "1"));
                req.setGender("2".equals(g) ? 2 : 1);
                String phone = String.valueOf(r.getOrDefault("电话", ""));
                if (!phone.isEmpty() && !"null".equals(phone)) req.setPhone(phone);
                String email = String.valueOf(r.getOrDefault("邮箱", ""));
                if (!email.isEmpty() && !"null".equals(email)) req.setEmail(email);
                String title = String.valueOf(r.getOrDefault("职称", ""));
                if (!title.isEmpty() && !"null".equals(title)) req.setTitle(title);
                String pos = String.valueOf(r.getOrDefault("职务", ""));
                if (!pos.isEmpty() && !"null".equals(pos)) req.setPosition(pos);
                String dept = String.valueOf(r.getOrDefault("院系ID", "0"));
                if (!dept.isEmpty() && !"0".equals(dept) && !"null".equals(dept)) req.setDeptId(Long.parseLong(dept));
                staffService.create(req);
                success++;
            } catch (Exception e) {
                fail++;
                errors.append("第").append(i + 2).append("行: ").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", rows.size()); result.put("success", success);
        result.put("fail", fail); result.put("errors", errors.toString());
        return CommonResult.success(result);
    }

    @GetMapping("/export")
    @Operation(summary = "导出教职工档案 Excel")
    @RequirePermission("base:read")
    public void export(HttpServletResponse response) throws IOException {
        var query = new com.smartcampus.contract.dto.StaffQuery();
        query.setPage(1);
        query.setSize(50000);
        var page = staffService.pageVo(query);
        List<ExcelExportService.ColumnDef> columns = List.of(
                new ExcelExportService.ColumnDef("工号", "username"),
                new ExcelExportService.ColumnDef("姓名", "realName"),
                new ExcelExportService.ColumnDef("性别", "genderText"),
                new ExcelExportService.ColumnDef("类别", "userTypeText"),
                new ExcelExportService.ColumnDef("院系", "deptName"),
                new ExcelExportService.ColumnDef("职称", "title"),
                new ExcelExportService.ColumnDef("职务", "position"),
                new ExcelExportService.ColumnDef("电话", "phone"),
                new ExcelExportService.ColumnDef("邮箱", "email"),
                new ExcelExportService.ColumnDef("状态", "statusText")
        );
        List<Map<String, Object>> rows = page.getRecords().stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("username", v.getUsername());
            m.put("realName", v.getRealName());
            m.put("genderText", v.getGender() == null ? "" : v.getGender() == 1 ? "男" : "女");
            m.put("userTypeText", v.getUserType() == null ? "" : v.getUserType() == 2 ? "教师" : "教职工");
            m.put("deptName", v.getDeptName());
            m.put("title", v.getTitle());
            m.put("position", v.getPosition());
            m.put("phone", v.getPhone());
            m.put("email", v.getEmail());
            m.put("statusText", v.getStatus() == null ? "" : v.getStatus() == 1 ? "启用" : "停用");
            return m;
        }).toList();
        excelExportService.export(response, "教职工档案.xlsx", columns, rows);
    }
}
