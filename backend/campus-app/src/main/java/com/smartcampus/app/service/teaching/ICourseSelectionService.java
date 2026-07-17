package com.smartcampus.app.service.teaching;

import com.smartcampus.contract.entity.CourseSelection;
import com.smartcampus.common.result.CommonResult;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ICourseSelectionService extends IService<CourseSelection> {

    //学生选课（含三重校验：容量/重复/时间冲突）
    CommonResult selectCourse(Long studentId, Long courseId, String semester);

    //学生退选
    CommonResult dropCourse(Long studentId, Long courseId, String semester);

    //教务查看某课程的选课学生名单
    CommonResult getStudentListByCourse(Long courseId, String semester);

    //学生查看我的选课列表
    CommonResult getMySelection(Long studentId, String semester);

    //教务调整课程容量
    CommonResult updateCapacity(Long capacityId, Integer maxCapacity, Integer minCapacity);
}
