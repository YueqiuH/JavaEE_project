-- ============================================
-- 成员 D：论坛权限独立 — 学生只能看论坛，不能看数据报表
-- ============================================

-- 1. 新增 forum:read 权限
INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('forum:read', '查看新闻与论坛')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 2. 所有角色都有 forum:read
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r CROSS JOIN `permission` p
WHERE p.permission_code = 'forum:read';

-- 3. 移除学生的 base:read 权限
DELETE rp FROM `role_permission` rp
JOIN `role` r ON r.role_id = rp.role_id
JOIN `permission` p ON p.permission_id = rp.permission_id
WHERE r.role_code = 'STUDENT' AND p.permission_code = 'base:read';
