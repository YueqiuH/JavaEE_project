package com.smartcampus.app.service.base;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 基础数据模块（成员 D）业务错误码，段位 4xx1xx / 4091xx。
 */
public interface BaseErrorCodes {

    ErrorCode DEPT_NOT_FOUND       = new ErrorCode(404101, "院系不存在", HttpStatus.NOT_FOUND);
    ErrorCode DEPT_CODE_DUPLICATE  = new ErrorCode(409101, "院系编号已存在", HttpStatus.CONFLICT);
    ErrorCode DEPT_HAS_MAJORS      = new ErrorCode(409102, "该院系下仍有专业，无法删除", HttpStatus.CONFLICT);

    ErrorCode MAJOR_NOT_FOUND      = new ErrorCode(404102, "专业不存在", HttpStatus.NOT_FOUND);
    ErrorCode MAJOR_CODE_DUPLICATE = new ErrorCode(409103, "专业编号已存在", HttpStatus.CONFLICT);
    ErrorCode MAJOR_HAS_STUDENTS   = new ErrorCode(409104, "该专业下仍有学生，无法删除", HttpStatus.CONFLICT);

    ErrorCode STUDENT_NOT_FOUND    = new ErrorCode(404103, "学生不存在", HttpStatus.NOT_FOUND);
    ErrorCode STAFF_NOT_FOUND      = new ErrorCode(404104, "教职工不存在", HttpStatus.NOT_FOUND);
    ErrorCode USERNAME_DUPLICATE   = new ErrorCode(409105, "工号/账号已存在", HttpStatus.CONFLICT);
    ErrorCode MAJOR_DEPT_MISMATCH  = new ErrorCode(409106, "所选专业不属于所选院系", HttpStatus.CONFLICT);
    ErrorCode ROLE_ASSIGN_FAILED   = new ErrorCode(500110, "角色分配失败，目标角色不存在", HttpStatus.INTERNAL_SERVER_ERROR);

    ErrorCode NEWS_NOT_FOUND       = new ErrorCode(404105, "新闻公告不存在", HttpStatus.NOT_FOUND);
    ErrorCode POST_NOT_FOUND       = new ErrorCode(404106, "帖子不存在或已删除", HttpStatus.NOT_FOUND);
    ErrorCode COMMENT_NOT_FOUND    = new ErrorCode(404107, "回复不存在或已删除", HttpStatus.NOT_FOUND);
    ErrorCode POST_NOT_ACTIVE      = new ErrorCode(409108, "帖子已封禁或删除，无法操作", HttpStatus.CONFLICT);
    ErrorCode CANNOT_LIKE_OWN_POST  = new ErrorCode(409111, "不能给自己的帖子点赞", HttpStatus.CONFLICT);
    ErrorCode ALREADY_LIKED        = new ErrorCode(409112, "您已经点过赞了", HttpStatus.CONFLICT);

    ErrorCode ENROLLMENT_NOT_FOUND = new ErrorCode(404108, "招生计划不存在", HttpStatus.NOT_FOUND);
    ErrorCode ENROLLMENT_DUPLICATE = new ErrorCode(409109, "该专业该年度的招生计划已存在", HttpStatus.CONFLICT);

    ErrorCode AI_SQL_REJECTED      = new ErrorCode(409110, "AI 生成的查询未通过安全校验，请换个问法", HttpStatus.CONFLICT);
    ErrorCode AI_RATE_LIMITED      = new ErrorCode(429101, "AI 报表请求过于频繁，请稍后再试", HttpStatus.TOO_MANY_REQUESTS);
    ErrorCode AI_SERVICE_ERROR     = new ErrorCode(502101, "AI 服务调用失败，请稍后重试", HttpStatus.BAD_GATEWAY);
    ErrorCode AI_RESPONSE_INVALID  = new ErrorCode(502102, "AI 返回结果无法解析，请换个问法", HttpStatus.BAD_GATEWAY);
}
