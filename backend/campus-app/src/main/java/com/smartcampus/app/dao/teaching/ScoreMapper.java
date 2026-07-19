package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.ScoreEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ScoreMapper extends BaseMapper<ScoreEntity> {

    /** 根据学生ID查询所有成绩（含课程名） */
    List<Map<String, Object>> selectByStudentId(@Param("studentId") Long studentId);

    /** 根据课程ID和学期查询该课程所有学生成绩（含学生信息） */
    List<Map<String, Object>> selectByCourseAndSemester(@Param("courseId") Long courseId,
                                                         @Param("semester") String semester);

    /** 教师查看自己执教的教学班列表（含课程信息、选课人数） */
    List<Map<String, Object>> selectTeacherClasses(@Param("teacherId") Long teacherId,
                                                    @Param("semester") String semester);

    /** 辅导员查看所辖班级学生学业预警（按不及格学分排序） */
    List<Map<String, Object>> selectCounselorWarnings(@Param("counselorId") Long counselorId,
                                                       @Param("semester") String semester);
}
