package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.DocumentApproverMapper;
import com.smartcampus.app.service.office.IDocumentApproverService;
import com.smartcampus.contract.entity.DocumentApprover;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentApproverServiceImpl
        extends ServiceImpl<DocumentApproverMapper, DocumentApprover>
        implements IDocumentApproverService {

    @Override
    public List<DocumentApprover> listAvailable() {
        return baseMapper.findAvailable();
    }

    @Override
    public boolean isAvailable(Long userId) {
        return userId != null && baseMapper.countAvailableByUserId(userId) > 0;
    }
}
