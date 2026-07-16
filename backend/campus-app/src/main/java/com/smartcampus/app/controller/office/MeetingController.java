package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.service.office.IMeetingAttendeeService;
import com.smartcampus.app.service.office.IMeetingService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Meeting;
import com.smartcampus.contract.entity.MeetingAttendee;
import com.smartcampus.contract.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/office/meeting")
@Tag(name = "校园会议与通知发布")
public class MeetingController {
    @Autowired private IMeetingService meetingService;
    @Autowired private IMeetingAttendeeService attendeeService;
    @Autowired private INotificationService notificationService;

    @PostMapping("/publish")
    @Transactional
    @Operation(summary = "发布会议并向参会人员推送通知")
    public CommonResult<Meeting> publish(@RequestBody PublishRequest request) {
        Meeting meeting = request.getMeeting();
        if (meeting == null || meeting.getTitle() == null || meeting.getInitiatorId() == null
                || request.getAttendeeIds() == null || request.getAttendeeIds().isEmpty())
            return CommonResult.error(1401, "会议信息和参会人员不能为空");
        meeting.setCreateTime(new java.sql.Date(System.currentTimeMillis()));
        meetingService.save(meeting);
        for (Long userId : request.getAttendeeIds().stream().distinct().toList()) {
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
    @Operation(summary = "查询会议列表")
    public CommonResult<List<Meeting>> list() {
        return CommonResult.success(meetingService.list(new LambdaQueryWrapper<Meeting>()
                .orderByDesc(Meeting::getMeetingDate, Meeting::getStartTime)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户收到的会议")
    public CommonResult<List<Meeting>> userMeetings(@PathVariable Long userId) {
        List<Long> ids = attendeeService.list(new LambdaQueryWrapper<MeetingAttendee>()
                .eq(MeetingAttendee::getUserId, userId)).stream().map(MeetingAttendee::getMeetingId).toList();
        return CommonResult.success(ids.isEmpty() ? List.of() : meetingService.listByIds(ids));
    }

    @PostMapping("/{meetingId}/reply")
    @Operation(summary = "参会人反馈参会或请假")
    public CommonResult<MeetingAttendee> reply(@PathVariable Long meetingId, @RequestParam Long userId, @RequestParam String status) {
        if (!List.of("参会", "请假").contains(status)) return CommonResult.error(1402, "反馈状态必须为参会或请假");
        MeetingAttendee attendee = attendeeService.getOne(new LambdaQueryWrapper<MeetingAttendee>()
                .eq(MeetingAttendee::getMeetingId, meetingId).eq(MeetingAttendee::getUserId, userId));
        if (attendee == null) return CommonResult.error(1403, "您不在该会议参会名单中");
        attendee.setStatus(status);
        attendee.setReplyTime(new Date());
        attendeeService.updateById(attendee);
        return CommonResult.success(attendee);
    }

    @GetMapping("/{meetingId}/summary")
    @Operation(summary = "汇总会议参会反馈情况")
    public CommonResult<Map<String, Long>> summary(@PathVariable Long meetingId) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (String status : List.of("待确认", "参会", "请假")) {
            result.put(status, attendeeService.count(new LambdaQueryWrapper<MeetingAttendee>()
                    .eq(MeetingAttendee::getMeetingId, meetingId).eq(MeetingAttendee::getStatus, status)));
        }
        return CommonResult.success(result);
    }

    @GetMapping("/notification/{userId}")
    @Operation(summary = "查询用户通知")
    public CommonResult<List<Notification>> notifications(@PathVariable Long userId) {
        return CommonResult.success(notificationService.list(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId).orderByDesc(Notification::getCreateTime)));
    }

    @PostMapping("/notification/{notifyId}/read")
    @Operation(summary = "标记通知已读")
    public CommonResult<Boolean> read(@PathVariable Long notifyId) {
        Notification notification = notificationService.getById(notifyId);
        if (notification == null) return CommonResult.error(1404, "通知不存在");
        notification.setIsRead(1);
        return CommonResult.success(notificationService.updateById(notification));
    }

    @Data
    public static class PublishRequest {
        private Meeting meeting;
        private List<Long> attendeeIds;
    }
}
