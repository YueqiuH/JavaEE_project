-- ============================================
-- Flyway 兼容: backport forum:write 权限到 Flyway 迁移路径
-- ============================================
INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('forum:write', '发布和操作论坛内容')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r CROSS JOIN `permission` p
WHERE p.permission_code = 'forum:write'
  AND r.role_code IN ('ADMIN', 'TEACHER', 'COUNSELOR', 'STAFF');
