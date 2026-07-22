package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.service.teaching.ICourseService;
import com.smartcampus.auth.repository.AuthUserMapper;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.contract.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/teaching/course")
@Tag(name = "课程信息", description = "用于查询课程列表及详细信息")
public class CourseController {

    private final ICourseService courseService;
    private final AuthUserMapper authUserMapper;

    public CourseController(ICourseService courseService, AuthUserMapper authUserMapper) {
        this.courseService = courseService;
        this.authUserMapper = authUserMapper;
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

    @GetMapping("/teachers")
    @RequirePermission("teaching:read")
    @Operation(summary = "教师列表", description = "获取所有教师(user_type=2)的简要列表")
    public CommonResult listTeachers() {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.eq(User::getUserType, 2).eq(User::getStatus, 1);
        List<User> teachers = authUserMapper.selectList(w);
        List<Map<String, Object>> result = teachers.stream().map(t -> Map.<String, Object>of(
                "userId", t.getUserId(),
                "realName", t.getRealName() != null ? t.getRealName() : t.getUsername(),
                "username", t.getUsername(),
                "title", t.getTitle() != null ? t.getTitle() : "",
                "deptId", t.getDeptId() != null ? t.getDeptId() : 0
        )).collect(Collectors.toList());
        return CommonResult.success(result);
    }
}
