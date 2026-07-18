package com.smartcampus.app.service.student;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface ScholarshipErrorCodes {

    ErrorCode APPLICATION_NOT_FOUND = new ErrorCode(404201, "奖助贷申请不存在", HttpStatus.NOT_FOUND);
    ErrorCode STUDENT_PROFILE_NOT_FOUND = new ErrorCode(409201, "当前账号未关联学生档案", HttpStatus.CONFLICT);
    ErrorCode INVALID_STATUS = new ErrorCode(409202, "当前申请状态不允许执行此操作", HttpStatus.CONFLICT);
    ErrorCode APPLICATION_NOT_OWNED = new ErrorCode(403201, "不能操作其他学生的申请", HttpStatus.FORBIDDEN);
    ErrorCode REVIEW_OPINION_REQUIRED = new ErrorCode(400201, "退回或拒绝时必须填写评审意见", HttpStatus.BAD_REQUEST);
    ErrorCode RESULT_SELECTION_INVALID = new ErrorCode(409203, "只有已通过的申请可以生成资助名单", HttpStatus.CONFLICT);
}
