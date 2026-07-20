package com.smartcampus.app.service.teaching;

import com.smartcampus.contract.entity.Course;

import java.util.List;
import java.util.Map;

/**
 * Course information service.
 */
public interface ICourseService {

    /**
     * Query all courses for a semester, including schedule time, classroom, capacity, teacher info.
     */
    List<Map<String, Object>> listCoursesWithDetails(String semester);

    /**
     * Insert or update a course (updates if courseId already exists).
     */
    void saveCourse(Course course);
}
