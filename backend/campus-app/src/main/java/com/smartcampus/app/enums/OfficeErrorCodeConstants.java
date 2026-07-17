package com.smartcampus.app.enums;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

/** 成员 C 协同办公域稳定错误码。 */
public interface OfficeErrorCodeConstants {

    ErrorCode BAD_REQUEST = new ErrorCode(400100, "办公业务参数不正确", HttpStatus.BAD_REQUEST);
    ErrorCode FORBIDDEN = new ErrorCode(403100, "无权操作该办公资源", HttpStatus.FORBIDDEN);
    ErrorCode NOT_FOUND = new ErrorCode(404100, "办公业务资源不存在", HttpStatus.NOT_FOUND);
    ErrorCode STATE_CONFLICT = new ErrorCode(409100, "办公业务状态冲突", HttpStatus.CONFLICT);
    ErrorCode AI_NOT_CONFIGURED = new ErrorCode(503100, "AI 服务未配置", HttpStatus.SERVICE_UNAVAILABLE);
    ErrorCode AI_UNAVAILABLE = new ErrorCode(503101, "AI 服务暂时不可用", HttpStatus.SERVICE_UNAVAILABLE);
}
