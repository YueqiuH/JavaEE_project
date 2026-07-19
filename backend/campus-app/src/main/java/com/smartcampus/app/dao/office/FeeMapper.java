package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.vo.StudentFeeOverviewVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FeeMapper extends BaseMapper<Fee> {

    @Select("""
            <script>
            SELECT u.user_id AS student_id,
                   u.username AS student_no,
                   COALESCE(s.student_name, u.username) AS student_name,
                   COUNT(f.fee_id) AS bill_count,
                   COALESCE(SUM(f.amount), 0.00) AS payable_amount,
                   COALESCE(SUM(CASE WHEN f.status = 1 THEN f.amount ELSE 0 END), 0.00) AS paid_amount,
                   COALESCE(SUM(CASE WHEN f.status = 0 THEN f.amount ELSE 0 END), 0.00) AS unpaid_amount,
                   CASE
                       WHEN COUNT(f.fee_id) = 0 THEN '未出账'
                       WHEN SUM(CASE WHEN f.status = 0 THEN 1 ELSE 0 END) &gt; 0 THEN '欠费'
                       ELSE '已缴清'
                   END AS payment_status
            FROM `user` u
            LEFT JOIN student s ON CAST(s.student_no AS CHAR) = u.username
            LEFT JOIN fee f ON f.student_id = u.user_id
            WHERE u.user_type = 1 AND u.status = 1
            <if test="keyword != null and keyword != ''">
              AND (u.username LIKE CONCAT('%', #{keyword}, '%')
                   OR s.student_name LIKE CONCAT('%', #{keyword}, '%'))
            </if>
            GROUP BY u.user_id, u.username, s.student_name
            <if test="paymentStatus != null and paymentStatus != ''">
              HAVING
              <choose>
                <when test="paymentStatus == '欠费'">
                  SUM(CASE WHEN f.status = 0 THEN 1 ELSE 0 END) &gt; 0
                </when>
                <when test="paymentStatus == '已缴清'">
                  COUNT(f.fee_id) &gt; 0 AND SUM(CASE WHEN f.status = 0 THEN 1 ELSE 0 END) = 0
                </when>
                <otherwise>
                  COUNT(f.fee_id) = 0
                </otherwise>
              </choose>
            </if>
            ORDER BY unpaid_amount DESC, u.username
            </script>
            """)
    IPage<StudentFeeOverviewVo> selectStudentFeeOverview(Page<StudentFeeOverviewVo> page,
                                                          @Param("keyword") String keyword,
                                                          @Param("paymentStatus") String paymentStatus);
}
