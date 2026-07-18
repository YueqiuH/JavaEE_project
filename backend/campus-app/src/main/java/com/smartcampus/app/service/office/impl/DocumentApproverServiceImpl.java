package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartcampus.app.dao.office.DocumentApproverMapper;
import com.smartcampus.app.enums.OfficeErrorCodeConstants;
import com.smartcampus.app.service.office.IDocumentApprovalTaskService;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.common.exception.BusinessException;
import com.smartcampus.contract.entity.DocumentApprover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DocumentApproverServiceImpl
        extends ServiceImpl<DocumentApproverMapper, DocumentApprover>
        implements IDocumentApproverService {

    @Autowired
    private IDocumentApprovalTaskService taskService;

    @Override
    public List<DocumentApprover> listAvailable() {
        return baseMapper.findAvailable();
    }

    @Override
    public List<DocumentApprover> listCandidates() {
        return baseMapper.findCandidates();
    }

    @Override
    public boolean isAvailable(Long userId) {
        return userId != null && baseMapper.countAvailableByUserId(userId) > 0;
    }

    @Override
    public DocumentApprover updateQualification(Long userId, String displayName,
                                                boolean enabled, Long adminId) {
        if (userId == null || baseMapper.countEligibleUser(userId) == 0) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST,
                    "审批人只能是已启用的教师、教职工或管理员，不能是学生");
        }
        DocumentApprover config = baseMapper.selectOne(new LambdaQueryWrapper<DocumentApprover>()
                .eq(DocumentApprover::getUserId, userId));
        if (config == null && !enabled) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT, "该用户当前没有审批资格");
        }
        if (!enabled && (baseMapper.countActiveWorkflowReferences(userId) > 0
                || taskService.countOpenByApprover(userId) > 0)) {
            throw new BusinessException(OfficeErrorCodeConstants.STATE_CONFLICT,
                    "该用户仍在启用流程或未结束审批任务中，请先调整流程并完成在途任务");
        }
        if (config == null) {
            config = new DocumentApprover();
            config.setUserId(userId);
            config.setCreateTime(new Date());
        }
        String normalizedName = displayName == null ? "" : displayName.trim();
        if (enabled && normalizedName.isBlank()) {
            throw new BusinessException(OfficeErrorCodeConstants.BAD_REQUEST, "审批人显示名称不能为空");
        }
        if (!normalizedName.isBlank()) {
            config.setDisplayName(normalizedName);
        }
        config.setStatus(enabled ? 1 : 0);
        config.setUpdatedBy(adminId);
        config.setUpdatedTime(new Date());
        saveOrUpdate(config);
        return config;
    }
}
