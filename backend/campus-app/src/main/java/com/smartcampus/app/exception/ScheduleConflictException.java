package com.smartcampus.app.exception;

/**
 * 排课冲突异常 —— 携带清晰的中文冲突原因。
 * 由 {@link com.smartcampus.app.validator.ScheduleValidator} 在校验失败时抛出，
 * Controller 层通过全局异常处理器捕获后返回给前端。
 */
public class ScheduleConflictException extends RuntimeException {

    /** 业务错误码（便于前端分类展示） */
    private final String errorCode;

    public ScheduleConflictException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ScheduleConflictException(String message) {
        this("SCHEDULE_CONFLICT", message);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
