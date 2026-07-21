package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.Competition;
import com.smartcampus.contract.vo.student.CompetitionVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("""
            <script>
            SELECT c.competition_id, c.competition_no, c.title, c.description, c.requirements,
                   c.publisher_id, u.username AS publisher_name, c.deadline,
                   c.min_members, c.max_members, c.max_team_count, c.status AS status_code,
                   c.create_time, c.updated_at, c.published_at, c.closed_at,
                   (SELECT COUNT(*) FROM competition_team t
                    WHERE t.competition_id = c.competition_id AND t.status != 4) AS team_count,
                   (SELECT COUNT(*) FROM competition_team t
                    WHERE t.competition_id = c.competition_id AND t.status = 2) AS approved_team_count,
                   <choose>
                     <when test="studentId != null">
                       (SELECT t.team_id FROM competition_team t
                        JOIN competition_member m ON m.team_id = t.team_id
                        WHERE t.competition_id = c.competition_id AND m.student_id = #{studentId}
                          AND m.invitation_status = 1 AND t.status IN (0, 1, 2, 3)
                        ORDER BY t.team_id DESC LIMIT 1) AS my_team_id
                     </when>
                     <otherwise>NULL AS my_team_id</otherwise>
                   </choose>
            FROM competition c
            JOIN user u ON u.user_id = c.publisher_id
            WHERE 1 = 1
            <if test="publisherId != null">AND c.publisher_id = #{publisherId}</if>
            <if test="openOnly">AND c.status = 1 AND c.deadline &gt;= CURRENT_DATE</if>
            <if test="status != null">AND c.status = #{status}</if>
            ORDER BY c.updated_at DESC, c.competition_id DESC
            </script>
            """)
    IPage<CompetitionVo> selectCompetitionPage(
            Page<CompetitionVo> page,
            @Param("publisherId") Long publisherId,
            @Param("openOnly") boolean openOnly,
            @Param("studentId") Long studentId,
            @Param("status") Integer status
    );

    @Select("""
            <script>
            SELECT c.competition_id, c.competition_no, c.title, c.description, c.requirements,
                   c.publisher_id, u.username AS publisher_name, c.deadline,
                   c.min_members, c.max_members, c.max_team_count, c.status AS status_code,
                   c.create_time, c.updated_at, c.published_at, c.closed_at,
                   (SELECT COUNT(*) FROM competition_team t
                    WHERE t.competition_id = c.competition_id AND t.status != 4) AS team_count,
                   (SELECT COUNT(*) FROM competition_team t
                    WHERE t.competition_id = c.competition_id AND t.status = 2) AS approved_team_count,
                   <choose>
                     <when test="studentId != null">
                       (SELECT t.team_id FROM competition_team t
                        JOIN competition_member m ON m.team_id = t.team_id
                        WHERE t.competition_id = c.competition_id AND m.student_id = #{studentId}
                          AND m.invitation_status = 1 AND t.status IN (0, 1, 2, 3)
                        ORDER BY t.team_id DESC LIMIT 1) AS my_team_id
                     </when>
                     <otherwise>NULL AS my_team_id</otherwise>
                   </choose>
            FROM competition c
            JOIN user u ON u.user_id = c.publisher_id
            WHERE c.competition_id = #{id}
            </script>
            """)
    CompetitionVo selectCompetitionView(@Param("id") Long id, @Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM competition_team WHERE competition_id = #{competitionId} AND status = 2")
    int countApprovedTeams(@Param("competitionId") Long competitionId);
}
