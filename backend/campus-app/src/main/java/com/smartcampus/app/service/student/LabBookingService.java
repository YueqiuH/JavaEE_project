package com.smartcampus.app.service.student;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartcampus.app.dao.student.LabBookingMapper;
import com.smartcampus.app.dao.student.LabBookingNoticeMapper;
import com.smartcampus.app.dao.student.LabMapper;
import com.smartcampus.app.dao.student.LabOpenSlotMapper;
import com.smartcampus.app.dao.student.LabResourceMapper;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.enums.GlobalErrorCodeConstants;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.LabBookingRequest;
import com.smartcampus.contract.dto.student.LabOpenSlotRequest;
import com.smartcampus.contract.dto.student.LabRequest;
import com.smartcampus.contract.dto.student.LabResourceRequest;
import com.smartcampus.contract.entity.Lab;
import com.smartcampus.contract.entity.LabBooking;
import com.smartcampus.contract.entity.LabBookingNotice;
import com.smartcampus.contract.entity.LabOpenSlot;
import com.smartcampus.contract.entity.LabResource;
import com.smartcampus.contract.entity.StudentEntity;
import com.smartcampus.contract.vo.student.LabBookingNoticeVo;
import com.smartcampus.contract.vo.student.LabBookingVo;
import com.smartcampus.contract.vo.student.LabOpenSlotVo;
import com.smartcampus.contract.vo.student.LabResourceVo;
import com.smartcampus.contract.vo.student.LabVo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LabBookingService {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String TEACHER_ROLE = "TEACHER";
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final LabMapper labMapper;
    private final LabResourceMapper resourceMapper;
    private final LabOpenSlotMapper slotMapper;
    private final LabBookingMapper bookingMapper;
    private final LabBookingNoticeMapper noticeMapper;

    public LabBookingService(
            LabMapper labMapper,
            LabResourceMapper resourceMapper,
            LabOpenSlotMapper slotMapper,
            LabBookingMapper bookingMapper,
            LabBookingNoticeMapper noticeMapper) {
        this.labMapper = labMapper;
        this.resourceMapper = resourceMapper;
        this.slotMapper = slotMapper;
        this.bookingMapper = bookingMapper;
        this.noticeMapper = noticeMapper;
    }

    public PageResult<LabVo> listLabs(long page, long size, String statusName) {
        AuthSession session = requireAnyRole("lab:read");
        expireStaleBookings();
        boolean student = session.roles().contains(STUDENT_ROLE);
        Long managerId = student ? null : session.userId();
        Integer status = student ? Integer.valueOf(1) : parseAvailabilityStatus(statusName);
        Page<LabVo> query = new Page<>(page, size);
        IPage<LabVo> result = labMapper.selectLabPage(query, managerId, student, status);
        result.getRecords().forEach(this::translateLab);
        return PageResult.from(result);
    }

    public LabVo getLab(Long id) {
        AuthSession session = requireAnyRole("lab:read");
        LabVo lab = requireLabView(id);
        if (session.roles().contains(STUDENT_ROLE)) {
            if (!Objects.equals(lab.getStatusCode(), 1)) throw new BusinessException(LabBookingErrorCodes.LAB_UNAVAILABLE);
        } else if (!session.userId().equals(lab.getManagerId())) {
            throw new BusinessException(LabBookingErrorCodes.LAB_NOT_OWNED);
        }
        return lab;
    }

    @Transactional
    public LabVo createLab(LabRequest request) {
        AuthSession session = requireTeacher("lab:manage-self");
        LocalDateTime now = LocalDateTime.now();
        Lab lab = new Lab();
        lab.setLabNo(createNumber("LAB"));
        lab.setManagerId(session.userId());
        copyLab(request, lab);
        lab.setCreatedAt(now);
        lab.setUpdatedAt(now);
        if (labMapper.insert(lab) != 1) throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        return requireLabView(lab.getLabId());
    }

    @Transactional
    public LabVo updateLab(Long id, LabRequest request) {
        requireTeacher("lab:manage-self");
        requireOwnedLab(id);
        Lab update = new Lab();
        update.setLabId(id);
        copyLab(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        if (labMapper.updateById(update) != 1) throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        return requireLabView(id);
    }

    public List<LabResourceVo> listResources(Long labId) {
        AuthSession session = requireAnyRole("lab:read");
        boolean student = session.roles().contains(STUDENT_ROLE);
        if (labId != null) getLab(labId);
        List<LabResourceVo> resources = resourceMapper.selectResourceList(
                labId, student ? null : session.userId(), student);
        resources.forEach(this::translateResource);
        return resources;
    }

    @Transactional
    public LabResourceVo createResource(Long labId, LabResourceRequest request) {
        requireTeacher("lab:resource:manage-self");
        requireOwnedLab(labId);
        LocalDateTime now = LocalDateTime.now();
        LabResource resource = new LabResource();
        resource.setLabId(labId);
        copyResource(request, resource);
        resource.setCreatedAt(now);
        resource.setUpdatedAt(now);
        try {
            if (resourceMapper.insert(resource) != 1) throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        return requireResourceView(resource.getResourceId());
    }

    @Transactional
    public LabResourceVo updateResource(Long id, LabResourceRequest request) {
        requireTeacher("lab:resource:manage-self");
        LabResource current = requireResource(id);
        requireOwnedLab(current.getLabId());
        LabResource update = new LabResource();
        update.setResourceId(id);
        copyResource(request, update);
        update.setUpdatedAt(LocalDateTime.now());
        try {
            if (resourceMapper.updateById(update) != 1) throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
        return requireResourceView(id);
    }

    public PageResult<LabOpenSlotVo> listSlots(long page, long size, Long labId) {
        AuthSession session = requireAnyRole("lab:read");
        boolean student = session.roles().contains(STUDENT_ROLE);
        if (labId != null) getLab(labId);
        Page<LabOpenSlotVo> query = new Page<>(page, size);
        IPage<LabOpenSlotVo> result = slotMapper.selectSlotPage(
                query, labId, student ? null : session.userId(), student);
        return PageResult.from(result);
    }

    @Transactional
    public LabOpenSlotVo createSlot(Long labId, LabOpenSlotRequest request) {
        AuthSession session = requireTeacher("lab:slot:manage-self");
        requireOwnedLab(labId);
        validateSlot(request, labId, null);
        LocalDateTime now = LocalDateTime.now();
        LabOpenSlot slot = new LabOpenSlot();
        slot.setLabId(labId);
        slot.setOpenDate(request.getOpenDate());
        slot.setStartPeriod(request.getStartPeriod());
        slot.setEndPeriod(request.getEndPeriod());
        slot.setCreatedBy(session.userId());
        slot.setCreatedAt(now);
        slot.setUpdatedAt(now);
        if (slotMapper.insert(slot) != 1) throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        return requireSlotView(slot.getSlotId());
    }

    @Transactional
    public LabOpenSlotVo updateSlot(Long id, LabOpenSlotRequest request) {
        requireTeacher("lab:slot:manage-self");
        LabOpenSlot current = requireSlot(id);
        requireOwnedLab(current.getLabId());
        requireSlotWithoutBookings(current);
        validateSlot(request, current.getLabId(), id);
        LabOpenSlot update = new LabOpenSlot();
        update.setSlotId(id);
        update.setOpenDate(request.getOpenDate());
        update.setStartPeriod(request.getStartPeriod());
        update.setEndPeriod(request.getEndPeriod());
        update.setUpdatedAt(LocalDateTime.now());
        if (slotMapper.updateById(update) != 1) throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        return requireSlotView(id);
    }

    @Transactional
    public void deleteSlot(Long id) {
        requireTeacher("lab:slot:manage-self");
        LabOpenSlot slot = requireSlot(id);
        requireOwnedLab(slot.getLabId());
        requireSlotWithoutBookings(slot);
        if (slotMapper.deleteById(id) != 1) throw new BusinessException(GlobalErrorCodeConstants.DELETE_ERROR);
    }

    public PageResult<LabBookingVo> listMyBookings(long page, long size, String statusName) {
        StudentEntity student = requireStudent("lab:booking:read-self");
        expireStaleBookings();
        return listBookings(page, size, student.getStudentId(), null, parseBookingStatus(statusName));
    }

    public PageResult<LabBookingVo> listManagedBookings(long page, long size, String statusName) {
        AuthSession session = requireTeacher("lab:booking:read-managed");
        expireStaleBookings();
        return listBookings(page, size, null, session.userId(), parseBookingStatus(statusName));
    }

    public LabBookingVo getBooking(Long id) {
        expireStaleBookings();
        AuthSession session = CurrentUserContext.require();
        LabBookingVo booking = requireBookingView(id);
        if (session.roles().contains(STUDENT_ROLE)) {
            StudentEntity student = requireStudent("lab:booking:read-self");
            if (!student.getStudentId().equals(booking.getStudentId())) {
                throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_OWNED);
            }
        } else {
            requireTeacher("lab:booking:read-managed");
            Lab lab = requireLab(booking.getLabId());
            if (!session.userId().equals(lab.getManagerId())) {
                throw new BusinessException(LabBookingErrorCodes.LAB_NOT_OWNED);
            }
        }
        return booking;
    }

    @Transactional
    public LabBookingVo createBooking(LabBookingRequest request) {
        StudentEntity student = requireStudent("lab:booking:create");
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        bookingMapper.expireStaleBookings(today, now);
        Lab lab = requireLabForUpdate(request.getLabId());
        if (!Objects.equals(lab.getStatus(), 1)) throw new BusinessException(LabBookingErrorCodes.LAB_UNAVAILABLE);
        if (bookingMapper.countStudentActiveLabBookings(
                lab.getLabId(), student.getStudentId(), today, now) > 0) {
            throw new BusinessException(LabBookingErrorCodes.ACTIVE_BOOKING_EXISTS);
        }
        if (bookingMapper.countActiveLabBookings(lab.getLabId(), today, now) >= lab.getCapacity()) {
            throw new BusinessException(LabBookingErrorCodes.LAB_CAPACITY_FULL);
        }

        LabBooking booking = new LabBooking();
        booking.setBookingNo(createNumber("LB"));
        booking.setLabId(lab.getLabId());
        booking.setStudentId(student.getStudentId());
        booking.setBookingDate(today);
        booking.setPurpose(request.getPurpose().trim());
        booking.setStatus(LabBookingStatus.RESERVED.code());
        booking.setCreateTime(now);
        booking.setExpiresAt(now.plusMinutes(30));
        booking.setUpdatedAt(now);
        try {
            if (bookingMapper.insert(booking) != 1) throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(LabBookingErrorCodes.ACTIVE_BOOKING_EXISTS);
        }

        LabBookingNotice notice = new LabBookingNotice();
        notice.setBookingId(booking.getBookingId());
        notice.setStudentId(student.getStudentId());
        notice.setTitle("实验室预约成功");
        notice.setContent("已获得" + lab.getLabName() + "今日使用名额，请在30分钟内完成签到。");
        notice.setIsRead(0);
        notice.setCreatedAt(now);
        if (noticeMapper.insert(notice) != 1) throw new BusinessException(GlobalErrorCodeConstants.ADD_ERROR);
        return requireBookingView(booking.getBookingId());
    }

    @Transactional
    public LabBookingVo cancelBooking(Long id) {
        StudentEntity student = requireStudent("lab:booking:cancel-self");
        expireStaleBookings();
        LabBooking booking = requireBooking(id);
        if (!student.getStudentId().equals(booking.getStudentId())) {
            throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_OWNED);
        }
        requireBookingToday(booking);
        requireBookingStatus(booking, LabBookingStatus.RESERVED);
        UpdateWrapper<LabBooking> update = new UpdateWrapper<LabBooking>()
                .eq("booking_id", id)
                .eq("status", LabBookingStatus.RESERVED.code())
                .set("status", LabBookingStatus.CANCELLED.code())
                .set("cancelled_at", LocalDateTime.now())
                .set("updated_at", LocalDateTime.now());
        updateBooking(update);
        return requireBookingView(id);
    }

    @Transactional
    public LabBookingVo checkIn(Long id) {
        StudentEntity student = requireStudent("lab:booking:check-in-self");
        expireStaleBookings();
        LabBooking booking = requireBooking(id);
        requireOwnedBooking(booking, student.getStudentId());
        requireBookingToday(booking);
        if (Objects.equals(booking.getStatus(), LabBookingStatus.EXPIRED.code())
                || (Objects.equals(booking.getStatus(), LabBookingStatus.RESERVED.code())
                && booking.getExpiresAt() != null
                && !booking.getExpiresAt().isAfter(LocalDateTime.now()))) {
            throw new BusinessException(LabBookingErrorCodes.CHECK_IN_EXPIRED);
        }
        requireBookingStatus(booking, LabBookingStatus.RESERVED);
        Lab lab = requireLabForUpdate(booking.getLabId());
        if (!Objects.equals(lab.getStatus(), 1)) throw new BusinessException(LabBookingErrorCodes.LAB_UNAVAILABLE);
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<LabBooking> update = new UpdateWrapper<LabBooking>()
                .eq("booking_id", id)
                .eq("status", LabBookingStatus.RESERVED.code())
                .set("status", LabBookingStatus.CHECKED_IN.code())
                .set("check_in_at", now)
                .set("updated_at", now);
        updateBooking(update);
        return requireBookingView(id);
    }

    @Transactional
    public LabBookingVo checkOut(Long id) {
        StudentEntity student = requireStudent("lab:booking:check-out-self");
        LabBooking booking = requireBooking(id);
        requireOwnedBooking(booking, student.getStudentId());
        requireBookingToday(booking);
        return checkOutBooking(booking);
    }

    @Transactional
    public LabBookingVo completeBooking(Long id) {
        AuthSession session = requireTeacher("lab:booking:complete-managed");
        LabBooking booking = requireBooking(id);
        Lab lab = requireLab(booking.getLabId());
        if (!session.userId().equals(lab.getManagerId())) {
            throw new BusinessException(LabBookingErrorCodes.LAB_NOT_OWNED);
        }
        requireBookingToday(booking);
        return checkOutBooking(booking);
    }

    private LabBookingVo checkOutBooking(LabBooking booking) {
        requireBookingStatus(booking, LabBookingStatus.CHECKED_IN);
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<LabBooking> update = new UpdateWrapper<LabBooking>()
                .eq("booking_id", booking.getBookingId())
                .eq("status", LabBookingStatus.CHECKED_IN.code())
                .set("status", LabBookingStatus.CHECKED_OUT.code())
                .set("check_out_at", now)
                .set("completed_at", now)
                .set("updated_at", now);
        updateBooking(update);
        return requireBookingView(booking.getBookingId());
    }

    public PageResult<LabBookingNoticeVo> listNotices(long page, long size, Integer isRead) {
        StudentEntity student = requireStudent("lab:notice:read-self");
        Page<LabBookingNoticeVo> query = new Page<>(page, size);
        return PageResult.from(noticeMapper.selectNoticePage(query, student.getStudentId(), isRead));
    }

    @Transactional
    public LabBookingNoticeVo markNoticeRead(Long id) {
        StudentEntity student = requireStudent("lab:notice:mark-self");
        LabBookingNotice notice = noticeMapper.selectById(id);
        if (notice == null) throw new BusinessException(LabBookingErrorCodes.NOTICE_NOT_FOUND);
        if (!student.getStudentId().equals(notice.getStudentId())) {
            throw new BusinessException(LabBookingErrorCodes.NOTICE_NOT_OWNED);
        }
        if (!Objects.equals(notice.getIsRead(), 1)) {
            LabBookingNotice update = new LabBookingNotice();
            update.setNoticeId(id);
            update.setIsRead(1);
            update.setReadAt(LocalDateTime.now());
            if (noticeMapper.updateById(update) != 1) throw new BusinessException(GlobalErrorCodeConstants.UPDATE_ERROR);
        }
        return requireNoticeView(id);
    }

    private PageResult<LabBookingVo> listBookings(
            long page, long size, Long studentId, Long managerId, Integer status) {
        Page<LabBookingVo> query = new Page<>(page, size);
        IPage<LabBookingVo> result = bookingMapper.selectBookingPage(query, studentId, managerId, status);
        result.getRecords().forEach(this::translateBooking);
        return PageResult.from(result);
    }

    private void validateSlot(LabOpenSlotRequest request, Long labId, Long excludeSlotId) {
        validatePeriod(request.getStartPeriod(), request.getEndPeriod());
        if (request.getOpenDate().isBefore(LocalDate.now())) {
            throw new BusinessException(LabBookingErrorCodes.DATE_IN_PAST);
        }
        if (slotMapper.countOverlappingSlots(labId, request.getOpenDate(), request.getStartPeriod(),
                request.getEndPeriod(), excludeSlotId) > 0) {
            throw new BusinessException(LabBookingErrorCodes.SLOT_OVERLAP);
        }
    }

    private void requireSlotWithoutBookings(LabOpenSlot slot) {
        if (slotMapper.countActiveBookings(slot.getLabId(), slot.getOpenDate(),
                slot.getStartPeriod(), slot.getEndPeriod()) > 0) {
            throw new BusinessException(LabBookingErrorCodes.SLOT_HAS_BOOKINGS);
        }
    }

    private void validatePeriod(Integer start, Integer end) {
        if (start == null || end == null || start > end) {
            throw new BusinessException(LabBookingErrorCodes.INVALID_PERIOD);
        }
    }

    private Lab requireOwnedLab(Long id) {
        AuthSession session = CurrentUserContext.require();
        Lab lab = requireLab(id);
        if (!session.userId().equals(lab.getManagerId())) {
            throw new BusinessException(LabBookingErrorCodes.LAB_NOT_OWNED);
        }
        return lab;
    }

    private Lab requireLabForUpdate(Long id) {
        Lab lab = labMapper.selectByIdForUpdate(id);
        if (lab == null) throw new BusinessException(LabBookingErrorCodes.LAB_NOT_FOUND);
        return lab;
    }

    private Lab requireLab(Long id) {
        Lab lab = labMapper.selectById(id);
        if (lab == null) throw new BusinessException(LabBookingErrorCodes.LAB_NOT_FOUND);
        return lab;
    }

    private LabVo requireLabView(Long id) {
        LabVo lab = labMapper.selectLabView(id);
        if (lab == null) throw new BusinessException(LabBookingErrorCodes.LAB_NOT_FOUND);
        translateLab(lab);
        return lab;
    }

    private LabResource requireResource(Long id) {
        LabResource resource = resourceMapper.selectById(id);
        if (resource == null) throw new BusinessException(LabBookingErrorCodes.RESOURCE_NOT_FOUND);
        return resource;
    }

    private LabResourceVo requireResourceView(Long id) {
        LabResourceVo resource = resourceMapper.selectResourceView(id);
        if (resource == null) throw new BusinessException(LabBookingErrorCodes.RESOURCE_NOT_FOUND);
        translateResource(resource);
        return resource;
    }

    private LabOpenSlot requireSlot(Long id) {
        LabOpenSlot slot = slotMapper.selectById(id);
        if (slot == null) throw new BusinessException(LabBookingErrorCodes.SLOT_NOT_FOUND);
        return slot;
    }

    private LabOpenSlotVo requireSlotView(Long id) {
        LabOpenSlotVo slot = slotMapper.selectSlotView(id);
        if (slot == null) throw new BusinessException(LabBookingErrorCodes.SLOT_NOT_FOUND);
        return slot;
    }

    private LabBooking requireBooking(Long id) {
        LabBooking booking = bookingMapper.selectById(id);
        if (booking == null) throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_FOUND);
        return booking;
    }

    private LabBookingVo requireBookingView(Long id) {
        LabBookingVo booking = bookingMapper.selectBookingView(id);
        if (booking == null) throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_FOUND);
        translateBooking(booking);
        return booking;
    }

    private LabBookingNoticeVo requireNoticeView(Long id) {
        LabBookingNoticeVo notice = noticeMapper.selectNoticeView(id);
        if (notice == null) throw new BusinessException(LabBookingErrorCodes.NOTICE_NOT_FOUND);
        return notice;
    }

    private void requireBookingStatus(LabBooking booking, LabBookingStatus expected) {
        if (!Objects.equals(booking.getStatus(), expected.code())) {
            throw new BusinessException(LabBookingErrorCodes.INVALID_BOOKING_STATUS);
        }
    }

    private void requireOwnedBooking(LabBooking booking, Long studentId) {
        if (!studentId.equals(booking.getStudentId())) {
            throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_OWNED);
        }
    }

    private void requireBookingToday(LabBooking booking) {
        if (!LocalDate.now().equals(booking.getBookingDate())) {
            throw new BusinessException(LabBookingErrorCodes.BOOKING_NOT_TODAY);
        }
    }

    private void expireStaleBookings() {
        bookingMapper.expireStaleBookings(LocalDate.now(), LocalDateTime.now());
    }

    private void updateBooking(UpdateWrapper<LabBooking> update) {
        if (bookingMapper.update(null, update) != 1) {
            throw new BusinessException(LabBookingErrorCodes.INVALID_BOOKING_STATUS);
        }
    }

    private AuthSession requireAnyRole(String permission) {
        AuthSession session = CurrentUserContext.require();
        if ((!session.roles().contains(STUDENT_ROLE) && !session.roles().contains(TEACHER_ROLE))
                || !session.hasPermission(permission)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private AuthSession requireTeacher(String permission) {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(TEACHER_ROLE) || !session.hasPermission(permission)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        return session;
    }

    private StudentEntity requireStudent(String permission) {
        AuthSession session = CurrentUserContext.require();
        if (!session.roles().contains(STUDENT_ROLE) || !session.hasPermission(permission)) {
            throw new BusinessException(GlobalErrorCodeConstants.FORBIDDEN);
        }
        try {
            StudentEntity student = labMapper.selectStudentByNo(Long.valueOf(session.username()));
            if (student != null) return student;
        } catch (NumberFormatException ignored) {
            // Fall through to the stable profile error.
        }
        throw new BusinessException(LabBookingErrorCodes.STUDENT_PROFILE_NOT_FOUND);
    }

    private Integer parseBookingStatus(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            return LabBookingStatus.valueOf(name).code();
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        }
    }

    private Integer parseAvailabilityStatus(String name) {
        if (name == null || name.isBlank()) return null;
        return switch (name) {
            case "AVAILABLE", "OPEN" -> 1;
            case "MAINTENANCE" -> 0;
            default -> throw new BusinessException(GlobalErrorCodeConstants.BAD_REQUEST);
        };
    }

    private void translateLab(LabVo lab) {
        lab.setStatus(Objects.equals(lab.getStatusCode(), 1) ? "OPEN" : "MAINTENANCE");
    }

    private void translateResource(LabResourceVo resource) {
        resource.setStatus(Objects.equals(resource.getStatusCode(), 1) ? "AVAILABLE" : "MAINTENANCE");
        resource.setResourceTypeLabel("EQUIPMENT".equals(resource.getResourceType()) ? "实验设备" : "实验工位");
    }

    private void translateBooking(LabBookingVo booking) {
        booking.setStatus(LabBookingStatus.fromCode(booking.getStatusCode()).name());
    }

    private void copyLab(LabRequest request, Lab target) {
        target.setLabName(request.getLabName().trim());
        target.setLocation(request.getLocation().trim());
        target.setCapacity(request.getCapacity());
        target.setDescription(normalize(request.getDescription()));
        target.setStatus(request.getStatus());
    }

    private void copyResource(LabResourceRequest request, LabResource target) {
        target.setResourceNo(request.getResourceNo().trim().toUpperCase());
        target.setResourceName(request.getResourceName().trim());
        target.setResourceType(request.getResourceType());
        target.setDescription(normalize(request.getDescription()));
        target.setStatus(request.getStatus());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String createNumber(String prefix) {
        return prefix + LocalDate.now().format(NUMBER_DATE)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
