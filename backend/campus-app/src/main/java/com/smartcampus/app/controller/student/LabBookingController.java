package com.smartcampus.app.controller.student;

import com.smartcampus.app.service.student.LabBookingService;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.dto.student.LabBookingRequest;
import com.smartcampus.contract.dto.student.LabOpenSlotRequest;
import com.smartcampus.contract.dto.student.LabRequest;
import com.smartcampus.contract.dto.student.LabResourceRequest;
import com.smartcampus.contract.vo.student.LabBookingNoticeVo;
import com.smartcampus.contract.vo.student.LabBookingVo;
import com.smartcampus.contract.vo.student.LabOpenSlotVo;
import com.smartcampus.contract.vo.student.LabResourceVo;
import com.smartcampus.contract.vo.student.LabVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/student")
@Tag(name = "实验室与实践预约")
public class LabBookingController {

    private final LabBookingService labBookingService;

    public LabBookingController(LabBookingService labBookingService) {
        this.labBookingService = labBookingService;
    }

    @GetMapping("/labs")
    @RequirePermission("lab:read")
    @Operation(summary = "按当前角色查询实验室")
    public CommonResult<PageResult<LabVo>> listLabs(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(labBookingService.listLabs(page, size, status));
    }

    @GetMapping("/labs/{id}")
    @RequirePermission("lab:read")
    @Operation(summary = "按角色和归属查询实验室详情")
    public CommonResult<LabVo> getLab(@PathVariable Long id) {
        return CommonResult.success(labBookingService.getLab(id));
    }

    @PostMapping("/labs")
    @RequirePermission("lab:manage-self")
    @Operation(summary = "教师、辅导员或教务处创建本人负责的实验室")
    public CommonResult<LabVo> createLab(@Valid @RequestBody LabRequest request) {
        return CommonResult.success(labBookingService.createLab(request));
    }

    @PutMapping("/labs/{id}")
    @RequirePermission("lab:manage-self")
    @Operation(summary = "教师、辅导员或教务处修改本人负责的实验室")
    public CommonResult<LabVo> updateLab(@PathVariable Long id, @Valid @RequestBody LabRequest request) {
        return CommonResult.success(labBookingService.updateLab(id, request));
    }

    @GetMapping("/lab-resources")
    @RequirePermission("lab:read")
    @Operation(summary = "查询可见的实验设备和工位")
    public CommonResult<List<LabResourceVo>> listResources(@RequestParam(required = false) Long labId) {
        return CommonResult.success(labBookingService.listResources(labId));
    }

    @PostMapping("/labs/{labId}/resources")
    @RequirePermission("lab:resource:manage-self")
    @Operation(summary = "教师为本人实验室新增设备或工位")
    public CommonResult<LabResourceVo> createResource(
            @PathVariable Long labId, @Valid @RequestBody LabResourceRequest request) {
        return CommonResult.success(labBookingService.createResource(labId, request));
    }

    @PutMapping("/lab-resources/{id}")
    @RequirePermission("lab:resource:manage-self")
    @Operation(summary = "教师修改本人实验室的设备或工位")
    public CommonResult<LabResourceVo> updateResource(
            @PathVariable Long id, @Valid @RequestBody LabResourceRequest request) {
        return CommonResult.success(labBookingService.updateResource(id, request));
    }

