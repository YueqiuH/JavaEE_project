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
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/office/asset")
@Tag(name = "固定资产管理与申领")
public class AssetController {
    private static final int APPROVAL_PENDING = 0;
    private static final int APPROVAL_APPROVED = 1;
    private static final int APPROVAL_REJECTED = 2;
    private static final int STATUS_IN_STOCK = 1;
    private static final int STATUS_ASSIGNED = 2;
    private static final List<String> SUPPORTED_ASSET_TYPES = List.of("设备", "办公用品", "其他");

    @Autowired private IAssetService assetService;

    @GetMapping("/list")
    @RequirePermission(OfficePermissions.ASSET_READ)
    @Operation(summary = "查询可用固定资产台账（兼容旧接口）")
    public CommonResult<List<Asset>> list(@RequestParam(required = false) Long deptId) {
        return availableInventory(deptId);
    }

    @GetMapping("/inventory")
    @RequirePermission(OfficePermissions.ASSET_READ)
    @Operation(summary = "查询审批通过、尚有库存的固定资产台账")
    public CommonResult<List<Asset>> inventory(@RequestParam(required = false) Long deptId) {
        return availableInventory(deptId);
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
    @Operation(summary = "资产负责人查询全部资产申请")
    public CommonResult<List<Asset>> applications() {
        return CommonResult.success(assetService.list(new LambdaQueryWrapper<Asset>()
                .isNotNull(Asset::getApplyUserId)
                .orderByDesc(Asset::getCreateTime)));
    }

    @PostMapping("/save")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "新增或修改固定资产")
    public CommonResult<Boolean> save(@RequestBody Asset asset) {
        validateAsset(asset);
        if (asset.getAssetId() == null) {
            asset.setApplyUserId(null);
            asset.setUserId(null);
            asset.setStatus(STATUS_IN_STOCK);
            asset.setApproveStatus(APPROVAL_APPROVED);
            asset.setCreateTime(new Date());
        } else {
            Asset existing = requireAsset(asset.getAssetId());
            if (existing.getApplyUserId() != null) {
                throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "申请记录不能通过资产台账接口修改");
            }
            asset.setApplyUserId(null);
            asset.setUserId(existing.getUserId());
            asset.setStatus(asset.getStatus() == null ? existing.getStatus() : asset.getStatus());
            asset.setApproveStatus(APPROVAL_APPROVED);
            asset.setCreateTime(existing.getCreateTime());
        }
        return CommonResult.success(assetService.saveOrUpdate(asset));
    }

    @PostMapping("/apply")
    @RequirePermission(OfficePermissions.ASSET_APPLY)
    @Operation(summary = "教职工提交资产购置或领用申请")
    public CommonResult<Asset> apply(@RequestBody Asset asset) {
        validateAsset(asset);
        asset.setAssetId(null);
        asset.setApplyUserId(CurrentUserContext.require().userId());
        asset.setUserId(null);
        asset.setApproveStatus(APPROVAL_PENDING);
        asset.setStatus(STATUS_IN_STOCK);
        asset.setCreateTime(new Date());
        assetService.save(asset);
        return CommonResult.success(asset);
    }

    @PostMapping("/approve/{assetId}")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "资产负责人审批资产申请")
    public CommonResult<Asset> approve(@PathVariable Long assetId, @RequestParam Integer approved) {
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
        LambdaUpdateWrapper<Asset> update = new LambdaUpdateWrapper<Asset>()
                .eq(Asset::getAssetId, assetId)
                .eq(Asset::getApproveStatus, APPROVAL_PENDING)
                .isNotNull(Asset::getApplyUserId)
                .set(Asset::getApproveStatus, targetApproval);
        if (passed) {
            update.set(Asset::getStatus, STATUS_ASSIGNED)
                    .set(Asset::getUserId, asset.getApplyUserId());
        }
        if (!assetService.update(update)) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "申请状态已变化，请刷新后重试");
        }
        asset.setApproveStatus(targetApproval);
        if (passed) {
            asset.setStatus(STATUS_ASSIGNED);
            asset.setUserId(asset.getApplyUserId());
        }
        return CommonResult.success(asset);
    }

    @DeleteMapping("/{assetId}")
    @RequirePermission(OfficePermissions.ASSET_MANAGE)
    @Operation(summary = "删除固定资产记录")
    public CommonResult<Boolean> delete(@PathVariable Long assetId) {
        return CommonResult.success(assetService.removeById(assetId));
    }

    private CommonResult<List<Asset>> availableInventory(Long deptId) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<Asset>()
                .eq(Asset::getApproveStatus, APPROVAL_APPROVED)
                .eq(Asset::getStatus, STATUS_IN_STOCK)
                .gt(Asset::getQuantity, 0)
                .orderByDesc(Asset::getCreateTime);
        if (deptId != null) {
            wrapper.eq(Asset::getDeptId, deptId);
        }
        return CommonResult.success(assetService.list(wrapper));
    }

    private Asset requireAsset(Long assetId) {
        Asset asset = assetService.getById(assetId);
        if (asset == null) {
            throw new BusinessException(OfficeErrorCodeConstants.NOT_FOUND, "资产记录不存在");
        }
        return asset;
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
    }
}
