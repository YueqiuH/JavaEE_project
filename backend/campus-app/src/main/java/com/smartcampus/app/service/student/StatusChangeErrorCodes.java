package com.smartcampus.app.service.student;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface StatusChangeErrorCodes {
    ErrorCode APPLICATION_NOT_FOUND = new ErrorCode(404211, "学籍异动申请不存在", HttpStatus.NOT_FOUND);
    ErrorCode STUDENT_PROFILE_NOT_FOUND = new ErrorCode(409211, "当前账号未关联学生档案", HttpStatus.CONFLICT);
    ErrorCode APPLICATION_NOT_OWNED = new ErrorCode(403211, "不能操作其他学生的学籍申请", HttpStatus.FORBIDDEN);
    ErrorCode INVALID_STATUS = new ErrorCode(409212, "当前申请状态不允许执行此操作", HttpStatus.CONFLICT);
    ErrorCode MAJOR_REQUIRED = new ErrorCode(400211, "转专业申请必须选择目标专业", HttpStatus.BAD_REQUEST);
    ErrorCode MAJOR_NOT_FOUND = new ErrorCode(404212, "目标专业不存在", HttpStatus.NOT_FOUND);
    ErrorCode REVIEW_OPINION_REQUIRED = new ErrorCode(400212, "拒绝申请时必须填写审核意见", HttpStatus.BAD_REQUEST);
    ErrorCode ACTIVE_APPLICATION_EXISTS = new ErrorCode(409213, "已有学籍异动申请正在审核中", HttpStatus.CONFLICT);
}
