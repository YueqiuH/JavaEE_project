package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.DocumentApprovalTaskMapper;
import com.smartcampus.app.service.office.IDocumentApprovalTaskService;
import com.smartcampus.contract.entity.DocumentApprovalTask;
import org.springframework.stereotype.Service;

@Service
public class DocumentApprovalTaskServiceImpl
        extends ServiceImpl<DocumentApprovalTaskMapper, DocumentApprovalTask>
        implements IDocumentApprovalTaskService {

    @Override
    public int countOpenByApprover(Long userId) {
        return baseMapper.countOpenByApprover(userId);
    }
}
