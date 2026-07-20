-- ============================================
-- 移除毕业设计管理模块
-- 删除 graduation_topic / graduation_selection / graduation_report 三张表
-- ============================================
DROP TABLE IF EXISTS `graduation_report`;
DROP TABLE IF EXISTS `graduation_selection`;
DROP TABLE IF EXISTS `graduation_topic`;
