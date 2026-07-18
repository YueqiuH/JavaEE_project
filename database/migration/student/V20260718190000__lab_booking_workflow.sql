USE school_spring;

ALTER TABLE `lab`
    ADD COLUMN `lab_no` VARCHAR(32) DEFAULT NULL AFTER `lab_id`,
    ADD COLUMN `manager_id` BIGINT DEFAULT NULL AFTER `description`,
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `status`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;

UPDATE `lab`
SET `lab_no` = CONCAT('LEGACYLAB', LPAD(`lab_id`, 8, '0'))
WHERE `lab_no` IS NULL;

ALTER TABLE `lab`
    MODIFY COLUMN `lab_no` VARCHAR(32) NOT NULL COMMENT '实验室编号',
    MODIFY COLUMN `capacity` INT NOT NULL DEFAULT 1 COMMENT '容纳人数',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 1 COMMENT '1=开放,0=维护中',
    ADD UNIQUE KEY `uk_lab_no` (`lab_no`),
    ADD KEY `idx_lab_manager_status` (`manager_id`, `status`);

CREATE TABLE `lab_resource` (
    `resource_id` BIGINT NOT NULL AUTO_INCREMENT,
    `lab_id` BIGINT NOT NULL,
    `resource_no` VARCHAR(32) NOT NULL,
    `resource_name` VARCHAR(64) NOT NULL,
    `resource_type` VARCHAR(16) NOT NULL COMMENT 'EQUIPMENT/WORKSTATION',
    `description` VARCHAR(500) DEFAULT NULL,
    `status` INT NOT NULL DEFAULT 1 COMMENT '1=可预约,0=维护中',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`resource_id`),
    UNIQUE KEY `uk_lab_resource_no` (`lab_id`, `resource_no`),
    KEY `idx_lab_resource_status` (`lab_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室设备与工位';

CREATE TABLE `lab_open_slot` (
    `slot_id` BIGINT NOT NULL AUTO_INCREMENT,
    `lab_id` BIGINT NOT NULL,
    `open_date` DATE NOT NULL,
    `start_period` INT NOT NULL,
    `end_period` INT NOT NULL,
    `created_by` BIGINT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`slot_id`),
    KEY `idx_lab_slot_date` (`lab_id`, `open_date`, `start_period`, `end_period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室开放时段';

ALTER TABLE `lab_booking`
    ADD COLUMN `booking_no` VARCHAR(32) DEFAULT NULL AFTER `booking_id`,
    ADD COLUMN `resource_id` BIGINT DEFAULT NULL AFTER `lab_id`,
    ADD COLUMN `cancelled_at` DATETIME DEFAULT NULL AFTER `create_time`,
    ADD COLUMN `completed_at` DATETIME DEFAULT NULL AFTER `cancelled_at`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `completed_at`;

UPDATE `lab_booking`
SET `booking_no` = COALESCE(`booking_no`, CONCAT('LEGACYBOOK', LPAD(`booking_id`, 8, '0'))),
    `purpose` = COALESCE(`purpose`, '历史预约');

ALTER TABLE `lab_booking`
    MODIFY COLUMN `booking_no` VARCHAR(32) NOT NULL COMMENT '预约编号',
    MODIFY COLUMN `purpose` VARCHAR(256) NOT NULL COMMENT '预约用途',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 1 COMMENT '1=已预约,2=已取消,3=已完成',
    ADD UNIQUE KEY `uk_lab_booking_no` (`booking_no`),
    ADD KEY `idx_lab_booking_student_status` (`student_id`, `status`, `booking_date`),
    ADD KEY `idx_lab_booking_resource_date` (`resource_id`, `booking_date`, `status`);

CREATE TABLE `lab_booking_period` (
    `booking_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    `booking_date` DATE NOT NULL,
    `period_no` INT NOT NULL,
    PRIMARY KEY (`booking_id`, `period_no`),
    UNIQUE KEY `uk_booking_resource_period` (`resource_id`, `booking_date`, `period_no`),
    UNIQUE KEY `uk_booking_student_period` (`student_id`, `booking_date`, `period_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约冲突占用明细';

CREATE TABLE `lab_booking_notice` (
    `notice_id` BIGINT NOT NULL AUTO_INCREMENT,
    `booking_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    `title` VARCHAR(128) NOT NULL,
    `content` VARCHAR(500) NOT NULL,
    `is_read` INT NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at` DATETIME DEFAULT NULL,
    PRIMARY KEY (`notice_id`),
    KEY `idx_lab_notice_student_read` (`student_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室预约站内通知';

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('lab:read', '查看实验室与可预约资源'),
    ('lab:manage-self', '维护本人负责的实验室'),
    ('lab:resource:manage-self', '维护本人实验室的设备与工位'),
    ('lab:slot:manage-self', '维护本人实验室开放时段'),
    ('lab:booking:read-self', '查看本人实验室预约'),
    ('lab:booking:create', '创建实验室预约'),
    ('lab:booking:cancel-self', '取消本人实验室预约'),
    ('lab:booking:read-managed', '查看本人实验室的预约记录'),
    ('lab:booking:complete-managed', '完成本人实验室的预约'),
    ('lab:notice:read-self', '查看本人预约通知'),
    ('lab:notice:mark-self', '标记本人预约通知')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'lab:read', 'lab:booking:read-self', 'lab:booking:create',
    'lab:booking:cancel-self', 'lab:notice:read-self', 'lab:notice:mark-self')
WHERE r.role_code = 'STUDENT';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'lab:read', 'lab:manage-self', 'lab:resource:manage-self',
    'lab:slot:manage-self', 'lab:booking:read-managed', 'lab:booking:complete-managed')
WHERE r.role_code = 'TEACHER';
