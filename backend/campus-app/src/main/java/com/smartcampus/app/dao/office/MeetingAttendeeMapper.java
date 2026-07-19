package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.MeetingAttendee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

public interface MeetingAttendeeMapper extends BaseMapper<MeetingAttendee> {

    @Select("""
            <script>
            SELECT DISTINCT u.user_id
            FROM `user` u
            JOIN user_role ur ON ur.user_id = u.user_id
            JOIN `role` r ON r.role_id = ur.role_id
            WHERE u.status = 1
              AND r.status = 1
              AND r.role_code IN
              <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
                  #{roleCode}
              </foreach>
            ORDER BY u.user_id
            </script>
            """)
    List<Long> findActiveUserIdsByRoleCodes(@Param("roleCodes") Collection<String> roleCodes);
}
