package com.smartcampus.app.service.office;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.contract.entity.DocumentApprover;

import java.util.List;

public interface IDocumentApproverService extends IService<DocumentApprover> {

    List<DocumentApprover> listAvailable();

    List<DocumentApprover> listCandidates();

    boolean isAvailable(Long userId);

    DocumentApprover updateQualification(Long userId, String displayName, boolean enabled, Long adminId);
}
