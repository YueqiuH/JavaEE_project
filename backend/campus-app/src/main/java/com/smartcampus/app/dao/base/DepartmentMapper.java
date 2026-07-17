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
                   COUNT(DISTINCT m.major_id)   AS major_count,
                   COUNT(DISTINCT s.student_id) AS student_count
            FROM department d
            LEFT JOIN major m   ON m.dept_id = d.dept_id
            LEFT JOIN student s ON s.dept_id = d.dept_id AND s.status = 1
            WHERE (#{keyword} IS NULL OR #{keyword} = ''
                   OR d.dept_name LIKE CONCAT('%', #{keyword}, '%')
                   OR d.dept_code LIKE CONCAT('%', #{keyword}, '%'))
            GROUP BY d.dept_id, d.dept_name, d.dept_code, d.description
            ORDER BY d.dept_id
            """)
    IPage<DepartmentSummaryVo> selectSummaryPage(IPage<DepartmentSummaryVo> page, @Param("keyword") String keyword);

    @Select("SELECT COUNT(*) FROM major WHERE dept_id = #{deptId}")
    long countMajors(@Param("deptId") Long deptId);
}
