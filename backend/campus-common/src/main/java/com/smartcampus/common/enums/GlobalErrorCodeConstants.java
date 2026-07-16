package com.smartcampus.common.enums;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(0, "success", HttpStatus.OK);

    ErrorCode UPDATE_ERROR = new ErrorCode(500101, "更新失败", HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorCode ADD_ERROR = new ErrorCode(500102, "新增失败", HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorCode DELETE_ERROR = new ErrorCode(500103, "删除失败", HttpStatus.INTERNAL_SERVER_ERROR);

    // ========== 客户端错误段 ==========
    ErrorCode BAD_REQUEST = new ErrorCode(400001, "请求参数不正确", HttpStatus.BAD_REQUEST);
    ErrorCode UNAUTHORIZED = new ErrorCode(401001, "账号未登录", HttpStatus.UNAUTHORIZED);
    ErrorCode LOGIN_ERROR = new ErrorCode(401002, "账号或密码错误", HttpStatus.UNAUTHORIZED);
    ErrorCode FORBIDDEN = new ErrorCode(403001, "没有该操作权限", HttpStatus.FORBIDDEN);
    ErrorCode NOT_FOUND = new ErrorCode(404001, "请求未找到", HttpStatus.NOT_FOUND);
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405001, "请求方法不正确", HttpStatus.METHOD_NOT_ALLOWED);
    ErrorCode LOCKED = new ErrorCode(423001, "请求失败，请稍后重试", HttpStatus.LOCKED);
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429001, "请求过于频繁，请稍后重试", HttpStatus.TOO_MANY_REQUESTS);

    // ========== 服务端错误段 ==========
    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500001, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorCode NOT_IMPLEMENTED = new ErrorCode(501001, "功能未实现或未开启", HttpStatus.NOT_IMPLEMENTED);
    ErrorCode ERROR_CONFIGURATION = new ErrorCode(500002, "服务配置错误", HttpStatus.INTERNAL_SERVER_ERROR);

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode(409001, "重复请求，请稍后重试", HttpStatus.CONFLICT);
    ErrorCode DEMO_DENY = new ErrorCode(403002, "演示模式，禁止写操作", HttpStatus.FORBIDDEN);
    ErrorCode STUDENT_NO_ERROR = new ErrorCode(409002, "学号重复", HttpStatus.CONFLICT);
    ErrorCode UNKNOWN = new ErrorCode(500999, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR);
}
