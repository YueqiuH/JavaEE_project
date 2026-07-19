package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.CourseSelection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CourseSelectionMapper extends BaseMapper<CourseSelection> {

    /**
     * 根据课程ID和学期查询选课学生名单（关联student和grade表）
     */
    List<Map<String, Object>> selectStudentListByCourse(@Param("courseId") Long courseId,
                                                         @Param("semester") String semester);

    /**
     * 根据学生ID查询已选课程（含课程名、教师名、教室名、上课时间）
     */
    List<Map<String, Object>> selectByStudentWithCourse(@Param("studentId") Long studentId,
                                                          @Param("semester") String semester);
}
