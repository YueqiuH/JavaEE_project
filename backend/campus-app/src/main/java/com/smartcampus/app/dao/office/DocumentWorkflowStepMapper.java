package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.DocumentWorkflowStep;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DocumentWorkflowStepMapper extends BaseMapper<DocumentWorkflowStep> {

    @Select("""
            SELECT s.step_id, s.workflow_id, s.step_order, s.step_name, s.approver_id,
                   da.display_name AS approver_name, u.username AS approver_username
            FROM document_workflow_step s
            JOIN `user` u ON u.user_id = s.approver_id
            LEFT JOIN document_approver da ON da.user_id = s.approver_id
            WHERE s.workflow_id = #{workflowId}
            ORDER BY s.step_order
            """)
    List<DocumentWorkflowStep> findByWorkflowId(@Param("workflowId") Long workflowId);
}
