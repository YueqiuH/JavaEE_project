package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.service.office.IAssetService;
import com.smartcampus.common.result.CommonResult;
import com.smartcampus.contract.entity.Asset;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/office/asset")
@Tag(name = "固定资产管理与申领")
public class AssetController {
    @Autowired private IAssetService assetService;

    @GetMapping("/list")
    @Operation(summary = "查询固定资产台账")
    public CommonResult<List<Asset>> list(@RequestParam(required = false) Long deptId) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<Asset>().orderByDesc(Asset::getCreateTime);
        if (deptId != null) wrapper.eq(Asset::getDeptId, deptId);
        return CommonResult.success(assetService.list(wrapper));
    }

    @PostMapping("/save")
    @Operation(summary = "新增或修改固定资产")
    public CommonResult<Boolean> save(@RequestBody Asset asset) {
        if (asset.getAssetName() == null || asset.getAssetName().isBlank()) return CommonResult.error(1101, "资产名称不能为空");
        if (asset.getAssetId() == null) {
            if (asset.getStatus() == null) asset.setStatus(1);
            if (asset.getApproveStatus() == null) asset.setApproveStatus(1);
            asset.setCreateTime(new Date());
        }
        return CommonResult.success(assetService.saveOrUpdate(asset));
    }

    @PostMapping("/apply")
    @Operation(summary = "教职工提交资产购置或领用申请")
    public CommonResult<Asset> apply(@RequestBody Asset asset) {
        if (asset.getApplyUserId() == null) return CommonResult.error(1102, "申请人不能为空");
        asset.setApproveStatus(0);
        asset.setStatus(1);
        asset.setCreateTime(new Date());
        assetService.save(asset);
        return CommonResult.success(asset);
    }

    @PostMapping("/approve/{assetId}")
    @Operation(summary = "部门负责人审批资产申请")
    public CommonResult<Asset> approve(@PathVariable Long assetId, @RequestParam Integer approved) {
        Asset asset = assetService.getById(assetId);
        if (asset == null) return CommonResult.error(1103, "资产申请不存在");
        if (!Integer.valueOf(0).equals(asset.getApproveStatus())) return CommonResult.error(1104, "该申请已审批");
        asset.setApproveStatus(Integer.valueOf(1).equals(approved) ? 1 : 2);
        if (Integer.valueOf(1).equals(approved)) {
            asset.setStatus(2);
            asset.setUserId(asset.getApplyUserId());
        }
        assetService.updateById(asset);
        return CommonResult.success(asset);
    }

    @DeleteMapping("/{assetId}")
    @Operation(summary = "删除固定资产记录")
    public CommonResult<Boolean> delete(@PathVariable Long assetId) {
        return CommonResult.success(assetService.removeById(assetId));
    }
}
