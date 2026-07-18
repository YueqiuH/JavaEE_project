package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.LabBooking;
import com.smartcampus.contract.vo.student.LabBookingVo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface LabBookingMapper extends BaseMapper<LabBooking> {

    @Select("""
            <script>
            SELECT b.booking_id, b.booking_no, b.lab_id, l.lab_name, l.location,
                   b.resource_id, r.resource_no, r.resource_name, r.resource_type,
                   b.student_id, st.student_no, st.student_name, b.booking_date,
                   b.start_period, b.end_period, b.purpose, b.status AS status_code,
                   b.create_time, b.cancelled_at, b.check_in_at, b.check_out_at,
                   b.expires_at, b.completed_at, b.updated_at
            FROM lab_booking b
            JOIN lab l ON l.lab_id = b.lab_id
            LEFT JOIN lab_resource r ON r.resource_id = b.resource_id
            JOIN student st ON st.student_id = b.student_id
            WHERE 1 = 1
            <if test="studentId != null">AND b.student_id = #{studentId}</if>
            <if test="managerId != null">AND l.manager_id = #{managerId}</if>
            <if test="status != null">AND b.status = #{status}</if>
            ORDER BY b.booking_date DESC, b.start_period DESC, b.booking_id DESC
            </script>
            """)
    IPage<LabBookingVo> selectBookingPage(
            Page<LabBookingVo> page,
            @Param("studentId") Long studentId,
            @Param("managerId") Long managerId,
            @Param("status") Integer status
    );

    @Select("""
            SELECT b.booking_id, b.booking_no, b.lab_id, l.lab_name, l.location,
                   b.resource_id, r.resource_no, r.resource_name, r.resource_type,
                   b.student_id, st.student_no, st.student_name, b.booking_date,
                   b.start_period, b.end_period, b.purpose, b.status AS status_code,
                   b.create_time, b.cancelled_at, b.check_in_at, b.check_out_at,
                   b.expires_at, b.completed_at, b.updated_at
            FROM lab_booking b
            JOIN lab l ON l.lab_id = b.lab_id
            LEFT JOIN lab_resource r ON r.resource_id = b.resource_id
            JOIN student st ON st.student_id = b.student_id
            WHERE b.booking_id = #{id}
            """)
    LabBookingVo selectBookingView(@Param("id") Long id);

    @Select("""
            SELECT COUNT(*) FROM lab_booking
            WHERE resource_id = #{resourceId} AND booking_date = #{bookingDate} AND status = 1
              AND start_period <= #{endPeriod} AND end_period >= #{startPeriod}
            """)
    int countResourceConflicts(
            @Param("resourceId") Long resourceId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );

    @Select("""
            SELECT COUNT(*) FROM lab_booking
            WHERE student_id = #{studentId} AND booking_date = #{bookingDate} AND status = 1
              AND start_period <= #{endPeriod} AND end_period >= #{startPeriod}
            """)
    int countStudentConflicts(
            @Param("studentId") Long studentId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );

    @Insert("""
            INSERT INTO lab_booking_period (booking_id, resource_id, student_id, booking_date, period_no)
            VALUES (#{bookingId}, #{resourceId}, #{studentId}, #{bookingDate}, #{periodNo})
            """)
    int insertPeriod(
            @Param("bookingId") Long bookingId,
            @Param("resourceId") Long resourceId,
            @Param("studentId") Long studentId,
            @Param("bookingDate") LocalDate bookingDate,
            @Param("periodNo") Integer periodNo
    );

    @Delete("DELETE FROM lab_booking_period WHERE booking_id = #{bookingId}")
    int deletePeriods(@Param("bookingId") Long bookingId);

    @Update("""
            UPDATE lab_booking
            SET status = 5, updated_at = #{now}
            WHERE status IN (1, 4)
              AND (booking_date < #{today}
                   OR (status = 1 AND expires_at IS NOT NULL AND expires_at <= #{now}))
            """)
    int expireStaleBookings(@Param("today") LocalDate today, @Param("now") LocalDateTime now);

    @Select("""
            SELECT COUNT(*) FROM lab_booking
            WHERE lab_id = #{labId} AND booking_date = #{today}
              AND (status = 4 OR (status = 1 AND expires_at > #{now}))
            """)
    int countActiveLabBookings(
            @Param("labId") Long labId,
            @Param("today") LocalDate today,
            @Param("now") LocalDateTime now);

    @Select("""
            SELECT COUNT(*) FROM lab_booking
            WHERE lab_id = #{labId} AND student_id = #{studentId} AND booking_date = #{today}
              AND (status = 4 OR (status = 1 AND expires_at > #{now}))
            """)
    int countStudentActiveLabBookings(
            @Param("labId") Long labId,
            @Param("studentId") Long studentId,
            @Param("today") LocalDate today,
            @Param("now") LocalDateTime now);
}
