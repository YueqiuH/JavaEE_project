package com.smartcampus.app.dao.office;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.contract.entity.DocumentWorkflow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DocumentWorkflowMapper extends BaseMapper<DocumentWorkflow> {

    @Select("""
            SELECT * FROM document_workflow
            WHERE doc_type = #{docType} AND status = 1
            ORDER BY version DESC LIMIT 1
            """)
    DocumentWorkflow findActiveByType(@Param("docType") String docType);

    @Select("SELECT COALESCE(MAX(version), 0) FROM document_workflow WHERE doc_type = #{docType}")
    int findMaxVersion(@Param("docType") String docType);
}
