package com.smartcampus.app.dao.student;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.contract.entity.CompetitionMember;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.CompetitionInvitationVo;
import com.smartcampus.contract.vo.student.CompetitionMemberVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CompetitionMemberMapper extends BaseMapper<CompetitionMember> {

    @Select("SELECT * FROM student WHERE student_no = #{studentNo} LIMIT 1")
    Student selectStudentByNo(@Param("studentNo") Long studentNo);

    @Select("""
            SELECT m.member_id, m.student_id, s.student_no, s.student_name, m.role,
                   m.invitation_status AS invitation_status_code, m.invited_at, m.responded_at
            FROM competition_member m
            JOIN student s ON s.student_id = m.student_id
            WHERE m.team_id = #{teamId} AND m.invitation_status != 3
            ORDER BY CASE m.role WHEN '队长' THEN 0 ELSE 1 END, m.member_id
            """)
    List<CompetitionMemberVo> selectMembers(@Param("teamId") Long teamId);

    @Select("SELECT * FROM competition_member WHERE team_id = #{teamId} AND student_id = #{studentId} LIMIT 1")
    CompetitionMember selectTeamMember(@Param("teamId") Long teamId, @Param("studentId") Long studentId);

    @Select("SELECT COUNT(*) FROM competition_member WHERE team_id = #{teamId} AND invitation_status = 1")
    int countAcceptedMembers(@Param("teamId") Long teamId);

    @Select("SELECT COUNT(*) FROM competition_member WHERE team_id = #{teamId} AND invitation_status IN (0, 1)")
    int countActiveMembers(@Param("teamId") Long teamId);

    @Select("""
            <script>
            SELECT m.member_id, m.team_id, t.team_name, t.competition_id,
                   c.title AS competition_title, c.deadline,
                   leader.student_no AS leader_no, leader.student_name AS leader_name,
                   m.invitation_status AS invitation_status_code, m.invited_at, m.responded_at
            FROM competition_member m
            JOIN competition_team t ON t.team_id = m.team_id
            JOIN competition c ON c.competition_id = t.competition_id
            JOIN student leader ON leader.student_id = t.leader_id
            WHERE m.student_id = #{studentId} AND m.role != '队长'
            <if test="status != null">AND m.invitation_status = #{status}</if>
            ORDER BY m.invited_at DESC, m.member_id DESC
            </script>
            """)
    IPage<CompetitionInvitationVo> selectInvitationPage(
            Page<CompetitionInvitationVo> page,
            @Param("studentId") Long studentId,
            @Param("status") Integer status
    );

    @Select("""
            SELECT m.member_id, m.team_id, t.team_name, t.competition_id,
                   c.title AS competition_title, c.deadline,
                   leader.student_no AS leader_no, leader.student_name AS leader_name,
                   m.invitation_status AS invitation_status_code, m.invited_at, m.responded_at
            FROM competition_member m
            JOIN competition_team t ON t.team_id = m.team_id
            JOIN competition c ON c.competition_id = t.competition_id
            JOIN student leader ON leader.student_id = t.leader_id
            WHERE m.student_id = #{studentId} AND m.member_id = #{memberId} AND m.role != '队长'
            """)
    CompetitionInvitationVo selectInvitation(
            @Param("studentId") Long studentId,
            @Param("memberId") Long memberId
    );
}
