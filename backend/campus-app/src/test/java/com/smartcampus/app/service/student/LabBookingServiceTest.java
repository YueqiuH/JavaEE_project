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
import com.smartcampus.contract.entity.LabResource;
import com.smartcampus.contract.entity.StudentEntity;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    void studentCannotBookOutsideOpenSlot() {
        prepareStudentBooking();
        when(slotMapper.countContainingSlots(eq(10L), any(), eq(3), eq(4))).thenReturn(0);

        assertCode(() -> service.createBooking(bookingRequest()), LabBookingErrorCodes.OUTSIDE_OPEN_SLOT.getCode());
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
    void studentCannotBookOccupiedResource() {
        prepareStudentBooking();
        when(slotMapper.countContainingSlots(eq(10L), any(), eq(3), eq(4))).thenReturn(1);
        when(bookingMapper.countResourceConflicts(eq(20L), any(), eq(3), eq(4))).thenReturn(1);

        assertCode(() -> service.createBooking(bookingRequest()), LabBookingErrorCodes.RESOURCE_CONFLICT.getCode());
        verify(bookingMapper, never()).insert(any(LabBooking.class));
    }

    @Test
    void studentCannotCreateOverlappingPersonalBooking() {
        prepareStudentBooking();
        when(slotMapper.countContainingSlots(eq(10L), any(), eq(3), eq(4))).thenReturn(1);
        when(bookingMapper.countResourceConflicts(eq(20L), any(), eq(3), eq(4))).thenReturn(0);
        when(bookingMapper.countStudentConflicts(eq(1L), any(), eq(3), eq(4))).thenReturn(1);

        assertCode(() -> service.createBooking(bookingRequest()), LabBookingErrorCodes.STUDENT_TIME_CONFLICT.getCode());
        verify(bookingMapper, never()).insert(any(LabBooking.class));
    }

    @Test
    void successfulBookingOccupiesEveryPeriodAndCreatesNotice() {
        prepareStudentBooking();
        when(slotMapper.countContainingSlots(eq(10L), any(), eq(3), eq(4))).thenReturn(1);
        when(bookingMapper.countResourceConflicts(eq(20L), any(), eq(3), eq(4))).thenReturn(0);
        when(bookingMapper.countStudentConflicts(eq(1L), any(), eq(3), eq(4))).thenReturn(0);
        doAnswer(invocation -> {
            LabBooking booking = invocation.getArgument(0);
            booking.setBookingId(30L);
            return 1;
        }).when(bookingMapper).insert(any(LabBooking.class));
        when(bookingMapper.insertPeriod(eq(30L), eq(20L), eq(1L), any(), anyInt())).thenReturn(1);
        when(noticeMapper.insert(any(LabBookingNotice.class))).thenReturn(1);
        LabBookingVo view = new LabBookingVo();
        view.setBookingId(30L);
        view.setStatusCode(LabBookingStatus.BOOKED.code());
        when(bookingMapper.selectBookingView(30L)).thenReturn(view);

        LabBookingVo result = service.createBooking(bookingRequest());

        assertThat(result.getStatus()).isEqualTo("BOOKED");
        verify(bookingMapper, times(2)).insertPeriod(eq(30L), eq(20L), eq(1L), any(), anyInt());
        ArgumentCaptor<LabBookingNotice> notice = ArgumentCaptor.forClass(LabBookingNotice.class);
        verify(noticeMapper).insert(notice.capture());
        assertThat(notice.getValue().getTitle()).isEqualTo("实验室预约成功");
        assertThat(notice.getValue().getStudentId()).isEqualTo(1L);
    }

    @Test
    void studentCannotCancelAnotherStudentsBooking() {
        CurrentUserContext.set(studentSession());
        when(labMapper.selectStudentByNo(600001L)).thenReturn(student(1L));
        LabBooking booking = booking(30L, 99L, LabBookingStatus.BOOKED);
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
        when(bookingMapper.selectById(30L)).thenReturn(booking(30L, 1L, LabBookingStatus.BOOKED));
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
        LabResource resource = new LabResource();
        resource.setResourceId(20L);
        resource.setLabId(10L);
        resource.setResourceName("图形工作站 A01");
        resource.setStatus(1);
        when(resourceMapper.selectById(20L)).thenReturn(resource);
        Lab lab = lab(10L, 2L);
        lab.setLabName("计算机实践实验室");
        lab.setStatus(1);
        when(labMapper.selectById(10L)).thenReturn(lab);
    }

    private LabBookingRequest bookingRequest() {
        LabBookingRequest request = new LabBookingRequest();
        request.setResourceId(20L);
        request.setBookingDate(LocalDate.now().plusDays(2));
        request.setStartPeriod(3);
        request.setEndPeriod(4);
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
        booking.setBookingDate(LocalDate.now().plusDays(2));
        booking.setStatus(status.code());
        return booking;
    }

    private StudentEntity student(Long id) {
        StudentEntity student = new StudentEntity();
        student.setStudentId(id);
        student.setStudentNo(600001L);
        return student;
    }

    private AuthSession studentSession() {
        return new AuthSession(1L, "600001", 1, Set.of("STUDENT"), Set.of(
                "lab:booking:create", "lab:booking:read-self", "lab:booking:cancel-self",
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
