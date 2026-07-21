package com.smartcampus.app.controller.office;

import com.smartcampus.app.dao.office.MeetingAttendeeMapper;
import com.smartcampus.app.service.office.IMeetingAttendeeService;
import com.smartcampus.app.service.office.IMeetingService;
import com.smartcampus.app.service.office.INotificationService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.Meeting;
import com.smartcampus.contract.entity.MeetingAttendee;
import com.smartcampus.contract.entity.Notification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.sql.Time;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingControllerTest {

    @Mock
    private IMeetingService meetingService;
    @Mock
    private IMeetingAttendeeService attendeeService;
    @Mock
    private INotificationService notificationService;
    @Mock
    private MeetingAttendeeMapper attendeeMapper;

    @InjectMocks
    private MeetingController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    void publishExpandsAudienceTypesIntoAttendeesAndNotifications() {
        setCurrentUser(8L);
        MeetingController.PublishRequest request = publishRequest(
                List.of("ALL_STUDENTS", "COUNSELORS", "ACADEMIC_AFFAIRS"));
        when(attendeeMapper.findActiveUserIdsByRoleCodes(argThat(roleCodes ->
                new HashSet<>(roleCodes).equals(Set.of("STUDENT", "TEACHER", "ADMIN")))))
                .thenReturn(List.of(2L, 3L, 4L));
        doAnswer(invocation -> {
            Meeting meeting = invocation.getArgument(0);
            meeting.setMeetingId(99L);
            return true;
        }).when(meetingService).save(any(Meeting.class));

        Meeting published = controller.publish(request).getData();

        assertEquals(99L, published.getMeetingId());
        assertEquals(8L, published.getInitiatorId());

        ArgumentCaptor<MeetingAttendee> attendeeCaptor = ArgumentCaptor.forClass(MeetingAttendee.class);
        verify(attendeeService, times(3)).save(attendeeCaptor.capture());
        assertEquals(Set.of(2L, 3L, 4L), attendeeCaptor.getAllValues().stream()
                .map(MeetingAttendee::getUserId).collect(java.util.stream.Collectors.toSet()));
        attendeeCaptor.getAllValues().forEach(attendee -> {
            assertEquals(99L, attendee.getMeetingId());
            assertEquals("待确认", attendee.getStatus());
        });

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService, times(3)).save(notificationCaptor.capture());
        assertEquals(Set.of(2L, 3L, 4L), notificationCaptor.getAllValues().stream()
                .map(Notification::getUserId).collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void publishRejectsUnknownAudienceType() {
        setCurrentUser(8L);
        MeetingController.PublishRequest request = publishRequest(List.of("ALL_STUDENTS", "USER_IDS"));

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.publish(request));

        assertEquals(400100, exception.getErrorCode().getCode());
        verify(attendeeMapper, never()).findActiveUserIdsByRoleCodes(any());
        verify(meetingService, never()).save(any(Meeting.class));
    }

    @Test
    void publishRejectsAudienceWithoutActiveUsers() {
        setCurrentUser(8L);
        MeetingController.PublishRequest request = publishRequest(List.of("STAFF"));
        when(attendeeMapper.findActiveUserIdsByRoleCodes(List.of("STAFF"))).thenReturn(List.of());

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.publish(request));

        assertEquals(400100, exception.getErrorCode().getCode());
        verify(meetingService, never()).save(any(Meeting.class));
    }

    private MeetingController.PublishRequest publishRequest(List<String> audienceTypes) {
        Meeting meeting = new Meeting();
        meeting.setTitle("教学工作会议");
        meeting.setContent("讨论本周教学安排");
        meeting.setMeetingDate(Date.valueOf("2026-07-20"));
        meeting.setStartTime(Time.valueOf("09:00:00"));
        meeting.setEndTime(Time.valueOf("10:00:00"));
        meeting.setLocation("行政楼 201");

        MeetingController.PublishRequest request = new MeetingController.PublishRequest();
        request.setMeeting(meeting);
        request.setAudienceTypes(audienceTypes);
        return request;
    }

    private void setCurrentUser(Long userId) {
        CurrentUserContext.set(new AuthSession(userId, "manager", 3,
                Set.of("STAFF"), Set.of("meeting:manage"), 1L));
    }
}
