ALTER TABLE competition_team
    ADD COLUMN material_storage_name VARCHAR(128) NULL COMMENT '报名材料内部存储名' AFTER material_url,
    ADD COLUMN material_original_name VARCHAR(255) NULL COMMENT '报名材料原文件名' AFTER material_storage_name,
    ADD COLUMN material_content_type VARCHAR(128) NULL COMMENT '报名材料媒体类型' AFTER material_original_name,
    ADD COLUMN material_size BIGINT NULL COMMENT '报名材料字节数' AFTER material_content_type;
