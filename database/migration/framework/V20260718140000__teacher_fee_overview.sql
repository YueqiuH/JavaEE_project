USE school_spring;

-- 教师可查看学生缴费概览；管理员和教职工保留账单管理能力，但不能查看学生缴费情况。
INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('fee:overview:read', '查询学生缴费概览', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

DELETE rp
FROM `role_permission` rp
JOIN `role` r ON r.role_id = rp.role_id
JOIN `permission` p ON p.permission_id = rp.permission_id
WHERE r.role_code IN ('ADMIN', 'STAFF')
  AND p.permission_code = 'fee:overview:read';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code = 'fee:overview:read'
WHERE r.role_code = 'TEACHER';
