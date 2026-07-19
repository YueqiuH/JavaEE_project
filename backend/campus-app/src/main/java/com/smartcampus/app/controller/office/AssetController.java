package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.security.OfficePermissions;
import com.smartcampus.app.service.office.IAssetService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.permission.RequirePermission;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Asset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/asset")
@Tag(name = "固定资产申请与审批")
public class AssetController {
    private static final int APPROVAL_PENDING = 0;
    private static final int APPROVAL_APPROVED = 1;
    private static final int APPROVAL_REJECTED = 2;
    private static final int STATUS_IN_STOCK = 1;
    private static final int STATUS_ASSIGNED = 2;
    private static final int STATUS_SCRAPPED = 3;
    private static final long SINGLE_DEPARTMENT_ID = 1L;
    private static final String APPLICATION_PURCHASE = "PURCHASE";
    private static final String APPLICATION_ADD = "ADD";
    private static final String APPLICATION_BORROW = "BORROW";
    private static final String APPLICATION_SCRAP = "SCRAP";
    private static final List<String> SUPPORTED_ASSET_TYPES = List.of("设备", "办公用品", "其他");

    @Autowired private IAssetService assetService;

    @GetMapping("/list")
    @RequirePermission(OfficePermissions.ASSET_READ)
    @Operation(summary = "查询唯一部门的可用固定资产台账（兼容旧接口）")
    public CommonResult<List<Asset>> list() {
        return availableInventory();
    }

    @GetMapping("/inventory")
    @RequirePermission(OfficePermissions.ASSET_READ)
    @Operation(summary = "查询唯一部门审批通过、尚有库存的固定资产台账")
    public CommonResult<List<Asset>> inventory() {
        return availableInventory();
    }

