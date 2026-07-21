package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.DocumentWorkflowStep;

import java.util.List;

public interface IDocumentWorkflowService extends IService<DocumentWorkflow> {

    List<DocumentWorkflow> listActive();

    DocumentWorkflow getActive(String docType);

    DocumentWorkflow createVersion(String docType, String workflowName,
                                   List<DocumentWorkflowStep> steps, Long adminId);
}
