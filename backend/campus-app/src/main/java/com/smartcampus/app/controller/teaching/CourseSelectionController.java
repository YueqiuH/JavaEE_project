package com.smartcampus.app.controller.teaching;

import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.CourseCapacity;
import com.smartcampus.contract.entity.CourseSelection;
import com.smartcampus.app.service.teaching.ICourseSelectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/teaching/selection")
@Tag(name = "选课与容量控制", description = "用于处理学生选课、退选、容量管理的业务")
public class CourseSelectionController {

    private final ICourseSelectionService selectionService;
    private final Map<String, Boolean> selectionStatus = new ConcurrentHashMap<>();

    public CourseSelectionController(ICourseSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    @RequirePermission("teaching:read")
    @GetMapping("/status")
    @Operation(summary = "选课开放状态", description = "查询某学期选课是否开放")
    public CommonResult getStatus(@RequestParam String semester) {
        return CommonResult.success(Map.of("semester", semester, "open", selectionStatus.getOrDefault(semester, false)));
    }

    @RequirePermission("teaching:write")
    @PostMapping("/toggle")
    @Operation(summary = "开启/关闭选课", description = "教务处开关某学期选课入口")
    public CommonResult toggleSelection(@RequestBody Map<String, Object> body) {
        String semester = (String) body.get("semester");
        boolean open = Boolean.TRUE.equals(body.get("open"));
        selectionStatus.put(semester, open);
        return CommonResult.success(Map.of("semester", semester, "open", open));
    }

    /**
     * 学生选课
     */
    @RequirePermission("teaching:write")
    @PostMapping("/select")
    @Operation(summary = "学生选课", description = "学生选择课程，系统自动校验容量、重复、时间冲突")
    public CommonResult selectCourse(@RequestBody CourseSelection selection) {
        return selectionService.selectCourse(
            selection.getStudentId(),
            selection.getCourseId(),
            selection.getSemester()
        );
    }

    /**
     * 学生退选
     */
    @RequirePermission("teaching:write")
    @PostMapping("/drop")
    @Operation(summary = "学生退选", description = "学生退选已选课程，释放课程容量")
    public CommonResult dropCourse(@RequestBody CourseSelection selection) {
        return selectionService.dropCourse(
            selection.getStudentId(),
            selection.getCourseId(),
            selection.getSemester()
        );
    }

    /**
     * 我的选课列表
     */
    @RequirePermission("teaching:read")
    @GetMapping("/my/{studentId}")
    @Operation(summary = "查看我的选课", description = "学生查看自己的选课列表")
    public CommonResult getMySelection(@PathVariable Long studentId,
                                        @RequestParam String semester) {
        Long currentUserId = CurrentUserContext.require().userId();
        if (!currentUserId.equals(studentId)) {
            return CommonResult.error(403, "无权访问其他用户的数据");
        }
        return selectionService.getMySelection(studentId, semester);
    }

    /**
     * 教师/教务查看选课学生名单
     */
    @RequirePermission("teaching:read")
    @GetMapping("/student-list/{courseId}")
    @Operation(summary = "查看选课学生名单", description = "教师/教务查看某门课程的选课学生名单")
    public CommonResult getStudentList(@PathVariable Long courseId,
                                        @RequestParam String semester) {
        return selectionService.getStudentListByCourse(courseId, semester);
    }

    /**
     * 教务调整课程容量
     */
    @RequirePermission("teaching:write")
    @PostMapping("/capacity/update")
    @Operation(summary = "调整课程容量", description = "教务人员动态调整课程的最大和最小选课人数")
    public CommonResult updateCapacity(@RequestBody CourseCapacity capacity) {
        return selectionService.updateCapacity(
            capacity.getCapacityId(),
            capacity.getMaxCapacity(),
            capacity.getMinCapacity()
        );
    }
}
