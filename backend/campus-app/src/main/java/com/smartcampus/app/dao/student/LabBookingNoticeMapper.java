package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.LabBookingNotice;
import com.smartcampus.contract.vo.student.LabBookingNoticeVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface LabBookingNoticeMapper extends BaseMapper<LabBookingNotice> {

    @Select("""
            <script>
            SELECT n.notice_id, n.booking_id, b.booking_no, n.student_id, n.title,
                   n.content, n.is_read, n.created_at, n.read_at
            FROM lab_booking_notice n
            JOIN lab_booking b ON b.booking_id = n.booking_id
            WHERE n.student_id = #{studentId}
            <if test="isRead != null">AND n.is_read = #{isRead}</if>
            ORDER BY n.created_at DESC, n.notice_id DESC
            </script>
            """)
    IPage<LabBookingNoticeVo> selectNoticePage(
            Page<LabBookingNoticeVo> page,
            @Param("studentId") Long studentId,
            @Param("isRead") Integer isRead
    );

    @Select("""
            SELECT n.notice_id, n.booking_id, b.booking_no, n.student_id, n.title,
                   n.content, n.is_read, n.created_at, n.read_at
            FROM lab_booking_notice n
            JOIN lab_booking b ON b.booking_id = n.booking_id
            WHERE n.notice_id = #{id}
            """)
    LabBookingNoticeVo selectNoticeView(@Param("id") Long id);
}
