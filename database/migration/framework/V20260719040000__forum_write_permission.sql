-- ============================================
-- 新增 forum:write 权限（发帖/点赞/删帖需要写权限）
-- ============================================
INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('forum:write', '发布和操作论坛内容')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- ADMIN 和 TEACHER 获得 forum:write
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r CROSS JOIN `permission` p
WHERE p.permission_code = 'forum:write'
  AND r.role_code IN ('ADMIN', 'TEACHER', 'COUNSELOR', 'STAFF');
