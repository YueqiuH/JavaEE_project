USE school_spring;

ALTER TABLE `scholarship`
    ADD COLUMN `application_no` VARCHAR(32) DEFAULT NULL AFTER `scholarship_id`,
    MODIFY COLUMN `scholarship_type` VARCHAR(32) NOT NULL,
    ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL AFTER `apply_time`,
    ADD COLUMN `selected_at` DATETIME DEFAULT NULL AFTER `reviewed_at`,
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `selected_at`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;

UPDATE `scholarship`
SET `application_no` = CONCAT('LEGACY', LPAD(`scholarship_id`, 12, '0'))
WHERE `application_no` IS NULL;

UPDATE `scholarship`
SET `status` = CASE `status`
    WHEN 0 THEN 1
    WHEN 1 THEN 3
    WHEN 2 THEN 4
    ELSE `status`
END;

ALTER TABLE `scholarship`
    MODIFY COLUMN `application_no` VARCHAR(32) NOT NULL COMMENT '申请编号',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 0 COMMENT '0=草稿,1=已提交,2=已退回,3=已通过,4=已拒绝,5=已撤回,6=已入选',
    MODIFY COLUMN `apply_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    ADD UNIQUE KEY `uk_scholarship_application_no` (`application_no`),
    ADD KEY `idx_scholarship_student_status` (`student_id`, `status`),
    ADD KEY `idx_scholarship_status_updated` (`status`, `updated_at`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('scholarship:application:read-self', '查看本人奖助贷申请'),
    ('scholarship:application:create', '创建奖助贷申请'),
    ('scholarship:application:update-self', '修改本人奖助贷申请'),
    ('scholarship:application:submit-self', '提交本人奖助贷申请'),
    ('scholarship:application:withdraw-self', '撤回本人奖助贷申请'),
    ('scholarship:review:read', '查看奖助贷评审队列'),
    ('scholarship:review:submit', '提交奖助贷评审结论'),
    ('scholarship:result:generate', '生成奖助贷资助名单')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'scholarship:application:read-self', 'scholarship:application:create',
    'scholarship:application:update-self', 'scholarship:application:submit-self',
    'scholarship:application:withdraw-self')
WHERE r.role_code = 'STUDENT';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'scholarship:review:read', 'scholarship:review:submit', 'scholarship:result:generate')
WHERE r.role_code = 'TEACHER';

INSERT INTO `student` (`student_name`, `student_no`, `student_age`)
SELECT '演示学生', 600001, 20
WHERE NOT EXISTS (SELECT 1 FROM `student` WHERE `student_no` = 600001);
