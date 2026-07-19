package com.smartcampus.app.controller.teaching;

import com.smartcampus.app.dto.teaching.CourseSearchQueryDto;
import com.smartcampus.app.service.teaching.ICourseSearchService;
import com.smartcampus.common.result.CommonResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程搜索 Controller —— 供教师和学生使用。
 */
@RestController
@RequestMapping("/course")
@Tag(name = "课程搜索", description = "多维搜索课程——按课程名、课程代码、教师名模糊/精确匹配")
public class CourseSearchController {

    @Autowired
    private ICourseSearchService searchService;

    @PostMapping("/search")
    @Operation(summary = "搜索课程",
        description = "支持 searchType: course_name(模糊)、course_code(精确)、teacher_name(模糊)；"
                    + "可叠加 courseClassification 分类过滤；支持分页并按创建时间降序。")
    public CommonResult<IPage<?>> search(@RequestBody CourseSearchQueryDto query) {
        return searchService.search(query);
    }
}
