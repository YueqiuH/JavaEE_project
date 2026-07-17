USE school_spring;

CREATE TABLE `student_profile_extension` (
    `student_id` BIGINT NOT NULL,
    `current_address` VARCHAR(128) DEFAULT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `email` VARCHAR(128) DEFAULT NULL,
    `emergency_contact` VARCHAR(32) DEFAULT NULL,
    `emergency_phone` VARCHAR(20) DEFAULT NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生非核心联系信息扩展表';

ALTER TABLE `student_status_change`
    ADD COLUMN `application_no` VARCHAR(32) DEFAULT NULL AFTER `change_id`,
    MODIFY COLUMN `change_type` VARCHAR(32) NOT NULL,
    ADD COLUMN `desired_effective_date` DATE DEFAULT NULL AFTER `new_major_id`,
    ADD COLUMN `counselor_reviewed_at` DATETIME DEFAULT NULL AFTER `counselor_opinion`,
    CHANGE COLUMN `admin_id` `academic_reviewer_id` BIGINT DEFAULT NULL COMMENT '教务复审人ID',
    CHANGE COLUMN `admin_opinion` `academic_opinion` VARCHAR(256) DEFAULT NULL COMMENT '教务复审意见',
    ADD COLUMN `academic_reviewed_at` DATETIME DEFAULT NULL AFTER `academic_opinion`,
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `apply_time`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;

UPDATE `student_status_change`
SET `application_no` = CONCAT('LEGACYXJ', LPAD(`change_id`, 10, '0')),
    `desired_effective_date` = COALESCE(DATE(`apply_time`), CURRENT_DATE)
WHERE `application_no` IS NULL;

UPDATE `student_status_change`
SET `status` = CASE `status`
    WHEN 0 THEN 1
    WHEN 1 THEN 2
    WHEN 2 THEN 3
    WHEN 3 THEN 4
    ELSE `status`
END;

ALTER TABLE `student_status_change`
    MODIFY COLUMN `application_no` VARCHAR(32) NOT NULL COMMENT '申请编号',
    MODIFY COLUMN `desired_effective_date` DATE NOT NULL COMMENT '期望生效日期',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 0 COMMENT '0=草稿,1=辅导员初审,2=教务复审,3=通过,4=初审拒绝,5=复审拒绝,6=撤回',
    MODIFY COLUMN `apply_time` DATETIME DEFAULT NULL COMMENT '提交时间',
    ADD UNIQUE KEY `uk_status_change_application_no` (`application_no`),
    ADD KEY `idx_status_change_student_status` (`student_id`, `status`),
    ADD KEY `idx_status_change_status_updated` (`status`, `updated_at`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('status:profile:read-self', '查看本人非核心信息'),
    ('status:profile:update-self', '修改本人非核心信息'),
    ('status:change:read-self', '查看本人学籍异动申请'),
    ('status:change:create', '创建学籍异动申请'),
    ('status:change:update-self', '修改本人学籍异动草稿'),
    ('status:change:submit-self', '提交本人学籍异动申请'),
    ('status:change:withdraw-self', '撤回本人学籍异动申请'),
    ('status:review:read', '查看学籍异动审核队列'),
    ('status:review:submit', '提交学籍异动审核结论')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'status:profile:read-self', 'status:profile:update-self', 'status:change:read-self',
    'status:change:create', 'status:change:update-self', 'status:change:submit-self',
    'status:change:withdraw-self')
WHERE r.role_code = 'STUDENT';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN ('status:review:read', 'status:review:submit')
WHERE r.role_code = 'TEACHER';
