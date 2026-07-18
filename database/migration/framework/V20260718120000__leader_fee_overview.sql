-- 为管理员和校领导提供学生缴费情况查询权限；校领导仍只有只读权限。
INSERT INTO `role` (`role_code`, `role_name`, `status`) VALUES
    ('LEADER', '校领导', 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `status` = 1;

INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('fee:overview:read', '查询学生缴费概览', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

-- 演示领导账号，密码与其他本地演示账号一致：123321。
INSERT INTO `user` (`username`, `password`, `user_type`, `status`) VALUES
    ('leader', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, 1)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`), `user_type` = 3, `status` = 1;

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
SELECT u.user_id, r.role_id
FROM `user` u
JOIN `role` r ON r.role_code = 'LEADER'
WHERE u.username = 'leader';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
CROSS JOIN `permission` p
WHERE (r.role_code = 'LEADER' AND p.permission_code IN ('office:read', 'fee:overview:read'))
   OR (r.role_code = 'ADMIN' AND p.permission_code = 'fee:overview:read');
