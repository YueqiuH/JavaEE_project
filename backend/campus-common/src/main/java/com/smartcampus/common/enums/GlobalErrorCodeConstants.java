package com.smartcampus.common.enums;

import com.smartcampus.common.result.ErrorCode;

public interface GlobalErrorCodeConstants {

    ErrorCode SUCCESS = new ErrorCode(200, "成功");

    ErrorCode UPDATE_ERROR = new ErrorCode(500, "更新失败");
    ErrorCode ADD_ERROR = new ErrorCode(500, "新增失败");
    ErrorCode DELETE_ERROR = new ErrorCode(500, "删除失败");

    // ========== 客户端错误段 ==========
    ErrorCode BAD_REQUEST = new ErrorCode(400, "请求参数不正确");
    ErrorCode UNAUTHORIZED = new ErrorCode(401, "账号未登录");
    ErrorCode LOGIN_ERROR = new ErrorCode(402, "账号或密码错误");
    ErrorCode FORBIDDEN = new ErrorCode(403, "没有该操作权限");
    ErrorCode NOT_FOUND = new ErrorCode(404, "请求未找到");
    ErrorCode METHOD_NOT_ALLOWED = new ErrorCode(405, "请求方法不正确");
    ErrorCode LOCKED = new ErrorCode(423, "请求失败，请稍后重试");
    ErrorCode TOO_MANY_REQUESTS = new ErrorCode(429, "请求过于频繁，请稍后重试");

    // ========== 服务端错误段 ==========
    ErrorCode INTERNAL_SERVER_ERROR = new ErrorCode(500, "操作有误");
    ErrorCode NOT_IMPLEMENTED = new ErrorCode(501, "功能未实现/未开启");
    ErrorCode ERROR_CONFIGURATION = new ErrorCode(502, "错误的配置项");

    // ========== 自定义错误段 ==========
    ErrorCode REPEATED_REQUESTS = new ErrorCode(900, "重复请求，请稍后重试");
    ErrorCode DEMO_DENY = new ErrorCode(901, "演示模式，禁止写操作");
    ErrorCode STUDENT_NO_ERROR = new ErrorCode(902, "学号重复");
    ErrorCode UNKNOWN = new ErrorCode(999, "未知错误");
}
