package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.ICourseService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.auth.permission.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.smartcampus.contract.entity.Course;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/teaching/course")
@Tag(name = "课程信息", description = "用于查询课程列表及详细信息")
public class CourseController {

    private final ICourseService courseService;

    public CourseController(ICourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/list")
    @RequirePermission("teaching:read")
    @Operation(summary = "课程列表", description = "查询某学期全部课程，含排课时间、教室、容量、教师信息")
    public CommonResult getCourseList(@RequestParam String semester) {
        List<Map<String, Object>> courseList = courseService.listCoursesWithDetails(semester);
        return CommonResult.success(courseList);
    }

    @PostMapping("/add")
    @RequirePermission("teaching:write")
    @Operation(summary = "新增/更新课程", description = "新增课程；若传入courseId则更新已有课程（如恢复停开）")
    public CommonResult addCourse(@RequestBody Course course) {
        courseService.saveCourse(course);
        return CommonResult.success();
    }
}
