package com.smartcampus.app.service.office.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.app.dao.office.DocumentWorkflowMapper;
import com.smartcampus.app.dao.office.DocumentWorkflowStepMapper;
import com.smartcampus.app.service.office.IDocumentWorkflowService;
import com.smartcampus.contract.entity.DocumentWorkflow;
import com.smartcampus.contract.entity.DocumentWorkflowStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class DocumentWorkflowServiceImpl
        extends ServiceImpl<DocumentWorkflowMapper, DocumentWorkflow>
        implements IDocumentWorkflowService {

    @Autowired
    private DocumentWorkflowStepMapper stepMapper;

    @Override
    public List<DocumentWorkflow> listActive() {
        List<DocumentWorkflow> workflows = list(new LambdaQueryWrapper<DocumentWorkflow>()
                .eq(DocumentWorkflow::getStatus, 1)
                .orderByAsc(DocumentWorkflow::getDocType));
        workflows.forEach(workflow -> workflow.setSteps(stepMapper.findByWorkflowId(workflow.getWorkflowId())));
        return workflows;
    }

    @Override
    public DocumentWorkflow getActive(String docType) {
        DocumentWorkflow workflow = baseMapper.findActiveByType(docType);
        if (workflow != null) {
            workflow.setSteps(stepMapper.findByWorkflowId(workflow.getWorkflowId()));
        }
        return workflow;
    }

    @Override
    @Transactional
    public DocumentWorkflow createVersion(String docType, String workflowName,
                                          List<DocumentWorkflowStep> steps, Long adminId) {
        update(new LambdaUpdateWrapper<DocumentWorkflow>()
                .eq(DocumentWorkflow::getDocType, docType)
                .eq(DocumentWorkflow::getStatus, 1)
                .set(DocumentWorkflow::getStatus, 0));

        DocumentWorkflow workflow = new DocumentWorkflow();
        workflow.setWorkflowName(workflowName);
        workflow.setDocType(docType);
        workflow.setVersion(baseMapper.findMaxVersion(docType) + 1);
        workflow.setStatus(1);
        workflow.setCreatedBy(adminId);
        workflow.setCreateTime(new Date());
        save(workflow);

        for (int index = 0; index < steps.size(); index++) {
            DocumentWorkflowStep source = steps.get(index);
            DocumentWorkflowStep step = new DocumentWorkflowStep();
            step.setWorkflowId(workflow.getWorkflowId());
            step.setStepOrder(index + 1);
            step.setStepName(source.getStepName().trim());
            step.setApproverId(source.getApproverId());
            stepMapper.insert(step);
        }
        workflow.setSteps(stepMapper.findByWorkflowId(workflow.getWorkflowId()));
        return workflow;
    }
}