    @GetMapping("/lab-open-slots")
    @RequirePermission("lab:read")
    @Operation(summary = "按角色查询实验室开放时段")
    public CommonResult<PageResult<LabOpenSlotVo>> listSlots(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) long size,
            @RequestParam(required = false) Long labId) {
        return CommonResult.success(labBookingService.listSlots(page, size, labId));
    }

    @PostMapping("/labs/{labId}/open-slots")
    @RequirePermission("lab:slot:manage-self")
    @Operation(summary = "教师新增本人实验室开放时段")
    public CommonResult<LabOpenSlotVo> createSlot(
            @PathVariable Long labId, @Valid @RequestBody LabOpenSlotRequest request) {
        return CommonResult.success(labBookingService.createSlot(labId, request));
    }

    @PutMapping("/lab-open-slots/{id}")
    @RequirePermission("lab:slot:manage-self")
    @Operation(summary = "教师修改本人实验室开放时段")
    public CommonResult<LabOpenSlotVo> updateSlot(
            @PathVariable Long id, @Valid @RequestBody LabOpenSlotRequest request) {
        return CommonResult.success(labBookingService.updateSlot(id, request));
    }

    @DeleteMapping("/lab-open-slots/{id}")
    @RequirePermission("lab:slot:manage-self")
    @Operation(summary = "教师删除没有有效预约的开放时段")
    public CommonResult<Void> deleteSlot(@PathVariable Long id) {
        labBookingService.deleteSlot(id);
        return CommonResult.success(null);
    }

    @GetMapping("/lab-bookings/mine")
    @RequirePermission("lab:booking:read-self")
    @Operation(summary = "学生查询本人预约")
    public CommonResult<PageResult<LabBookingVo>> listMyBookings(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(labBookingService.listMyBookings(page, size, status));
    }

    @GetMapping("/lab-bookings/managed")
    @RequirePermission("lab:booking:read-managed")
    @Operation(summary = "教师查询本人实验室的预约记录")
    public CommonResult<PageResult<LabBookingVo>> listManagedBookings(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) String status) {
        return CommonResult.success(labBookingService.listManagedBookings(page, size, status));
    }

    @GetMapping("/lab-bookings/{id}")
    @RequirePermission("student:read")
    @Operation(summary = "按本人预约或实验室归属查询预约详情")
    public CommonResult<LabBookingVo> getBooking(@PathVariable Long id) {
        return CommonResult.success(labBookingService.getBooking(id));
    }

    @PostMapping("/lab-bookings")
    @RequirePermission("lab:booking:create")
    @Operation(summary = "学生预约实验室当天使用名额")
    public CommonResult<LabBookingVo> createBooking(@Valid @RequestBody LabBookingRequest request) {
        return CommonResult.success(labBookingService.createBooking(request));
    }

    @DeleteMapping("/lab-bookings/{id}")
    @RequirePermission("lab:booking:cancel-self")
    @Operation(summary = "学生取消本人未开始的预约")
    public CommonResult<LabBookingVo> cancelBooking(@PathVariable Long id) {
        return CommonResult.success(labBookingService.cancelBooking(id));
    }

    @PostMapping("/lab-bookings/{id}/check-ins")
    @RequirePermission("lab:booking:check-in-self")
    @Operation(summary = "学生为本人当天预约签到")
    public CommonResult<LabBookingVo> checkIn(@PathVariable Long id) {
        return CommonResult.success(labBookingService.checkIn(id));
    }

    @PostMapping("/lab-bookings/{id}/check-outs")
    @RequirePermission("lab:booking:check-out-self")
    @Operation(summary = "学生为本人使用中的预约签退")
    public CommonResult<LabBookingVo> checkOut(@PathVariable Long id) {
        return CommonResult.success(labBookingService.checkOut(id));
    }

    @PostMapping("/lab-bookings/{id}/completions")
    @RequirePermission("lab:booking:complete-managed")
    @Operation(summary = "教师为本人实验室的使用中预约强制签退")
    public CommonResult<LabBookingVo> completeBooking(@PathVariable Long id) {
        return CommonResult.success(labBookingService.completeBooking(id));
    }

    @GetMapping("/lab-booking-notices/mine")
    @RequirePermission("lab:notice:read-self")
    @Operation(summary = "学生查询本人预约通知")
    public CommonResult<PageResult<LabBookingNoticeVo>> listNotices(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) long size,
            @RequestParam(required = false) @Min(0) @Max(1) Integer isRead) {
        return CommonResult.success(labBookingService.listNotices(page, size, isRead));
    }

    @PostMapping("/lab-booking-notices/{id}/reads")
    @RequirePermission("lab:notice:mark-self")
    @Operation(summary = "学生标记本人预约通知为已读")
    public CommonResult<LabBookingNoticeVo> markNoticeRead(@PathVariable Long id) {
        return CommonResult.success(labBookingService.markNoticeRead(id));
    }
}
