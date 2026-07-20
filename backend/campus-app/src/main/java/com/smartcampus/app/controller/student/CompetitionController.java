package com.smartcampus.app.controller.student;

import com.smartcampus.app.service.student.CompetitionService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.CompetitionInvitationRequest;
import com.smartcampus.contract.dto.student.CompetitionInvitationResponseRequest;
import com.smartcampus.contract.dto.student.CompetitionRequest;
import com.smartcampus.contract.dto.student.CompetitionReviewRequest;
import com.smartcampus.contract.dto.student.CompetitionTeamRequest;
import com.smartcampus.contract.vo.student.CompetitionInvitationVo;
import com.smartcampus.contract.vo.student.CompetitionTeamVo;
import com.smartcampus.contract.vo.student.CompetitionVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@Tag(name = "学科竞赛与组队")
public class CompetitionController {

    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @GetMapping("/competitions")
    @RequirePermission("competition:read")
    @Operation(summary = "按当前角色查询竞赛列表")
    public CommonResult<PageResult<CompetitionVo>> listCompetitions(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(competitionService.listCompetitions(page, size, status));
    }

    @GetMapping("/competitions/{id}")
    @RequirePermission("competition:read")
    @Operation(summary = "查询竞赛详情")
    public CommonResult<CompetitionVo> getCompetition(@PathVariable Long id) {
        return CommonResult.success(competitionService.getCompetition(id));
    }

    @PostMapping("/competitions")
    @RequirePermission("competition:publish")
    @Operation(summary = "教师创建竞赛草稿")
    public CommonResult<CompetitionVo> createCompetition(@Valid @RequestBody CompetitionRequest request) {
        return CommonResult.success(competitionService.createCompetition(request));
    }

    @PutMapping("/competitions/{id}")
    @RequirePermission("competition:manage-self")
    @Operation(summary = "教师修改本人竞赛草稿")
    public CommonResult<CompetitionVo> updateCompetition(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionRequest request) {
        return CommonResult.success(competitionService.updateCompetition(id, request));
    }

    @PostMapping("/competitions/{id}/publications")
    @RequirePermission("competition:manage-self")
    @Operation(summary = "教师发布本人竞赛")
    public CommonResult<CompetitionVo> publishCompetition(@PathVariable Long id) {
        return CommonResult.success(competitionService.publishCompetition(id));
    }

    @PostMapping("/competitions/{id}/closures")
    @RequirePermission("competition:manage-self")
    @Operation(summary = "教师关闭本人竞赛报名")
    public CommonResult<CompetitionVo> closeCompetition(@PathVariable Long id) {
        return CommonResult.success(competitionService.closeCompetition(id));
    }

    @GetMapping("/competition-teams/mine")
    @RequirePermission("competition:team:read-self")
    @Operation(summary = "学生查询本人已加入的竞赛队伍")
    public CommonResult<PageResult<CompetitionTeamVo>> listMyTeams(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(competitionService.listMyTeams(page, size, status));
    }

    @GetMapping("/competitions/{id}/teams")
    @RequirePermission("competition:read")
    @Operation(summary = "教师查询本人竞赛队伍，辅导员和教务处查询全部竞赛队伍")
    public CommonResult<PageResult<CompetitionTeamVo>> listCompetitionTeams(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(competitionService.listCompetitionTeams(id, page, size, status));
    }

    @GetMapping("/competition-teams/{id}")
    @RequirePermission("student:read")
    @Operation(summary = "按归属查询竞赛队伍详情")
    public CommonResult<CompetitionTeamVo> getTeam(@PathVariable Long id) {
        return CommonResult.success(competitionService.getTeam(id));
    }

    @PostMapping("/competitions/{competitionId}/teams")
    @RequirePermission("competition:team:create")
    @Operation(summary = "学生为指定竞赛发起组队")
    public CommonResult<CompetitionTeamVo> createTeam(
            @PathVariable Long competitionId,
            @Valid @RequestBody CompetitionTeamRequest request) {
        return CommonResult.success(competitionService.createTeam(competitionId, request));
    }

    @PutMapping("/competition-teams/{id}")
    @RequirePermission("competition:team:manage-self")
    @Operation(summary = "队长维护组队信息与报名材料")
    public CommonResult<CompetitionTeamVo> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionTeamRequest request) {
        return CommonResult.success(competitionService.updateTeam(id, request));
    }

    @PutMapping(value = "/competition-teams/{id}/material", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("competition:team:manage-self")
    @Operation(summary = "队长上传或替换报名材料文件")
    public CommonResult<CompetitionTeamVo> uploadMaterial(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        return CommonResult.success(competitionService.uploadMaterial(id, file));
    }

    @GetMapping("/competition-teams/{id}/material")
    @RequirePermission("student:read")
    @Operation(summary = "按队伍归属下载报名材料")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        CompetitionService.DownloadMaterial material = competitionService.downloadMaterial(id);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(material.contentType());
        } catch (RuntimeException exception) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(material.fileName(), StandardCharsets.UTF_8)
                .build();
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        if (material.size() != null) {
            response.contentLength(material.size());
        }
        return response.body(material.resource());
    }

    @PostMapping("/competition-teams/{id}/invitations")
    @RequirePermission("competition:team:manage-self")
    @Operation(summary = "队长按学号邀请队员")
    public CommonResult<CompetitionTeamVo> inviteMember(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionInvitationRequest request) {
        return CommonResult.success(competitionService.inviteMember(id, request));
    }

    @DeleteMapping("/competition-teams/{teamId}/members/{memberId}")
    @RequirePermission("competition:team:manage-self")
    @Operation(summary = "队长移除非队长成员")
    public CommonResult<CompetitionTeamVo> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        return CommonResult.success(competitionService.removeMember(teamId, memberId));
    }

    @PostMapping("/competition-teams/{id}/submissions")
    @RequirePermission("competition:team:submit-self")
    @Operation(summary = "队长提交或重新提交竞赛报名")
    public CommonResult<CompetitionTeamVo> submitTeam(@PathVariable Long id) {
        return CommonResult.success(competitionService.submitTeam(id));
    }

    @GetMapping("/competition-invitations/mine")
    @RequirePermission("competition:team:read-self")
    @Operation(summary = "学生查询本人组队邀请")
    public CommonResult<PageResult<CompetitionInvitationVo>> listMyInvitations(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(competitionService.listMyInvitations(page, size, status));
    }

    @PostMapping("/competition-invitations/{memberId}/responses")
    @RequirePermission("competition:invitation:respond-self")
    @Operation(summary = "学生接受或拒绝本人组队邀请")
    public CommonResult<CompetitionInvitationVo> respondInvitation(
            @PathVariable Long memberId,
            @Valid @RequestBody CompetitionInvitationResponseRequest request) {
        return CommonResult.success(competitionService.respondInvitation(memberId, request));
    }

    @GetMapping("/competition-reviews")
    @RequirePermission("competition:read")
    @Operation(summary = "教师查询本人审核队列，辅导员和教务处查询全部队伍")
    public CommonResult<PageResult<CompetitionTeamVo>> listReviews(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(competitionService.listReviews(page, size, status));
    }

    @PostMapping("/competition-teams/{id}/reviews")
    @RequirePermission("competition:review:submit-self")
    @Operation(summary = "教师审核本人竞赛的参赛队伍")
    public CommonResult<CompetitionTeamVo> reviewTeam(
            @PathVariable Long id,
            @Valid @RequestBody CompetitionReviewRequest request) {
        return CommonResult.success(competitionService.reviewTeam(id, request));
    }
}
