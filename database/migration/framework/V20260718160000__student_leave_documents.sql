USE school_spring;

-- 学生可通过公文 OA 发起和跟踪请假申请；后端限制其不能发起其他公文类型。
INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('document:self', '发起并查看本人公文', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code = 'document:self'
WHERE r.role_code = 'STUDENT';
