package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.entity.Department;
import com.smartcampus.contract.vo.DepartmentSummaryVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("""
            SELECT d.dept_id, d.dept_name, d.dept_code, d.description,
                   (SELECT COUNT(*) FROM major m
                     WHERE m.dept_id = d.dept_id)                    AS major_count,
                   (SELECT COUNT(*) FROM student s
                     WHERE s.dept_id = d.dept_id AND s.status = 1)   AS student_count
            FROM department d
            WHERE (#{keyword} IS NULL OR #{keyword} = ''
                   OR d.dept_name LIKE CONCAT('%', #{keyword}, '%')
                   OR d.dept_code LIKE CONCAT('%', #{keyword}, '%'))
            ORDER BY d.dept_id
            """)
    IPage<DepartmentSummaryVo> selectSummaryPage(IPage<DepartmentSummaryVo> page, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM major WHERE dept_id = #{deptId}")
    long countMajors(@Param("deptId") Long deptId);
}
