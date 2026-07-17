package com.smartcampus.app.dao.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartcampus.contract.entity.Enrollment;
import com.smartcampus.contract.vo.EnrollmentStatVo;
import com.smartcampus.contract.vo.EnrollmentVo;
import com.smartcampus.contract.vo.NameValueVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface EnrollmentMapper extends BaseMapper<Enrollment> {

    @Select("""
            SELECT e.enrollment_id, e.major_id, m.major_name, m.dept_id, d.dept_name,
                   e.year, e.plan_count, e.actual_count, e.report_rate
            FROM enrollment e
            LEFT JOIN major m      ON m.major_id = e.major_id
            LEFT JOIN department d ON d.dept_id = m.dept_id
            WHERE (#{year} IS NULL OR e.year = #{year})
              AND (#{deptId} IS NULL OR m.dept_id = #{deptId})
            ORDER BY e.year DESC, m.dept_id, e.major_id
            """)
    IPage<EnrollmentVo> selectVoPage(IPage<EnrollmentVo> page,
                                     @Param("year") Integer year,
                                     @Param("deptId") Long deptId);

    @Select("""
            SELECT d.dept_name AS label,
                   COALESCE(SUM(e.plan_count), 0)   AS plan_count,
                   COALESCE(SUM(e.actual_count), 0) AS actual_count
            FROM enrollment e
            JOIN major m      ON m.major_id = e.major_id
            JOIN department d ON d.dept_id = m.dept_id
            WHERE e.year = #{year}
            GROUP BY d.dept_id, d.dept_name
            ORDER BY plan_count DESC
            """)
    List<EnrollmentStatVo> statsByDept(@Param("year") Integer year);

    @Select("""
            SELECT e.year AS label,
                   COALESCE(SUM(e.plan_count), 0)   AS plan_count,
                   COALESCE(SUM(e.actual_count), 0) AS actual_count
            FROM enrollment e
            GROUP BY e.year
            ORDER BY e.year
            """)
    List<EnrollmentStatVo> statsTrend();

    @Select("""
            SELECT COALESCE(s.origin_place, '未知') AS name, COUNT(*) AS value
            FROM student s
            WHERE s.enroll_year = #{year}
            GROUP BY s.origin_place
            ORDER BY value DESC
            """)
    List<NameValueVo> originDistribution(@Param("year") Integer year);
}
