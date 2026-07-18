package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.LabOpenSlot;
import com.smartcampus.contract.vo.student.LabOpenSlotVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface LabOpenSlotMapper extends BaseMapper<LabOpenSlot> {

    @Select("""
            SELECT s.slot_id, s.lab_id, l.lab_name, l.location, s.open_date,
                   s.start_period, s.end_period, s.created_by, creator.username AS created_by_name,
                   s.created_at, s.updated_at
            FROM lab_open_slot s
            JOIN lab l ON l.lab_id = s.lab_id
            LEFT JOIN user creator ON creator.user_id = s.created_by
            WHERE s.slot_id = #{id}
            """)
    LabOpenSlotVo selectSlotView(@Param("id") Long id);

    @Select("""
            <script>
            SELECT s.slot_id, s.lab_id, l.lab_name, l.location, s.open_date,
                   s.start_period, s.end_period, s.created_by, creator.username AS created_by_name,
                   s.created_at, s.updated_at
            FROM lab_open_slot s
            JOIN lab l ON l.lab_id = s.lab_id
            LEFT JOIN user creator ON creator.user_id = s.created_by
            WHERE 1 = 1
            <if test="labId != null">AND s.lab_id = #{labId}</if>
            <if test="managerId != null">AND l.manager_id = #{managerId}</if>
            <if test="studentVisible">AND l.status = 1 AND s.open_date &gt;= CURRENT_DATE</if>
            ORDER BY s.open_date, s.start_period, s.slot_id
            </script>
            """)
    IPage<LabOpenSlotVo> selectSlotPage(
            Page<LabOpenSlotVo> page,
            @Param("labId") Long labId,
            @Param("managerId") Long managerId,
            @Param("studentVisible") boolean studentVisible
    );

    @Select("""
            SELECT COUNT(*) FROM lab_open_slot
            WHERE lab_id = #{labId} AND open_date = #{openDate}
              AND start_period <= #{endPeriod} AND end_period >= #{startPeriod}
              AND (#{excludeSlotId} IS NULL OR slot_id != #{excludeSlotId})
            """)
    int countOverlappingSlots(
            @Param("labId") Long labId,
            @Param("openDate") LocalDate openDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod,
            @Param("excludeSlotId") Long excludeSlotId
    );

    @Select("""
            SELECT COUNT(*) FROM lab_open_slot
            WHERE lab_id = #{labId} AND open_date = #{openDate}
              AND start_period <= #{startPeriod} AND end_period >= #{endPeriod}
            """)
    int countContainingSlots(
            @Param("labId") Long labId,
            @Param("openDate") LocalDate openDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );

    @Select("""
            SELECT COUNT(*) FROM lab_booking
            WHERE lab_id = #{labId} AND booking_date = #{openDate} AND status = 1
              AND start_period <= #{endPeriod} AND end_period >= #{startPeriod}
            """)
    int countActiveBookings(
            @Param("labId") Long labId,
            @Param("openDate") LocalDate openDate,
            @Param("startPeriod") Integer startPeriod,
            @Param("endPeriod") Integer endPeriod
    );
}
