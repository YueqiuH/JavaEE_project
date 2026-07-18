package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IFeeService;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.common.result.PageResult;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.entity.Payment;
import com.smartcampus.contract.vo.CardBalanceVo;
import com.smartcampus.contract.vo.StudentFeeOverviewVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/fee")
@Tag(name = "学杂费交纳与流水查询")
public class FeeController {
    private static final List<String> OVERVIEW_STATUSES = List.of("欠费", "已缴清", "未出账");

    @Autowired private IFeeService feeService;
    @Autowired private IPaymentService paymentService;

    @PostMapping("/import")
    @RequirePermission(OfficePermissions.FEE_MANAGE)
    @Operation(summary = "财务人员批量导入费用账单")
    public CommonResult<Boolean> importFees(@RequestBody List<Fee> fees) {
        if (fees == null || fees.isEmpty()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "账单数据不能为空");
        }
        fees.forEach(fee -> {
            if (fee.getStudentId() == null || fee.getFeeType() == null || fee.getFeeType().isBlank()
                    || fee.getAmount() == null || fee.getAmount().signum() <= 0) {
                throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "学生、费用类型和正数金额不能为空");
            }
            fee.setFeeId(null);
            fee.setStatus(0);
            fee.setCreateTime(new Date());
        });
        return CommonResult.success(feeService.saveBatch(fees));
    }

    @GetMapping("/mine")
    @RequirePermission(OfficePermissions.FEE_SELF_READ)
    @Operation(summary = "学生查询个人费用账单")
    public CommonResult<List<Fee>> myFees() {
        Long studentId = CurrentUserContext.require().userId();
        return CommonResult.success(feeService.list(new LambdaQueryWrapper<Fee>()
                .eq(Fee::getStudentId, studentId).orderByDesc(Fee::getCreateTime)));
    }

    @PostMapping("/pay/{feeId}")
    @RequirePermission(OfficePermissions.FEE_SELF_PAY)
    @Transactional
    @Operation(summary = "学生在线支付费用账单")
    public CommonResult<Payment> pay(@PathVariable Long feeId) {
        Fee fee = feeService.getById(feeId);
        if (fee == null) throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "账单不存在");
        if (!CurrentUserContext.require().userId().equals(fee.getStudentId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能支付他人的账单");
        }
        if (Integer.valueOf(1).equals(fee.getStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该账单已支付");
        }
        boolean paid = feeService.update(new LambdaUpdateWrapper<Fee>()
                .eq(Fee::getFeeId, feeId).eq(Fee::getStatus, 0).set(Fee::getStatus, 1));
        if (!paid) throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该账单已支付或状态已变化");
        Payment payment = new Payment();
        payment.setFeeId(feeId);
        payment.setStudentId(fee.getStudentId());
        payment.setAmount(fee.getAmount());
        payment.setPaymentType(fee.getFeeType());
        payment.setDescription(fee.getSemester() + " " + fee.getFeeType());
        payment.setPaymentTime(new Date());
        paymentService.save(payment);
        return CommonResult.success(payment);
    }

    @GetMapping("/payment/recent")
    @RequirePermission(OfficePermissions.FEE_SELF_READ)
    @Operation(summary = "查询个人一卡通最近五笔充值消费明细")
    public CommonResult<List<Payment>> recentPayments() {
        Long studentId = CurrentUserContext.require().userId();
        return CommonResult.success(paymentService.list(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getStudentId, studentId)
                .in(Payment::getPaymentType, "一卡通充值", "勤工俭学工资", "消费")
                .orderByDesc(Payment::getPaymentTime).last("LIMIT 5")));
    }

    @GetMapping("/students")
    @RequirePermission(OfficePermissions.FEE_OVERVIEW_READ)
    @Operation(summary = "教师分页查看学生缴费情况")
    public CommonResult<PageResult<StudentFeeOverviewVo>> studentFeeOverview(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "分页参数不合法，size 必须在 1-100 之间");
        }
        if (status != null && !status.isBlank() && !OVERVIEW_STATUSES.contains(status)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "缴费状态必须为欠费、已缴清或未出账");
        }
        return CommonResult.success(PageResult.from(
                feeService.getStudentFeeOverview(page, size, keyword, status)));
    }

    @GetMapping("/card/balance")
    @RequirePermission(OfficePermissions.FEE_SELF_READ)
    @Operation(summary = "查询个人一卡通余额")
    public CommonResult<CardBalanceVo> cardBalance() {
        Long studentId = CurrentUserContext.require().userId();
        return CommonResult.success(new CardBalanceVo(paymentService.getCardBalance(studentId)));
    }
}
