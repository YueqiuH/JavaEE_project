package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.dao.office.MeetingAttendeeMapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IMeetingAttendeeService;
import com.smartcampus.app.service.office.IMeetingService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Meeting;
import com.smartcampus.contract.entity.MeetingAttendee;
import com.smartcampus.contract.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/office/meeting")
@Tag(name = "校园会议与通知发布")
public class MeetingController {
    private static final Map<String, List<String>> AUDIENCE_ROLE_CODES = Map.of(
            "ALL_STUDENTS", List.of("STUDENT"),
            "ALL_TEACHERS", List.of("TEACHER", "COUNSELOR"),
            "STAFF", List.of("STAFF"),
            "ACADEMIC_AFFAIRS", List.of("ADMIN")
    );

    @Autowired private IMeetingService meetingService;
    @Autowired private IMeetingAttendeeService attendeeService;
    @Autowired private INotificationService notificationService;
    @Autowired private MeetingAttendeeMapper attendeeMapper;

    @PostMapping("/publish")
    @RequirePermission(OfficePermissions.MEETING_MANAGE)
    @Transactional
    @Operation(summary = "发布会议并向参会人员推送通知")
    public CommonResult<Meeting> publish(@RequestBody PublishRequest request) {
        if (request == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "会议信息不能为空");
        }
        Meeting meeting = request.getMeeting();
        if (meeting == null || meeting.getTitle() == null || meeting.getTitle().isBlank()
                || meeting.getMeetingDate() == null || meeting.getStartTime() == null || meeting.getEndTime() == null
                || request.getAudienceTypes() == null || request.getAudienceTypes().isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "会议标题、时间和参会范围不能为空");
        }
        if (!meeting.getStartTime().before(meeting.getEndTime())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "会议开始时间必须早于结束时间");
        }

        List<String> audienceTypes = request.getAudienceTypes().stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        List<String> invalidAudienceTypes = audienceTypes.stream()
                .filter(value -> !AUDIENCE_ROLE_CODES.containsKey(value))
                .toList();
        if (audienceTypes.isEmpty() || !invalidAudienceTypes.isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST,
                    invalidAudienceTypes.isEmpty() ? "请选择参会范围" : "无效的参会范围：" + String.join(", ", invalidAudienceTypes));
        }
        List<String> actionCodes = audienceTypes.stream()
                .filter(AUDIENCE_ROLE_CODES::containsKey)
                .flatMap(type -> AUDIENCE_ROLE_CODES.get(type).stream())
                .distinct()
                .toList();
        if (actionCodes.isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "所选参会范围暂无启用用户");
        }
        List<Long> attendeeIds = attendeeMapper.findActiveUserIdsByRoleCodes(actionCodes);
        if (attendeeIds.isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "所选参会范围暂无启用用户");
        }

        meeting.setMeetingId(null);
        meeting.setInitiatorId(CurrentUserContext.require().userId());
        meeting.setCreateTime(new java.sql.Date(System.currentTimeMillis()));
        meetingService.save(meeting);
        for (Long userId : attendeeIds) {
            MeetingAttendee attendee = new MeetingAttendee();
            attendee.setMeetingId(meeting.getMeetingId());
            attendee.setUserId(userId);
            attendee.setStatus("待确认");
            attendeeService.save(attendee);
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle("会议通知：" + meeting.getTitle());
            notification.setContent(meeting.getMeetingDate() + " " + meeting.getStartTime() + "，地点：" + meeting.getLocation());
            notification.setNotifyType("会议通知");
            notification.setIsRead(0);
            notification.setCreateTime(new Date());
            notificationService.save(notification);
        }
        return CommonResult.success(meeting);
    }

    @GetMapping("/list")
    @RequirePermission(OfficePermissions.MEETING_MANAGE)
    @Operation(summary = "查询会议列表")
    public CommonResult<List<Meeting>> list() {
        return CommonResult.success(meetingService.list(new LambdaQueryWrapper<Meeting>()
                .orderByDesc(Meeting::getMeetingDate, Meeting::getStartTime)));
    }

    @GetMapping("/mine")
    @RequirePermission(OfficePermissions.MEETING_SELF)
    @Operation(summary = "查询用户收到的会议")
    public CommonResult<List<Meeting>> myMeetings() {
        Long userId = CurrentUserContext.require().userId();
        List<Long> ids = attendeeService.list(new LambdaQueryWrapper<MeetingAttendee>()
                .eq(MeetingAttendee::getUserId, userId)).stream().map(MeetingAttendee::getMeetingId).toList();
        return CommonResult.success(ids.isEmpty() ? List.of() : meetingService.listByIds(ids));
    }

    @PostMapping("/{meetingId}/reply")
    @RequirePermission(OfficePermissions.MEETING_SELF)
    @Operation(summary = "参会人反馈参会或请假")
    public CommonResult<MeetingAttendee> reply(@PathVariable Long meetingId, @RequestParam String status) {
        if (!List.of("参会", "请假").contains(status)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "反馈状态必须为参会或请假");
        }
        Long userId = CurrentUserContext.require().userId();
        MeetingAttendee attendee = attendeeService.getOne(new LambdaQueryWrapper<MeetingAttendee>()
                .eq(MeetingAttendee::getMeetingId, meetingId).eq(MeetingAttendee::getUserId, userId));
        if (attendee == null) throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "您不在该会议参会名单中");
        attendee.setStatus(status);
        attendee.setReplyTime(new Date());
        attendeeService.updateById(attendee);
        return CommonResult.success(attendee);
    }

    @GetMapping("/{meetingId}/summary")
    @RequirePermission(OfficePermissions.MEETING_MANAGE)
    @Operation(summary = "汇总会议参会反馈情况")
    public CommonResult<Map<String, Long>> summary(@PathVariable Long meetingId) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (String status : List.of("待确认", "参会", "请假")) {
            result.put(status, attendeeService.count(new LambdaQueryWrapper<MeetingAttendee>()
                    .eq(MeetingAttendee::getMeetingId, meetingId).eq(MeetingAttendee::getStatus, status)));
        }
        return CommonResult.success(result);
    }

    @GetMapping("/notifications")
    @RequirePermission(OfficePermissions.NOTIFICATION_SELF_READ)
    @Operation(summary = "查询用户通知")
    public CommonResult<List<Notification>> notifications() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(notificationService.list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).orderByDesc(Notification::getCreateTime)));
    }

    @PostMapping("/notifications/{notifyId}/read")
    @RequirePermission(OfficePermissions.NOTIFICATION_SELF_READ)
    @Operation(summary = "标记通知已读")
    public CommonResult<Boolean> read(@PathVariable Long notifyId) {
        Notification notification = notificationService.getById(notifyId);
        if (notification == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "通知不存在");
        if (!CurrentUserContext.require().userId().equals(notification.getUserId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能修改他人的通知");
        }
        notification.setIsRead(1);
        return CommonResult.success(notificationService.updateById(notification));
    }

    public static class PublishRequest {
        private Meeting meeting;
        private List<String> audienceTypes;

        public Meeting getMeeting() {
            return meeting;
        }

        public void setMeeting(Meeting meeting) {
            this.meeting = meeting;
        }

        public List<String> getAudienceTypes() {
            return audienceTypes;
        }

        public void setAudienceTypes(List<String> audienceTypes) {
            this.audienceTypes = audienceTypes;
        }
    }
}
