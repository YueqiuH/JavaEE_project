package com.smartcampus.common.exception;

import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;

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

    // ========== 业务异常 ==========

    /**
     * 排课冲突 / 选课冲突等业务校验异常 —— 保留完整中文消息直接透传给前端。
     * 通过运行时检查类名来兼容 campus-app 模块中的 ScheduleConflictException。
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResult<Void>> handleBusinessException(RuntimeException exception) {
        String className = exception.getClass().getName();
        // 仅拦截 campus-app 中的业务异常（ScheduleConflictException 等）。
        // 其他 RuntimeException（如 NPE）重新抛出交给兜底 handler。
        if (className.startsWith("com.smartcampus.app.exception.")) {
            log.warn("业务校验冲突: {}", exception.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(CommonResult.error(409001, exception.getMessage()));
        }
        // 不是我们的业务异常，重新抛出交给兜底
        throw exception;
    }

    /**
     * SQL 约束冲突——提示数据冲突而非暴露 SQL 内部信息。
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<CommonResult<Void>> handleSqlConstraint(SQLIntegrityConstraintViolationException exception) {
        log.warn("数据约束冲突", exception);
        return response(new ErrorCode(409002, "操作失败：数据冲突，可能已存在相同记录", HttpStatus.CONFLICT));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<CommonResult<Void>> handleSql(SQLException exception) {
        log.error("数据库异常", exception);
        return response(new ErrorCode(500003, "数据库操作异常，请联系管理员", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ========== 兜底 ==========

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
