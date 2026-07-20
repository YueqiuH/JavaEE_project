package com.smartcampus.app.service.teaching.impl;

import com.smartcampus.app.dao.teaching.CourseMapper;
import com.smartcampus.app.service.teaching.ICourseService;
import com.smartcampus.contract.entity.Course;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourseServiceImpl implements ICourseService {

    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseMapper courseMapper) {
        this.courseMapper = courseMapper;
    }

    @Override
    public List<Map<String, Object>> listCoursesWithDetails(String semester) {
        return courseMapper.selectCourseListWithDetails(semester);
    }

    @Override
    public void saveCourse(Course course) {
        if (course.getCourseId() != null) {
            Course exist = courseMapper.selectById(course.getCourseId());
            if (exist != null) {
                courseMapper.updateById(course);
                return;
            }
        }
        courseMapper.insert(course);
    }
}
