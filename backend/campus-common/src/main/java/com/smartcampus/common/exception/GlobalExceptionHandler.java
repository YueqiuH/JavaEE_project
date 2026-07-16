package com.smartcampus.common.exception;

import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.result.CommonResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 出现异常返回错误提示, 并且回滚事务
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult exceptionHandler(Exception e) {
        System.err.println("捕获到异常");
        e.printStackTrace();
        return CommonResult.error(GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR);
    }

}
