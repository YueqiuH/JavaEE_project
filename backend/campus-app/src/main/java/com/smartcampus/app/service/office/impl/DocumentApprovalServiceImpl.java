package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.DocumentApprovalMapper;
import com.smartcampus.app.service.office.IDocumentApprovalService;
import com.smartcampus.contract.entity.DocumentApproval;
import org.springframework.stereotype.Service;

@Service
public class DocumentApprovalServiceImpl extends ServiceImpl<DocumentApprovalMapper, DocumentApproval> implements IDocumentApprovalService {
}
