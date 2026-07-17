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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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

        controller.inventory(7L);

        ArgumentCaptor<LambdaQueryWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(assetService).list(captor.capture());
        String sql = captor.getValue().getSqlSegment();
        assertTrue(sql.contains("apply_user_id IS NULL"));
        assertTrue(sql.contains("status"));
        assertTrue(sql.contains("quantity"));
        assertTrue(sql.contains("dept_id"));
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
        when(assetService.getById(9L)).thenReturn(inventory);
        when(assetService.save(any(Asset.class))).thenReturn(true);

        Asset application = controller.applyAvailable(9L, 2).getData();

        assertEquals(2L, application.getApplyUserId());
        assertEquals(9L, application.getUserId());
        assertEquals(2, application.getQuantity());
        assertEquals(0, application.getApproveStatus());
        verify(assetService).save(application);
    }

    @Test
    void managerCannotApproveOwnApplication() {
        setCurrentUser(3L, Set.of("asset:manage"));
        Asset application = pendingApplication(5L, 3L);
        when(assetService.getById(5L)).thenReturn(application);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> controller.approve(5L, 1));

        assertEquals(403100, exception.getErrorCode().getCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    void approvedApplicationBecomesAssignedAndLeavesAvailableInventory() {
        setCurrentUser(3L, Set.of("asset:manage"));
        Asset application = pendingApplication(6L, 2L);
        when(assetService.getById(6L)).thenReturn(application);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        Asset approved = controller.approve(6L, 1).getData();

        assertEquals(1, approved.getApproveStatus());
        assertEquals(2, approved.getStatus());
        assertEquals(2L, approved.getUserId());
        verify(assetService).update(any(LambdaUpdateWrapper.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void approvedAvailableAssetApplicationConsumesSourceInventory() {
        setCurrentUser(3L, Set.of("asset:manage"));
        Asset application = pendingApplication(6L, 2L);
        application.setUserId(9L);
        application.setQuantity(2);
        Asset inventory = validAsset();
        inventory.setAssetId(9L);
        inventory.setQuantity(2);
        inventory.setStatus(1);
        inventory.setApproveStatus(1);
        when(assetService.getById(6L)).thenReturn(application);
        when(assetService.getById(9L)).thenReturn(inventory);
        when(assetService.update(any(LambdaUpdateWrapper.class))).thenReturn(true);

        Asset approved = controller.approve(6L, 1).getData();

        assertEquals(2L, approved.getUserId());
        ArgumentCaptor<LambdaUpdateWrapper<Asset>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(assetService, times(2)).update(captor.capture());
        assertTrue(captor.getAllValues().get(0).getSqlSegment().contains("quantity"));
    }

    @Test
    void managerCreatedInventoryCannotInjectApplicationOwner() {
        Asset asset = validAsset();
        asset.setApplyUserId(88L);
        asset.setUserId(77L);
        when(assetService.saveOrUpdate(asset)).thenReturn(true);

        controller.save(asset);

        assertNull(asset.getApplyUserId());
        assertNull(asset.getUserId());
        assertEquals(1, asset.getApproveStatus());
        assertEquals(1, asset.getStatus());
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
        asset.setApproveStatus(0);
        asset.setStatus(1);
        return asset;
    }

    private void setCurrentUser(Long userId, Set<String> permissions) {
        CurrentUserContext.set(new AuthSession(userId, "user" + userId, 3,
                Set.of("STAFF"), permissions, 1L));
    }
}
