package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.Exam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ExamMapper extends BaseMapper<Exam> {
    /** 查询学生相关考试（关联选课表） */
    List<Exam> selectByStudent(@Param("studentId") Long studentId, @Param("semester") String semester);
}
