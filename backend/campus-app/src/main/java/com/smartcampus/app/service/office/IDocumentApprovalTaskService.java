package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.DocumentApprovalTask;

public interface IDocumentApprovalTaskService extends IService<DocumentApprovalTask> {

    int countOpenByApprover(Long userId);
}
