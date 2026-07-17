package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 根据学生ID和学期查询课表（关联选课表）
     */
    List<Schedule> selectByStudent(@Param("studentId") Long studentId,
                                   @Param("semester") String semester);

    /**
     * 根据教师ID和学期查询课表
     */
    List<Schedule> selectByTeacher(@Param("teacherId") Long teacherId,
                                   @Param("semester") String semester);
}
