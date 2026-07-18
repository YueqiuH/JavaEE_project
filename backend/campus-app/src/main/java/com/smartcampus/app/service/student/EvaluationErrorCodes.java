package com.smartcampus.app.service.student;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface EvaluationErrorCodes {

    ErrorCode STUDENT_PROFILE_NOT_FOUND = new ErrorCode(409301, "当前账号未关联学生档案", HttpStatus.CONFLICT);
    ErrorCode TASK_NOT_AVAILABLE = new ErrorCode(403301, "该评教任务不属于当前学生或已失效", HttpStatus.FORBIDDEN);
    ErrorCode ALREADY_SUBMITTED = new ErrorCode(409302, "该课程评教已提交，不能重复提交", HttpStatus.CONFLICT);
    ErrorCode RESULT_NOT_FOUND = new ErrorCode(404301, "未找到该课程的个人评教结果", HttpStatus.NOT_FOUND);
}
