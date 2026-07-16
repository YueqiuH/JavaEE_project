package com.smartcampus.common.exception;

import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResult<Void>> handleBusinessException(BusinessException exception) {
        return response(exception.getErrorCode());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<CommonResult<Void>> handleValidationException(Exception exception) {
        log.debug("请求参数校验失败", exception);
        return response(GlobalErrorCodeConstants.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResult<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        log.debug("请求方法不受支持", exception);
        return response(GlobalErrorCodeConstants.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResult<Void>> handleUnexpectedException(Exception exception) {
        log.error("未处理的服务端异常", exception);
        return response(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<CommonResult<Void>> response(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResult.error(errorCode));
    }

}
