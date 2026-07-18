package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.CompetitionTeam;
import com.smartcampus.contract.vo.student.CompetitionTeamVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CompetitionTeamMapper extends BaseMapper<CompetitionTeam> {

    @Select("""
            <script>
            SELECT t.team_id, t.registration_no, t.competition_id, c.title AS competition_title,
                   c.deadline AS competition_deadline, c.min_members, c.max_members, c.publisher_id,
                   t.team_name, t.leader_id, leader.student_no AS leader_no,
                   leader.student_name AS leader_name, t.material_url, t.material_description,
                   t.status AS status_code, t.reviewer_id, reviewer.username AS reviewer_name,
                   t.review_opinion, t.apply_time, t.submitted_at, t.reviewed_at, t.updated_at,
                   (SELECT COUNT(*) FROM competition_member m
                    WHERE m.team_id = t.team_id AND m.invitation_status = 1) AS accepted_member_count,
                   (SELECT COUNT(*) FROM competition_member m
                    WHERE m.team_id = t.team_id AND m.invitation_status = 0) AS invited_member_count
            FROM competition_team t
            JOIN competition c ON c.competition_id = t.competition_id
            JOIN student leader ON leader.student_id = t.leader_id
            LEFT JOIN user reviewer ON reviewer.user_id = t.reviewer_id
            WHERE 1 = 1
            <if test="studentId != null">
              AND EXISTS (SELECT 1 FROM competition_member own_member
                          WHERE own_member.team_id = t.team_id
                            AND own_member.student_id = #{studentId}
                            AND own_member.invitation_status = 1)
            </if>
            <if test="publisherId != null">AND c.publisher_id = #{publisherId}</if>
            <if test="status != null">AND t.status = #{status}</if>
            ORDER BY t.updated_at DESC, t.team_id DESC
            </script>
            """)
    IPage<CompetitionTeamVo> selectTeamPage(
            Page<CompetitionTeamVo> page,
            @Param("studentId") Long studentId,
            @Param("publisherId") Long publisherId,
            @Param("status") Integer status
    );

    @Select("""
            SELECT t.team_id, t.registration_no, t.competition_id, c.title AS competition_title,
                   c.deadline AS competition_deadline, c.min_members, c.max_members, c.publisher_id,
                   t.team_name, t.leader_id, leader.student_no AS leader_no,
                   leader.student_name AS leader_name, t.material_url, t.material_description,
                   t.status AS status_code, t.reviewer_id, reviewer.username AS reviewer_name,
                   t.review_opinion, t.apply_time, t.submitted_at, t.reviewed_at, t.updated_at,
                   (SELECT COUNT(*) FROM competition_member m
                    WHERE m.team_id = t.team_id AND m.invitation_status = 1) AS accepted_member_count,
                   (SELECT COUNT(*) FROM competition_member m
                    WHERE m.team_id = t.team_id AND m.invitation_status = 0) AS invited_member_count
            FROM competition_team t
            JOIN competition c ON c.competition_id = t.competition_id
            JOIN student leader ON leader.student_id = t.leader_id
            LEFT JOIN user reviewer ON reviewer.user_id = t.reviewer_id
            WHERE t.team_id = #{id}
            """)
    CompetitionTeamVo selectTeamView(@Param("id") Long id);

    @Select("""
            <script>
            SELECT COUNT(*)
            FROM competition_team t
            JOIN competition_member m ON m.team_id = t.team_id
            WHERE t.competition_id = #{competitionId} AND m.student_id = #{studentId}
              AND m.invitation_status = 1 AND t.status IN (0, 1, 2, 3)
            <if test="excludeTeamId != null">AND t.team_id != #{excludeTeamId}</if>
            </script>
            """)
    int countStudentActiveTeams(
            @Param("competitionId") Long competitionId,
            @Param("studentId") Long studentId,
            @Param("excludeTeamId") Long excludeTeamId
    );
}
