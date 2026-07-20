package com.smartcampus.app.service.student;

import com.smartcampus.app.dao.student.LabBookingMapper;
import com.smartcampus.app.dao.student.LabBookingNoticeMapper;
import com.smartcampus.app.dao.student.LabMapper;
import com.smartcampus.app.dao.student.LabOpenSlotMapper;
import com.smartcampus.app.dao.student.LabResourceMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.dto.student.LabBookingRequest;
import com.smartcampus.contract.dto.student.LabOpenSlotRequest;
import com.smartcampus.contract.entity.Lab;
import com.smartcampus.contract.entity.LabBooking;
import com.smartcampus.contract.entity.LabBookingNotice;
import com.smartcampus.contract.entity.LabOpenSlot;
import com.smartcampus.contract.entity.Student;
import com.smartcampus.contract.vo.student.LabBookingVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LabBookingServiceTest {

    @Mock private LabMapper labMapper;
    @Mock private LabResourceMapper resourceMapper;
    @Mock private LabOpenSlotMapper slotMapper;
    @Mock private LabBookingMapper bookingMapper;
    @Mock private LabBookingNoticeMapper noticeMapper;

    private LabBookingService service;

    @BeforeEach
    void setUp() {
        service = new LabBookingService(labMapper, resourceMapper, slotMapper, bookingMapper, noticeMapper);
    }

    @AfterEach
    void clearCurrentUser() {
        CurrentUserContext.clear();
    }

    @Test
    void studentCannotBookWhenLabCapacityIsFull() {
        prepareStudentBooking();
        when(bookingMapper.countStudentActiveLabBookings(eq(10L), eq(1L), any(), any())).thenReturn(0);
        when(bookingMapper.countActiveLabBookings(eq(10L), any(), any())).thenReturn(1);

        assertCode(() -> service.createBooking(bookingRequest()), LabBookingErrorCodes.LAB_CAPACITY_FULL.getCode());
        verify(bookingMapper, never()).insert(any(LabBooking.class));
    }

    @Test
    void teacherCanListLabsWithoutStatusFilter() {
        CurrentUserContext.set(teacherSession());
        when(labMapper.selectLabPage(any(Page.class), eq(2L), eq(false), isNull()))
                .thenReturn(new Page<>());

        assertThat(service.listLabs(1, 10, null).getRecords()).isEmpty();
    }

    @Test
    void studentCannotCreateSecondActiveBookingForSameLab() {
        prepareStudentBooking();
        when(bookingMapper.countStudentActiveLabBookings(eq(10L), eq(1L), any(), any())).thenReturn(1);

        assertCode(() -> service.createBooking(bookingRequest()), LabBookingErrorCodes.ACTIVE_BOOKING_EXISTS.getCode());
        verify(bookingMapper, never()).insert(any(LabBooking.class));
    }

    @Test
    void successfulDailyBookingCreatesReservationAndNotice() {
        prepareStudentBooking();
        when(bookingMapper.countStudentActiveLabBookings(eq(10L), eq(1L), any(), any())).thenReturn(0);
        when(bookingMapper.countActiveLabBookings(eq(10L), any(), any())).thenReturn(0);
        doAnswer(invocation -> {
            LabBooking booking = invocation.getArgument(0);
            booking.setBookingId(30L);
            return 1;
        }).when(bookingMapper).insert(any(LabBooking.class));
        when(noticeMapper.insert(any(LabBookingNotice.class))).thenReturn(1);
        LabBookingVo view = new LabBookingVo();
        view.setBookingId(30L);
        view.setStatusCode(LabBookingStatus.RESERVED.code());
        when(bookingMapper.selectBookingView(30L)).thenReturn(view);

        LabBookingVo result = service.createBooking(bookingRequest());

        assertThat(result.getStatus()).isEqualTo("RESERVED");
        ArgumentCaptor<LabBooking> saved = ArgumentCaptor.forClass(LabBooking.class);
        verify(bookingMapper).insert(saved.capture());
        assertThat(saved.getValue().getBookingDate()).isEqualTo(LocalDate.now());
        assertThat(saved.getValue().getResourceId()).isNull();
        assertThat(saved.getValue().getExpiresAt()).isNotNull();
        ArgumentCaptor<LabBookingNotice> notice = ArgumentCaptor.forClass(LabBookingNotice.class);
        verify(noticeMapper).insert(notice.capture());
        assertThat(notice.getValue().getTitle()).isEqualTo("实验室预约成功");
        assertThat(notice.getValue().getStudentId()).isEqualTo(1L);
    }

    @Test
    void studentCanCheckInReservedBookingAndCheckOut() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        LabBooking reserved = booking(30L, 1L, LabBookingStatus.RESERVED);
        reserved.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(20));
        when(bookingMapper.selectById(30L)).thenReturn(reserved);
        Lab lab = lab(10L, 2L);
        lab.setStatus(1);
        when(labMapper.selectByIdForUpdate(10L)).thenReturn(lab);
        when(bookingMapper.update(isNull(), any())).thenReturn(1);
        LabBookingVo checkedIn = new LabBookingVo();
        checkedIn.setStatusCode(LabBookingStatus.CHECKED_IN.code());
        when(bookingMapper.selectBookingView(30L)).thenReturn(checkedIn);

        assertThat(service.checkIn(30L).getStatus()).isEqualTo("CHECKED_IN");

        LabBooking active = booking(30L, 1L, LabBookingStatus.CHECKED_IN);
        when(bookingMapper.selectById(30L)).thenReturn(active);
        LabBookingVo checkedOut = new LabBookingVo();
        checkedOut.setStatusCode(LabBookingStatus.CHECKED_OUT.code());
        when(bookingMapper.selectBookingView(30L)).thenReturn(checkedOut);

        assertThat(service.checkOut(30L).getStatus()).isEqualTo("CHECKED_OUT");
        verify(bookingMapper, times(2)).update(isNull(), any());
    }

    @Test
    void studentGetsExplicitErrorWhenCheckInHasExpired() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        LabBooking expired = booking(30L, 1L, LabBookingStatus.EXPIRED);
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(bookingMapper.selectById(30L)).thenReturn(expired);

        assertCode(() -> service.checkIn(30L), LabBookingErrorCodes.CHECK_IN_EXPIRED.getCode());
        verify(bookingMapper, never()).update(any(), any());
    }

    @Test
    void studentCannotCancelAnotherStudentsBooking() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        LabBooking booking = booking(30L, 99L, LabBookingStatus.RESERVED);
        when(bookingMapper.selectById(30L)).thenReturn(booking);

        assertCode(() -> service.cancelBooking(30L), LabBookingErrorCodes.BOOKING_NOT_OWNED.getCode());
        verify(bookingMapper, never()).update(any(), any());
    }

    @Test
    void teacherCannotMaintainAnotherTeachersSlot() {
        CurrentUserContext.set(teacherSession());
        LabOpenSlot slot = slot(40L, 10L);
        when(slotMapper.selectById(40L)).thenReturn(slot);
        when(labMapper.selectById(10L)).thenReturn(lab(10L, 88L));

        assertCode(() -> service.updateSlot(40L, slotRequest()), LabBookingErrorCodes.LAB_NOT_OWNED.getCode());
        verify(slotMapper, never()).updateById(any(LabOpenSlot.class));
    }

    @Test
    void teacherCannotCreateOverlappingOpenSlot() {
        CurrentUserContext.set(teacherSession());
        when(labMapper.selectById(10L)).thenReturn(lab(10L, 2L));
        when(slotMapper.countOverlappingSlots(eq(10L), any(), eq(3), eq(4), isNull())).thenReturn(1);

        assertCode(() -> service.createSlot(10L, slotRequest()), LabBookingErrorCodes.SLOT_OVERLAP.getCode());
        verify(slotMapper, never()).insert(any(LabOpenSlot.class));
    }

    @Test
    void teacherCannotDeleteSlotWithActiveBookings() {
        CurrentUserContext.set(teacherSession());
        LabOpenSlot slot = slot(40L, 10L);
        when(slotMapper.selectById(40L)).thenReturn(slot);
        when(labMapper.selectById(10L)).thenReturn(lab(10L, 2L));
        when(slotMapper.countActiveBookings(10L, slot.getOpenDate(), 3, 4)).thenReturn(1);

        assertCode(() -> service.deleteSlot(40L), LabBookingErrorCodes.SLOT_HAS_BOOKINGS.getCode());
        verify(slotMapper, never()).deleteById(40L);
    }

    @Test
    void teacherCannotCompleteBookingFromAnotherLab() {
        CurrentUserContext.set(teacherSession());
        when(bookingMapper.selectById(30L)).thenReturn(booking(30L, 1L, LabBookingStatus.CHECKED_IN));
        when(labMapper.selectById(10L)).thenReturn(lab(10L, 88L));

        assertCode(() -> service.completeBooking(30L), LabBookingErrorCodes.LAB_NOT_OWNED.getCode());
        verify(bookingMapper, never()).update(any(), any());
    }

    @Test
    void studentCannotReadAnotherStudentsNotice() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        LabBookingNotice notice = new LabBookingNotice();
        notice.setNoticeId(50L);
        notice.setStudentId(99L);
        when(noticeMapper.selectById(50L)).thenReturn(notice);

        assertCode(() -> service.markNoticeRead(50L), LabBookingErrorCodes.NOTICE_NOT_OWNED.getCode());
        verify(noticeMapper, never()).updateById(any(LabBookingNotice.class));
    }

    private void prepareStudentBooking() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        Lab lab = lab(10L, 2L);
        lab.setLabName("计算机实践实验室");
        lab.setStatus(1);
        lab.setCapacity(1);
        when(labMapper.selectByIdForUpdate(10L)).thenReturn(lab);
    }

    private LabBookingRequest bookingRequest() {
        LabBookingRequest request = new LabBookingRequest();
        request.setLabId(10L);
        request.setPurpose("课程实践");
        return request;
    }

    private LabOpenSlotRequest slotRequest() {
        LabOpenSlotRequest request = new LabOpenSlotRequest();
        request.setOpenDate(LocalDate.now().plusDays(2));
        request.setStartPeriod(3);
        request.setEndPeriod(4);
        return request;
    }

    private Lab lab(Long id, Long managerId) {
        Lab lab = new Lab();
        lab.setLabId(id);
        lab.setManagerId(managerId);
        return lab;
    }

    private LabOpenSlot slot(Long id, Long labId) {
        LabOpenSlot slot = new LabOpenSlot();
        slot.setSlotId(id);
        slot.setLabId(labId);
        slot.setOpenDate(LocalDate.now().plusDays(2));
        slot.setStartPeriod(3);
        slot.setEndPeriod(4);
        return slot;
    }

    private LabBooking booking(Long id, Long studentId, LabBookingStatus status) {
        LabBooking booking = new LabBooking();
        booking.setBookingId(id);
        booking.setLabId(10L);
        booking.setStudentId(studentId);
        booking.setBookingDate(LocalDate.now());
        booking.setStatus(status.code());
        return booking;
    }

    private Student student(Long id) {
        Student student = new Student();
        student.setStudentId(id);
        student.setStudentNo(600001L);
        return student;
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of(
                "lab:booking:create", "lab:booking:read-self", "lab:booking:cancel-self",
                "lab:booking:check-in-self", "lab:booking:check-out-self",
                "lab:notice:read-self", "lab:notice:mark-self", "lab:read"), 0);
    }

    private AuthSession teacherSession() {
        return new AuthSession(2L, "700001", 2, Set.of("TEACHER"), Set.of(
                "lab:manage-self", "lab:resource:manage-self", "lab:slot:manage-self",
                "lab:booking:read-managed", "lab:booking:complete-managed", "lab:read"), 0);
    }

    private void assertCode(ThrowingAction action, int code) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(error -> ((BusinessException) error).getErrorCode().getCode())
                .isEqualTo(code);
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run();
    }
}
