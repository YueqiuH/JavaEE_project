package com.smartcampus.common.result;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ErrorCode {
    /**
     * 错误码
     */
    private final Integer code;
    /**
     * 错误提示
     */
    private final String message;

    /**
     * 对应的 HTTP 状态码。
     */
    private final HttpStatus httpStatus;

    public ErrorCode(Integer code, String message) {
        this(code, message, HttpStatus.BAD_REQUEST);
    }

    public ErrorCode(Integer code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
