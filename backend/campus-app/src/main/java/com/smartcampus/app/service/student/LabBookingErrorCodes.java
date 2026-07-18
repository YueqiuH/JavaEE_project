package com.smartcampus.app.service.student;

import com.smartcampus.common.result.ErrorCode;
import org.springframework.http.HttpStatus;

public interface LabBookingErrorCodes {
    ErrorCode LAB_NOT_FOUND = new ErrorCode(404501, "实验室不存在", HttpStatus.NOT_FOUND);
    ErrorCode RESOURCE_NOT_FOUND = new ErrorCode(404502, "实验设备或工位不存在", HttpStatus.NOT_FOUND);
    ErrorCode SLOT_NOT_FOUND = new ErrorCode(404503, "开放时段不存在", HttpStatus.NOT_FOUND);
    ErrorCode BOOKING_NOT_FOUND = new ErrorCode(404504, "预约记录不存在", HttpStatus.NOT_FOUND);
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(404505, "预约通知不存在", HttpStatus.NOT_FOUND);
    ErrorCode STUDENT_PROFILE_NOT_FOUND = new ErrorCode(409501, "当前账号未关联学生档案", HttpStatus.CONFLICT);
    ErrorCode LAB_NOT_OWNED = new ErrorCode(403501, "不能维护其他教师负责的实验室", HttpStatus.FORBIDDEN);
    ErrorCode BOOKING_NOT_OWNED = new ErrorCode(403502, "不能操作其他学生的预约", HttpStatus.FORBIDDEN);
    ErrorCode NOTICE_NOT_OWNED = new ErrorCode(403503, "不能操作其他学生的通知", HttpStatus.FORBIDDEN);
    ErrorCode INVALID_PERIOD = new ErrorCode(400501, "结束节次不能早于开始节次", HttpStatus.BAD_REQUEST);
    ErrorCode DATE_IN_PAST = new ErrorCode(409502, "不能预约或设置过去的日期", HttpStatus.CONFLICT);
    ErrorCode LAB_UNAVAILABLE = new ErrorCode(409503, "实验室当前处于维护状态", HttpStatus.CONFLICT);
    ErrorCode RESOURCE_UNAVAILABLE = new ErrorCode(409504, "实验设备或工位当前不可预约", HttpStatus.CONFLICT);
    ErrorCode OUTSIDE_OPEN_SLOT = new ErrorCode(409505, "预约时间不在实验室开放时段内", HttpStatus.CONFLICT);
    ErrorCode RESOURCE_CONFLICT = new ErrorCode(409506, "该设备或工位在所选时段已被预约", HttpStatus.CONFLICT);
    ErrorCode STUDENT_TIME_CONFLICT = new ErrorCode(409507, "本人在所选时段已有其他预约", HttpStatus.CONFLICT);
    ErrorCode SLOT_OVERLAP = new ErrorCode(409508, "开放时段与现有时段重叠", HttpStatus.CONFLICT);
    ErrorCode SLOT_HAS_BOOKINGS = new ErrorCode(409509, "开放时段已有有效预约，不能修改或删除", HttpStatus.CONFLICT);
    ErrorCode INVALID_BOOKING_STATUS = new ErrorCode(409510, "当前预约状态不允许执行此操作", HttpStatus.CONFLICT);
    ErrorCode FUTURE_BOOKING_CANNOT_COMPLETE = new ErrorCode(409511, "未来的预约不能标记为已完成", HttpStatus.CONFLICT);
}
