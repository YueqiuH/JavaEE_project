package com.smartcampus.app.controller.office;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.smartcampus.app.service.office.IAssetService;
import com.smartcampus.auth.context.CurrentUserContext;
import com.smartcampus.auth.model.AuthSession;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.Asset;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @BeforeAll
    static void initializeMybatisMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Asset.class);
    }

    @Mock
    private IAssetService assetService;

    @InjectMocks
    private AssetController controller;

    @AfterEach
    void clearContext() {
        CurrentUserContext.clear();
    }

    @Test
    @SuppressWarnings("unchecked")
    void inventoryOnlyQueriesNonApplicationAvailablePositiveStock() {
        when(assetService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        controller.inventory();

        ArgumentCaptor<LambdaQueryWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(assetService).list(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("apply_user_id IS NULL"));
        assertTrue(sql.contains("status"));
        assertTrue(sql.contains("quantity"));
        assertFalse(sql.contains("dept_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void personalApplicationQueryUsesAuthenticatedUser() {
        setCurrentUser(2L, Set.of("asset:apply"));
        when(assetService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        controller.myApplications();

        ArgumentCaptor<LambdaQueryWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(assetService).list(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("apply_user_id"));
        assertTrue(captor.getValue().getParamNameValuePairs().containsValue(2L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void managerApplicationQueryExcludesInventoryRows() {
        setCurrentUser(1L, Set.of("ADMIN"), Set.of("asset:manage"));
        when(assetService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        controller.applications();

        ArgumentCaptor<LambdaQueryWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(assetService).list(captor.capture());
        assertTrue(captor.getValue().getSqlSegment().contains("apply_user_id IS NOT NULL"));
    }

    @Test
    void applyAlwaysUsesAuthenticatedApplicantAndPendingStatus() {
        setCurrentUser(2L, Set.of("asset:apply"));
        when(assetService.save(any(Asset.class))).thenReturn(true);
        Asset asset = validAsset();
        asset.setAssetId(99L);
        asset.setApplyUserId(88L);
        asset.setUserId(77L);
        asset.setApproveStatus(1);

        Asset saved = controller.apply(asset).getData();

        assertNull(saved.getAssetId());
        assertEquals(2L, saved.getApplyUserId());
        assertNull(saved.getUserId());
        assertEquals(0, saved.getApproveStatus());
        assertEquals(1, saved.getStatus());
        assertEquals(1L, saved.getDeptId());
        assertEquals("PURCHASE", saved.getApplicationType());
        verify(assetService).save(saved);
    }

    @Test
    void availableAssetApplicationCopiesInventoryAndRemembersSource() {
        setCurrentUser(2L, Set.of("asset:apply"));
        Asset inventory = validAsset();
        inventory.setAssetId(9L);
        inventory.setQuantity(3);
        inventory.setStatus(1);
        inventory.setApproveStatus(1);
        inventory.setDeptId(99L);
        when(assetService.getById(9L)).thenReturn(inventory);
        when(assetService.save(any(Asset.class))).thenReturn(true);

        Asset application = controller.applyAvailable(9L, 2).getData();

        assertEquals(2L, application.getApplyUserId());
        assertNull(application.getUserId());
        assertEquals(9L, application.getSourceAssetId());
        assertEquals("BORROW", application.getApplicationType());
        assertEquals(2, application.getQuantity());
        assertEquals(0, application.getApproveStatus());
        assertEquals(1L, application.getDeptId());
        verify(assetService).save(application);
    }

    @Test
    void teacherCanSubmitScrapApplicationWithDamageReason() {
        setCurrentUser(2L, Set.of("TEACHER"), Set.of("asset:apply"));
        Asset inventory = validAsset();
        inventory.setAssetId(10L);
        inventory.setQuantity(3);
        inventory.setStatus(1);
        inventory.setApproveStatus(1);
        when(assetService.getById(10L)).thenReturn(inventory);
        when(assetService.save(any(Asset.class))).thenReturn(true);

        Asset application = controller.applyScrap(10L, 2, " 显示屏碎裂，无法维修 ").getData();

        assertEquals("SCRAP", application.getApplicationType());
        assertEquals(10L, application.getSourceAssetId());
        assertEquals("显示屏碎裂，无法维修", application.getApplicationReason());
        assertEquals(2L, application.getApplyUserId());
        assertEquals(0, application.getApproveStatus());
        verify(assetService).save(application);
    }

    @Test
    void adminCannotSubmitAssetApplicationEvenWithApplyPermission() {
        setCurrentUser(1L, Set.of("ADMIN"), Set.of("asset:apply"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.apply(validAsset()));

        assertEquals(403100, exception.getErrorCode().getCode());
        verify(assetService, never()).save(any(Asset.class));
    }

    @Test
    void managerCannotApproveOwnApplication() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(5L, 3L);
        when(assetService.getById(5L)).thenReturn(application);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.approve(5L, 1, null));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void approvedPurchaseApplicationDoesNotCreateInventory() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(6L, 2L);
        when(assetService.getById(6L)).thenReturn(application);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        Asset approved = controller.approve(6L, 1, "同意采购").getData();

        assertEquals(1, approved.getApproveStatus());
        assertEquals(1, approved.getStatus());
        assertNull(approved.getUserId());
        assertEquals(3L, approved.getApproveUserId());
        assertEquals("同意采购", approved.getApproveRemark());
        verify(assetService, never()).save(any(Asset.class));
        verify(assetService).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void approvedAvailableAssetApplicationConsumesSourceInventory() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(6L, 2L);
        application.setApplicationType("BORROW");
        application.setSourceAssetId(9L);
        application.setQuantity(2);
        Asset inventory = validAsset();
        inventory.setAssetId(9L);
        inventory.setQuantity(2);
        inventory.setStatus(1);
        inventory.setApproveStatus(1);
        when(assetService.getById(6L)).thenReturn(application);
        when(assetService.getById(9L)).thenReturn(inventory);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        Asset approved = controller.approve(6L, 1, null).getData();

        assertEquals(2L, approved.getUserId());
        ArgumentCaptor<LambdaUpdateWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(assetService, times(2)).update(captor.capture());
        assertTrue(captor.getAllValues().get(0).getSqlSegment().contains("quantity"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void approvedScrapApplicationRemovesDamagedQuantityFromInventory() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(11L, 2L);
        application.setApplicationType("SCRAP");
        application.setSourceAssetId(10L);
        application.setApplicationReason("主板烧毁");
        application.setQuantity(2);
        Asset inventory = validAsset();
        inventory.setAssetId(10L);
        inventory.setQuantity(2);
        inventory.setStatus(1);
        inventory.setApproveStatus(1);
        when(assetService.getById(11L)).thenReturn(application);
        when(assetService.getById(10L)).thenReturn(inventory);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        Asset approved = controller.approve(11L, 1, "同意报废").getData();

        assertEquals(1, approved.getApproveStatus());
        assertEquals(3, approved.getStatus());
        assertNull(approved.getUserId());
        ArgumentCaptor<LambdaUpdateWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(assetService, times(2)).update(captor.capture());
        assertTrue(captor.getAllValues().get(0).getSqlSegment().contains("quantity"));
    }

    @Test
    void approvedAddApplicationCreatesSeparateInventoryRecord() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(7L, 2L);
        application.setApplicationType("ADD");
        application.setQuantity(4);
        when(assetService.getById(7L)).thenReturn(application);
        when(assetService.save(any(Asset.class))).thenReturn(true);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        controller.approve(7L, 1, "验收入库");

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetService).save(captor.capture());
        Asset inventory = captor.getValue();
        assertNull(inventory.getApplyUserId());
        assertNull(inventory.getApplicationType());
        assertEquals(4, inventory.getQuantity());
        assertEquals(1, inventory.getApproveStatus());
        assertEquals(3L, inventory.getApproveUserId());
    }

    @Test
    void staffWithLegacyManagePermissionCannotApprove() {
        setCurrentUser(3L, Set.of("STAFF"), Set.of("asset:manage"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.approve(6L, 1, null));

        assertEquals(403100, exception.getErrorCode().getCode());
        verify(assetService, never()).getById(any());
    }

    @Test
    void rejectedApplicationRequiresApprovalRemark() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset application = pendingApplication(8L, 2L);
        when(assetService.getById(8L)).thenReturn(application);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.approve(8L, 0, " "));

        assertEquals(400100, exception.getErrorCode().getCode());
    }

    @Test
    void adminCannotAddInventoryWithoutApproval() {
        setCurrentUser(3L, Set.of("ADMIN"), Set.of("asset:manage"));
        Asset asset = validAsset();
        asset.setApplyUserId(88L);
        asset.setUserId(77L);
        asset.setDeptId(99L);

        BusinessException exception = assertThrows(BusinessException.class, () -> controller.save(asset));

        assertEquals(400100, exception.getErrorCode().getCode());
        assertEquals(1L, asset.getDeptId());
    }

    private Asset validAsset() {
        Asset asset = new Asset();
        asset.setAssetName("投影仪");
        asset.setAssetType("设备");
        asset.setQuantity(1);
        asset.setDeptId(1L);
        return asset;
    }

    private Asset pendingApplication(Long assetId, Long applicantId) {
        Asset asset = validAsset();
        asset.setAssetId(assetId);
        asset.setApplyUserId(applicantId);
        asset.setApplicationType("PURCHASE");
        asset.setApproveStatus(0);
        asset.setStatus(1);
        return asset;
    }

    private void setCurrentUser(Long userId, Set<String> permissions) {
        setCurrentUser(userId, Set.of("STAFF"), permissions);
    }

    private void setCurrentUser(Long userId, Set<String> roles, Set<String> permissions) {
        CurrentUserContext.set(new AuthSession(userId, "user" + userId, 3,
                roles, permissions, 1L));
    }
}
