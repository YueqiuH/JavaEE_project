package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.DocumentApprovalTask;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DocumentApprovalTaskMapper extends BaseMapper<DocumentApprovalTask> {

    @Select("SELECT COUNT(*) FROM document_approval_task WHERE approver_id = #{userId} AND status IN (0, 1)")
    int countOpenByApprover(@Param("userId") Long userId);
}
