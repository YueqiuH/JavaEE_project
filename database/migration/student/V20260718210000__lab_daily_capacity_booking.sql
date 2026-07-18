ALTER TABLE `lab_booking`
    MODIFY COLUMN `resource_id` BIGINT NULL COMMENT '历史设备或工位ID，新预约不再使用',
    MODIFY COLUMN `start_period` INT NULL COMMENT '历史开始节次，新预约不再使用',
    MODIFY COLUMN `end_period` INT NULL COMMENT '历史结束节次，新预约不再使用',
    ADD COLUMN `check_in_at` DATETIME NULL AFTER `cancelled_at`,
    ADD COLUMN `check_out_at` DATETIME NULL AFTER `check_in_at`,
    ADD COLUMN `expires_at` DATETIME NULL AFTER `check_out_at`,
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 1 COMMENT '1=待签到,2=已取消,3=已签退,4=使用中,5=已过期';

UPDATE `lab_booking`
SET `check_out_at` = COALESCE(`check_out_at`, `completed_at`)
WHERE `status` = 3;

UPDATE `lab_booking`
SET `expires_at` = DATE_ADD(COALESCE(`create_time`, CURRENT_TIMESTAMP), INTERVAL 30 MINUTE)
WHERE `status` = 1 AND `expires_at` IS NULL;

ALTER TABLE `lab_booking`
    ADD KEY `idx_lab_booking_capacity` (`lab_id`, `booking_date`, `status`, `expires_at`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('lab:booking:check-in-self', '本人实验室预约签到'),
    ('lab:booking:check-out-self', '本人实验室预约签退')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'lab:booking:check-in-self', 'lab:booking:check-out-self')
WHERE r.role_code = 'STUDENT';
