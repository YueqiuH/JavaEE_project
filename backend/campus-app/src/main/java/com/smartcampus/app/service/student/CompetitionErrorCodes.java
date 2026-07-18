package com.smartcampus.app.service.student;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface CompetitionErrorCodes {

    ErrorCode COMPETITION_NOT_FOUND = new ErrorCode(404401, "竞赛不存在", HttpStatus.NOT_FOUND);
    ErrorCode TEAM_NOT_FOUND = new ErrorCode(404402, "竞赛队伍不存在", HttpStatus.NOT_FOUND);
    ErrorCode INVITATION_NOT_FOUND = new ErrorCode(404403, "组队邀请不存在", HttpStatus.NOT_FOUND);
    ErrorCode INVITEE_NOT_FOUND = new ErrorCode(404404, "受邀学号未关联学生档案", HttpStatus.NOT_FOUND);
    ErrorCode STUDENT_PROFILE_NOT_FOUND = new ErrorCode(409401, "当前账号未关联学生档案", HttpStatus.CONFLICT);
    ErrorCode INVALID_STATUS = new ErrorCode(409402, "当前状态不允许执行此操作", HttpStatus.CONFLICT);
    ErrorCode REGISTRATION_CLOSED = new ErrorCode(409403, "竞赛未开放报名或报名已截止", HttpStatus.CONFLICT);
    ErrorCode TEAM_SIZE_INVALID = new ErrorCode(409404, "队伍人数不符合竞赛要求", HttpStatus.CONFLICT);
    ErrorCode ALREADY_IN_TEAM = new ErrorCode(409405, "该学生已加入本竞赛的其他有效队伍", HttpStatus.CONFLICT);
    ErrorCode DUPLICATE_INVITATION = new ErrorCode(409406, "该学生已在当前队伍或已有待处理邀请", HttpStatus.CONFLICT);
    ErrorCode MATERIAL_REQUIRED = new ErrorCode(409407, "提交报名时必须上传报名材料文件", HttpStatus.CONFLICT);
    ErrorCode APPROVAL_CAPACITY_FULL = new ErrorCode(409408, "竞赛入选队伍名额已满", HttpStatus.CONFLICT);
    ErrorCode MEMBER_RANGE_INVALID = new ErrorCode(409409, "最少队伍人数不能大于最多队伍人数", HttpStatus.CONFLICT);
    ErrorCode COMPETITION_NOT_OWNED = new ErrorCode(403401, "不能维护其他教师发布的竞赛", HttpStatus.FORBIDDEN);
    ErrorCode TEAM_NOT_OWNED = new ErrorCode(403402, "只有当前队伍队长可以执行此操作", HttpStatus.FORBIDDEN);
    ErrorCode TEAM_NOT_VISIBLE = new ErrorCode(403403, "无权查看该竞赛队伍", HttpStatus.FORBIDDEN);
    ErrorCode INVITATION_NOT_OWNED = new ErrorCode(403404, "不能处理其他学生的组队邀请", HttpStatus.FORBIDDEN);
    ErrorCode REVIEW_OPINION_REQUIRED = new ErrorCode(400401, "退回或拒绝时必须填写审核意见", HttpStatus.BAD_REQUEST);
    ErrorCode MATERIAL_FILE_INVALID = new ErrorCode(400402, "仅支持 PDF、Word、Excel 或 ZIP 报名材料", HttpStatus.BAD_REQUEST);
    ErrorCode MATERIAL_FILE_TOO_LARGE = new ErrorCode(413401, "报名材料不能超过20MB", HttpStatus.PAYLOAD_TOO_LARGE);
    ErrorCode MATERIAL_FILE_NOT_FOUND = new ErrorCode(404405, "报名材料文件不存在", HttpStatus.NOT_FOUND);
    ErrorCode MATERIAL_STORAGE_ERROR = new ErrorCode(500401, "报名材料存储失败", HttpStatus.INTERNAL_SERVER_ERROR);
}