    @GetMapping("/applications/mine")
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "查询本人全部资产申请")
    public CommonResult<List<Asset>> myApplications() {
        Long userId = CurrentUserContext.require().userId();
        return CommonResult.success(assetService.list(new LambdaQueryWrapper<Asset>()
                .eq(Asset::getApplyUserId, userId)
                .orderByDesc(Asset::getCreateTime)));
    }

    @GetMapping("/applications")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "管理员查询全部固定资产审批申请")
    public CommonResult<List<Asset>> applications() {
        requireAdmin();
        return CommonResult.success(assetService.list(new LambdaQueryWrapper<Asset>()
                .isNotNull(Asset::getApplyUserId)
                .orderByDesc(Asset::getCreateTime)));
    }

    @PostMapping("/save")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "管理员修改现有固定资产；新增资产必须先提交添加审批")
    public CommonResult<Boolean> save(@RequestBody Asset asset) {
        requireAdmin();
        validateAsset(asset);
        if (asset.getAssetId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "新增资产必须先提交添加申请并由管理员审批");
        }
        Asset existing = requireAsset(asset.getAssetId());
        if (existing.getApplyUserId() != null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "申请记录不能通过资产台账接口修改");
        }
        asset.setApplyUserId(null);
        asset.setApplicationType(null);
        asset.setSourceAssetId(null);
        asset.setApplicationReason(null);
        asset.setUserId(existing.getUserId());
        asset.setStatus(asset.getStatus() == null ? existing.getStatus() : asset.getStatus());
        asset.setApproveStatus(APPROVAL_APPROVED);
        asset.setApproveUserId(existing.getApproveUserId());
        asset.setApproveRemark(existing.getApproveRemark());
        asset.setApproveTime(existing.getApproveTime());
        asset.setCreateTime(existing.getCreateTime());
        return CommonResult.success(assetService.saveOrUpdate(asset));
    }

    @PostMapping({"/apply", "/applications/purchase"})
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "教师或教职工提交固定资产购买申请")
    public CommonResult<Asset> apply(@RequestBody Asset asset) {
        requireTeacherOrStaffApplicant();
        return CommonResult.success(submitApplication(asset, APPLICATION_PURCHASE, null));
    }

    @PostMapping("/applications/add")
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "教师或教职工提交固定资产添加入库申请")
    public CommonResult<Asset> applyAdd(@RequestBody Asset asset) {
        requireTeacherOrStaffApplicant();
        return CommonResult.success(submitApplication(asset, APPLICATION_ADD, null));
    }

    @PostMapping({"/apply/{inventoryAssetId}", "/applications/borrow/{inventoryAssetId}"})
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "从可用资产台账发起借用申请")
    public CommonResult<Asset> applyAvailable(@PathVariable Long inventoryAssetId,
                                               @RequestParam Integer quantity) {
        requireTeacherOrStaffApplicant();
        Asset inventory = requireAvailableInventory(inventoryAssetId);
        if (quantity == null || quantity <= 0 || quantity > inventory.getQuantity()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "申请数量必须大于 0 且不能超过现有库存");
        }

        Asset application = new Asset();
        application.setAssetName(inventory.getAssetName());
        application.setAssetType(inventory.getAssetType());
        application.setQuantity(quantity);
        application.setDeptId(SINGLE_DEPARTMENT_ID);
        application.setApplyUserId(CurrentUserContext.require().userId());
        application.setApplicationType(APPLICATION_BORROW);
        application.setSourceAssetId(inventoryAssetId);
        application.setApplicationReason(null);
        application.setUserId(null);
        application.setApproveStatus(APPROVAL_PENDING);
        application.setApproveUserId(null);
        application.setApproveRemark(null);
        application.setApproveTime(null);
        application.setStatus(STATUS_IN_STOCK);
        application.setCreateTime(new Date());
        assetService.save(application);
        return CommonResult.success(application);
    }

    @PostMapping("/applications/scrap/{inventoryAssetId}")
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "教师或教职工提交固定资产损坏报废申请")
    public CommonResult<Asset> applyScrap(@PathVariable Long inventoryAssetId,
                                          @RequestParam Integer quantity,
                                          @RequestParam String reason) {
        requireTeacherOrStaffApplicant();
        Asset inventory = requireAvailableInventory(inventoryAssetId);
        if (quantity == null || quantity <= 0 || quantity > inventory.getQuantity()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "报废数量必须大于 0 且不能超过现有库存");
        }
        String normalizedReason = normalizeApplicationReason(reason);

        Asset application = new Asset();
        application.setAssetName(inventory.getAssetName());
        application.setAssetType(inventory.getAssetType());
        application.setQuantity(quantity);
        application.setDeptId(SINGLE_DEPARTMENT_ID);
        application.setApplyUserId(CurrentUserContext.require().userId());
        application.setApplicationType(APPLICATION_SCRAP);
        application.setSourceAssetId(inventoryAssetId);
        application.setApplicationReason(normalizedReason);
        application.setUserId(null);
        application.setApproveStatus(APPROVAL_PENDING);
        application.setApproveUserId(null);
        application.setApproveRemark(null);
        application.setApproveTime(null);
        application.setStatus(STATUS_IN_STOCK);
        application.setCreateTime(new Date());
        assetService.save(application);
        return CommonResult.success(application);
    }

    @PostMapping({"/approve/{assetId}", "/applications/{assetId}/approve"})
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Transactional
    @Operation(summary = "管理员审批购买、添加、借用或损坏报废申请")
    public CommonResult<Asset> approve(@PathVariable Long assetId,
                                       @RequestParam Integer approved,
                                       @RequestParam(required = false) String remark) {
        requireAdmin();
        Asset asset = requireAsset(assetId);
        if (asset.getApplyUserId() == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "该记录不是资产申请");
        }
        if (!Integer.valueOf(APPROVAL_PENDING).equals(asset.getApproveStatus())) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该申请已审批");
        }
        if (!Integer.valueOf(0).equals(approved) && !Integer.valueOf(1).equals(approved)) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批结果只能为通过或拒绝");
        }
        Long approverId = CurrentUserContext.require().userId();
        if (approverId.equals(asset.getApplyUserId())) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "不能审批自己提交的资产申请");
        }

        boolean passed = Integer.valueOf(1).equals(approved);
        int targetApproval = passed ? APPROVAL_APPROVED : APPROVAL_REJECTED;
        String normalizedRemark = normalizeRemark(remark);
        if (!passed && normalizedRemark == null) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "拒绝申请时必须填写审批意见");
        }
        String applicationType = resolveApplicationType(asset);
        if (passed && APPLICATION_BORROW.equals(applicationType)) {
            Long sourceInventoryId = asset.getSourceAssetId() != null
                    ? asset.getSourceAssetId() : asset.getUserId();
            if (sourceInventoryId == null) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "借用申请缺少来源资产，不能通过");
            }
            consumeInventory(sourceInventoryId, asset.getQuantity());
        } else if (passed && APPLICATION_SCRAP.equals(applicationType)) {
            Long sourceInventoryId = asset.getSourceAssetId();
            if (sourceInventoryId == null) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "报废申请缺少来源资产，不能通过");
            }
            if (asset.getApplicationReason() == null || asset.getApplicationReason().isBlank()) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "报废申请缺少损坏情况和报废原因，不能通过");
            }
            scrapInventory(sourceInventoryId, asset.getQuantity());
        } else if (passed && APPLICATION_ADD.equals(applicationType)) {
            if (!assetService.save(buildInventoryFromApplication(asset, approverId, normalizedRemark))) {
                throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "资产入库失败，请稍后重试");
            }
        }
        LambdaUpdateWrapper<Asset> update = new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getAssetId, assetId)
                .eq(Asset::getApproveStatus, APPROVAL_PENDING)
                .isNotNull(Asset::getApplyUserId)
                .set(Asset::getApplicationType, applicationType)
                .set(Asset::getApproveStatus, targetApproval)
                .set(Asset::getApproveUserId, approverId)
                .set(Asset::getApproveRemark, normalizedRemark)
                .set(Asset::getApproveTime, new Date());
        if (passed && APPLICATION_BORROW.equals(applicationType)) {
            update.set(Asset::getStatus, STATUS_ASSIGNED)
                    .set(Asset::getUserId, asset.getApplyUserId());
        } else if (passed && APPLICATION_SCRAP.equals(applicationType)) {
            update.set(Asset::getStatus, STATUS_SCRAPPED)
                    .set(Asset::getUserId, null);
        } else {
            update.set(Asset::getUserId, null);
        }
        if (!assetService.update(update)) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "申请状态已变化，请刷新后重试");
        }
        asset.setApplicationType(applicationType);
        asset.setApproveStatus(targetApproval);
        asset.setApproveUserId(approverId);
        asset.setApproveRemark(normalizedRemark);
        asset.setApproveTime(new Date());
        if (passed && APPLICATION_BORROW.equals(applicationType)) {
            asset.setStatus(STATUS_ASSIGNED);
            asset.setUserId(asset.getApplyUserId());
        } else if (passed && APPLICATION_SCRAP.equals(applicationType)) {
            asset.setStatus(STATUS_SCRAPPED);
            asset.setUserId(null);
        } else {
            asset.setUserId(null);
        }
        return CommonResult.success(asset);
    }

    @DeleteMapping("/{assetId}")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "删除固定资产记录")
    public CommonResult<Boolean> delete(@PathVariable Long assetId) {
        requireAdmin();
        Asset asset = requireAsset(assetId);
        if (asset.getApplyUserId() != null) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "审批申请必须保留审计记录，不能删除");
        }
        return CommonResult.success(assetService.removeById(assetId));
    }

    private CommonResult<List<Asset>> availableInventory() {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<Asset>()
                .isNull(Asset::getApplyUserId)
                .eq(Asset::getApproveStatus, APPROVAL_APPROVED)
                .eq(Asset::getStatus, STATUS_IN_STOCK)
                .gt(Asset::getQuantity, 0)
                .orderByDesc(Asset::getCreateTime);
        return CommonResult.success(assetService.list(wrapper));
    }

    private Asset requireAvailableInventory(Long assetId) {
        Asset inventory = requireAsset(assetId);
        if (inventory.getApplyUserId() != null
                || !Integer.valueOf(APPROVAL_APPROVED).equals(inventory.getApproveStatus())
                || !Integer.valueOf(STATUS_IN_STOCK).equals(inventory.getStatus())
                || inventory.getQuantity() == null || inventory.getQuantity() <= 0) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该资产当前不可申请");
        }
        return inventory;
    }

    private void consumeInventory(Long inventoryAssetId, Integer requestedQuantity) {
        Asset inventory = requireAvailableInventory(inventoryAssetId);
        if (requestedQuantity == null || requestedQuantity <= 0 || requestedQuantity > inventory.getQuantity()) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "可用库存不足，无法通过申请");
        }
        int remaining = inventory.getQuantity() - requestedQuantity;
        boolean updated = assetService.update(new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getAssetId, inventoryAssetId)
                .isNull(Asset::getApplyUserId)
                .eq(Asset::getStatus, STATUS_IN_STOCK)
                .eq(Asset::getQuantity, inventory.getQuantity())
                .set(Asset::getQuantity, remaining)
                .set(Asset::getStatus, remaining == 0 ? STATUS_ASSIGNED : STATUS_IN_STOCK));
        if (!updated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "库存状态已变化，请刷新后重试");
        }
    }

    private void scrapInventory(Long inventoryAssetId, Integer requestedQuantity) {
        Asset inventory = requireAvailableInventory(inventoryAssetId);
        if (requestedQuantity == null || requestedQuantity <= 0 || requestedQuantity > inventory.getQuantity()) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "可报废库存不足，无法通过申请");
        }
        int remaining = inventory.getQuantity() - requestedQuantity;
        boolean updated = assetService.update(new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getAssetId, inventoryAssetId)
                .isNull(Asset::getApplyUserId)
                .eq(Asset::getStatus, STATUS_IN_STOCK)
                .eq(Asset::getQuantity, inventory.getQuantity())
                .set(Asset::getQuantity, remaining)
                .set(Asset::getStatus, remaining == 0 ? STATUS_SCRAPPED : STATUS_IN_STOCK));
        if (!updated) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "库存状态已变化，请刷新后重试");
        }
    }

    private Asset requireAsset(Long assetId) {
        Asset asset = assetService.getById(assetId);
        if (asset == null) {
            throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "资产记录不存在");
        }
        return asset;
    }

    private Asset submitApplication(Asset asset, String applicationType, Long sourceAssetId) {
        validateAsset(asset);
        asset.setAssetId(null);
        asset.setApplyUserId(CurrentUserContext.require().userId());
        asset.setApplicationType(applicationType);
        asset.setSourceAssetId(sourceAssetId);
        asset.setApplicationReason(null);
        asset.setUserId(null);
        asset.setApproveStatus(APPROVAL_PENDING);
        asset.setApproveUserId(null);
        asset.setApproveRemark(null);
        asset.setApproveTime(null);
        asset.setStatus(STATUS_IN_STOCK);
        asset.setCreateTime(new Date());
        assetService.save(asset);
        return asset;
    }

    private Asset buildInventoryFromApplication(Asset application, Long approverId, String remark) {
        Asset inventory = new Asset();
        inventory.setAssetName(application.getAssetName());
        inventory.setAssetType(application.getAssetType());
        inventory.setQuantity(application.getQuantity());
        inventory.setDeptId(SINGLE_DEPARTMENT_ID);
        inventory.setUserId(null);
        inventory.setStatus(STATUS_IN_STOCK);
        inventory.setApplyUserId(null);
        inventory.setApplicationType(null);
        inventory.setSourceAssetId(null);
        inventory.setApplicationReason(null);
        inventory.setApproveStatus(APPROVAL_APPROVED);
        inventory.setApproveUserId(approverId);
        inventory.setApproveRemark(remark);
        inventory.setApproveTime(new Date());
        inventory.setCreateTime(new Date());
        return inventory;
    }

    private String resolveApplicationType(Asset asset) {
        if (List.of(APPLICATION_PURCHASE, APPLICATION_ADD, APPLICATION_BORROW, APPLICATION_SCRAP)
                .contains(asset.getApplicationType())) {
            return asset.getApplicationType();
        }
        // 兼容旧版数据：待审批且 user_id 保存了来源资产 ID 的记录属于领用/借用申请。
        return asset.getSourceAssetId() != null || asset.getUserId() != null
                ? APPLICATION_BORROW : APPLICATION_PURCHASE;
    }

    private String normalizeRemark(String remark) {
        if (remark == null || remark.isBlank()) {
            return null;
        }
        String normalized = remark.trim();
        if (normalized.length() > 256) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批意见不能超过 256 个字符");
        }
        return normalized;
    }

    private String normalizeApplicationReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "请填写资产损坏情况和报废原因");
        }
        String normalized = reason.trim();
        if (normalized.length() > 512) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "损坏和报废原因不能超过 512 个字符");
        }
        return normalized;
    }

    private void requireAdmin() {
        if (!CurrentUserContext.require().roles().contains("ADMIN")) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "固定资产审批和管理仅限管理员操作");
        }
    }

    private void requireTeacherOrStaffApplicant() {
        if (CurrentUserContext.require().roles().stream()
                .noneMatch(role -> "TEACHER".equals(role) || "STAFF".equals(role))) {
            throw new BusinessException(OfficeErrorCodeConstants.FORBIDDEN, "固定资产申请仅限教师或教职工提交");
        }
    }

    private void validateAsset(Asset asset) {
        if (asset == null || asset.getAssetName() == null || asset.getAssetName().isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "资产名称不能为空");
        }
        if (!SUPPORTED_ASSET_TYPES.contains(asset.getAssetType())) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "资产类型必须为设备、办公用品或其他");
        }
        if (asset.getQuantity() == null || asset.getQuantity() <= 0) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "资产数量必须大于 0");
        }
        asset.setAssetName(asset.getAssetName().trim());
        asset.setDeptId(SINGLE_DEPARTMENT_ID);
    }
}
