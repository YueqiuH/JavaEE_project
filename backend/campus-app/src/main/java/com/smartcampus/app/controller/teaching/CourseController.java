package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.common.result.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import com.smartcampus.contract.entity.CourseEntity;
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

    @Autowired
    CourseMapper courseMapper;

    @GetMapping("/list")
    @Operation(summary = "课程列表", description = "查询某学期全部课程，含排课时间、教室、容量、教师信息")
    public CommonResult getCourseList(@RequestParam String semester) {
        List<Map<String, Object>> courseList =
            courseMapper.selectCourseListWithDetails(semester);
        return CommonResult.success(courseList);
    }

    @PostMapping("/add")
    @Operation(summary = "新增/更新课程", description = "新增课程；若传入courseId则更新已有课程（如恢复停开）")
    public CommonResult addCourse(@RequestBody CourseEntity course) {
        if (course.getCourseId() != null) {
            CourseEntity exist = courseMapper.selectById(course.getCourseId());
            if (exist != null) {
                courseMapper.updateById(course);
                return CommonResult.success();
            }
        }
        courseMapper.insert(course);
        return CommonResult.success();
    }
}
