package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.service.office.IFeeService;
import com.smartcampus.app.service.office.IPaymentService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Fee;
import com.smartcampus.contract.entity.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/office/fee")
@Tag(name = "学杂费交纳与流水查询")
public class FeeController {
    @Autowired private IFeeService feeService;
    @Autowired private IPaymentService paymentService;

    @PostMapping("/import")
    @Operation(summary = "财务人员批量导入费用账单")
    public CommonResult<Boolean> importFees(@RequestBody List<Fee> fees) {
        if (fees == null || fees.isEmpty()) return CommonResult.error(1001, "账单数据不能为空");
        fees.forEach(fee -> { if (fee.getStatus() == null) fee.setStatus(0); });
        return CommonResult.success(feeService.saveBatch(fees));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "学生查询个人费用账单")
    public CommonResult<List<Fee>> studentFees(@PathVariable Long studentId) {
        return CommonResult.success(feeService.list(new LambdaQueryWrapper<Fee>()
                .eq(Fee::getStudentId, studentId).orderByDesc(Fee::getCreateTime)));
    }

    @PostMapping("/pay/{feeId}")
    @Transactional
    @Operation(summary = "学生在线支付费用账单")
    public CommonResult<Payment> pay(@PathVariable Long feeId) {
        Fee fee = feeService.getById(feeId);
        if (fee == null) return CommonResult.error(1002, "账单不存在");
        if (Integer.valueOf(1).equals(fee.getStatus())) return CommonResult.error(1003, "该账单已支付");
        boolean paid = feeService.update(new LambdaUpdateWrapper<Fee>()
                .eq(Fee::getFeeId, feeId).eq(Fee::getStatus, 0).set(Fee::getStatus, 1));
        if (!paid) return CommonResult.error(1003, "该账单已支付或状态已变化");
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

    @GetMapping("/payment/recent/{studentId}")
    @Operation(summary = "查询个人一卡通最近五笔充值消费明细")
    public CommonResult<List<Payment>> recentPayments(@PathVariable Long studentId) {
        return CommonResult.success(paymentService.list(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getStudentId, studentId)
                .in(Payment::getPaymentType, "一卡通充值", "消费")
                .orderByDesc(Payment::getPaymentTime).last("LIMIT 5")));
    }
}
