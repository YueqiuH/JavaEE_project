package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.entity.Major;
import com.smartcampus.contract.vo.MajorVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface MajorMapper extends BaseMapper<Major> {

    @Select("""
            SELECT m.major_id, m.dept_id, d.dept_name, m.major_name, m.major_code, m.cultivation_plan,
                   (SELECT COUNT(*) FROM student s
                     WHERE s.major_id = m.major_id AND s.status = 1)   AS student_count
            FROM major m
            LEFT JOIN department d ON d.dept_id = m.dept_id
            WHERE (#{deptId} IS NULL OR m.dept_id = #{deptId})
              AND (#{keyword} IS NULL OR #{keyword} = ''
                   OR m.major_name LIKE CONCAT('%', #{keyword}, '%')
                   OR m.major_code LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY m.dept_id, m.major_id
            """)
    IPage<MajorVo> selectVoPage(IPage<MajorVo> page, @Param("deptId") Long deptId, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM student WHERE major_id = #{majorId}")
    long countStudents(@Param("majorId") Long majorId);
}
