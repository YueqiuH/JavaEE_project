package com.smartcampus.app.controller.base;

import com.smartcampus.app.service.base.StudentService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.StudentQuery;
import com.smartcampus.contract.dto.StudentSaveRequest;
import com.smartcampus.app.service.base.ExcelExportService;
import com.smartcampus.app.service.base.ExcelImportService;
import com.smartcampus.contract.vo.StudentStatsVo;
import com.smartcampus.contract.vo.StudentVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/base/students")
@Tag(name = "基础数据 - 学生信息库")
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    public StudentController(StudentService studentService, ExcelExportService excelExportService,
                             ExcelImportService excelImportService) {
        this.studentService = studentService;
        this.excelExportService = excelExportService;
        this.excelImportService = excelImportService;
    }

    @GetMapping("/by-no/{studentNo}")
    @Operation(summary = "按学号查询学生详情（含院系专业班级）")
    public CommonResult<StudentVo> getByNo(@PathVariable Long studentNo) {
        return CommonResult.success(studentService.getByStudentNo(studentNo));
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
    public CommonResult<StudentVo> create(@Valid @RequestBody StudentSaveRequest request) {
        return CommonResult.success(studentService.create(request));
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "修改学生档案", description = "错误示例：404103 学生不存在 / 409002 学号重复")
    @RequirePermission("base:write")
    public CommonResult<StudentVo> update(@PathVariable Long studentId,
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

    @GetMapping("/template")
    @Operation(summary = "下载学生导入模板")
    @RequirePermission("base:read")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        excelImportService.writeTemplate(response, "学生导入模板.xlsx",
                new String[]{"学号*", "姓名*", "性别(1男2女)", "出生日期(yyyy-MM-dd)", "院系ID", "专业ID", "班级", "入学年份", "生源地(省份)"},
                new Object[]{2025100001L, "张三", 1, "2007-06-15", 1, 1, "软件2501", 2025, "山东"}
        );
    }

    @PostMapping("/import")
    @Operation(summary = "批量导入学生（Excel）", description = "上传 .xlsx，首行为表头，返回成功/失败数")
    @RequirePermission("base:write")
    public CommonResult<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) throws IOException {
        var rows = excelImportService.parse(file);
        int success = 0, fail = 0;
        StringBuilder errors = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            try {
                var r = rows.get(i);
                var req = new com.smartcampus.contract.dto.StudentSaveRequest();
                String noStr = String.valueOf(r.getOrDefault("学号*", ""));
                req.setStudentNo(Long.parseLong(noStr));
                req.setStudentName(String.valueOf(r.getOrDefault("姓名*", "")));
                String g = String.valueOf(r.getOrDefault("性别(1男2女)", "1"));
                req.setGender("2".equals(g) ? 2 : 1);
                String birthStr = String.valueOf(r.getOrDefault("出生日期(yyyy-MM-dd)", "")).trim();
                if (!birthStr.isEmpty() && !"null".equals(birthStr)) req.setStudentBirth(java.time.LocalDate.parse(birthStr));
                String dept = String.valueOf(r.getOrDefault("院系ID", "0"));
                if (!dept.isEmpty() && !"0".equals(dept) && !"null".equals(dept)) req.setDeptId(Long.parseLong(dept));
                String major = String.valueOf(r.getOrDefault("专业ID", "0"));
                if (!major.isEmpty() && !"0".equals(major) && !"null".equals(major)) req.setMajorId(Long.parseLong(major));
                String year = String.valueOf(r.getOrDefault("入学年份", "0"));
                if (!year.isEmpty() && !"0".equals(year) && !"null".equals(year)) req.setEnrollYear(Integer.parseInt(year));
                String cls = String.valueOf(r.getOrDefault("班级", ""));
                if (!cls.isEmpty() && !"null".equals(cls)) req.setClassName(cls);
                String origin = String.valueOf(r.getOrDefault("生源地(省份)", ""));
                if (!origin.isEmpty() && !"null".equals(origin)) req.setOriginPlace(origin);
                String phone = String.valueOf(r.getOrDefault("手机号", "")).trim();
                if (!phone.isEmpty() && !"null".equals(phone)) req.setPhone(phone);
                req.setStatus(1);
                studentService.create(req);
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
    @Operation(summary = "导出学生档案 Excel")
    @RequirePermission("base:read")
    public void export(HttpServletResponse response,
                       @Parameter(description = "院系ID") @RequestParam(required = false) Long deptId,
                       @Parameter(description = "入学年份") @RequestParam(required = false) Integer enrollYear) throws IOException {
        var query = new com.smartcampus.contract.dto.StudentQuery();
        query.setPage(1);
        query.setSize(50000);
        query.setDeptId(deptId);
        query.setEnrollYear(enrollYear);
        var page = studentService.pageVo(query);
        List<ExcelExportService.ColumnDef> columns = List.of(
                new ExcelExportService.ColumnDef("学号", "studentNo"),
                new ExcelExportService.ColumnDef("姓名", "studentName"),
                new ExcelExportService.ColumnDef("性别", "genderText"),
                new ExcelExportService.ColumnDef("院系", "deptName"),
                new ExcelExportService.ColumnDef("专业", "majorName"),
                new ExcelExportService.ColumnDef("班级", "className"),
                new ExcelExportService.ColumnDef("入学年份", "enrollYear"),
                new ExcelExportService.ColumnDef("生源地", "originPlace"),
                new ExcelExportService.ColumnDef("学籍状态", "statusText")
        );
        List<Map<String, Object>> rows = page.getRecords().stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("studentNo", v.getStudentNo());
            m.put("studentName", v.getStudentName());
            m.put("genderText", v.getGender() == null ? "" : v.getGender() == 1 ? "男" : "女");
            m.put("deptName", v.getDeptName());
            m.put("majorName", v.getMajorName());
            m.put("className", v.getClassName());
            m.put("enrollYear", v.getEnrollYear() != null ? v.getEnrollYear() + "级" : "");
            m.put("originPlace", v.getOriginPlace());
            m.put("statusText", statusText(v.getStatus()));
            return m;
        }).toList();
        excelExportService.export(response, "学生档案.xlsx", columns, rows);
    }

    private static String statusText(Integer status) {
        return switch (status == null ? -1 : status) {
            case 1 -> "在读";
            case 2 -> "休学";
            case 3 -> "毕业";
            case 0 -> "退学";
            default -> "";
        };
    }
}
