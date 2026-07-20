-- ============================================
-- V7: 移除毕业设计管理功能
-- 通过新增迁移安全删除旧表，不修改 V1-V6
-- ============================================
DROP TABLE IF EXISTS `graduation_report`;
DROP TABLE IF EXISTS `graduation_selection`;
DROP TABLE IF EXISTS `graduation_topic`;
