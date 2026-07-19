package com.smartcampus.app.dao.teaching;

import com.smartcampus.contract.entity.Exam;
import com.smartcampus.contract.entity.Invigilation;
import com.smartcampus.contract.entity.ExamRoom;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface ExamMapper extends BaseMapper<Exam> {
    List<Exam> selectByStudent(@Param("studentId") Long studentId, @Param("semester") String semester);

    List<Map<String, Object>> selectExamStudents(@Param("examId") Long examId);

    int insertExamRoom(ExamRoom examRoom);

    /** 删除某考试的所有考场分配 */
    int deleteExamRoomsByExamId(@Param("examId") Long examId);

    int insertInvigilation(Invigilation invigilation);

    /** 删除某考试的所有监考指派 */
    int deleteInvigilationsByExamId(@Param("examId") Long examId);

    /** 按教师ID+日期查监考次数 */
    long countInvigilationByTeacherAndDate(@Param("teacherId") Long teacherId, @Param("examDate") String examDate);

    /** 按教师ID查所有监考记录 */
    List<Map<String, Object>> selectInvigilationsByTeacher(@Param("teacherId") Long teacherId);
}
