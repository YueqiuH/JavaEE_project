USE school_spring;

ALTER TABLE `user`
    MODIFY COLUMN `username` VARCHAR(64) NOT NULL,
    MODIFY COLUMN `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    MODIFY COLUMN `user_type` TINYINT NOT NULL DEFAULT 1,
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 AFTER `user_type`,
    ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `status`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`,
    ADD UNIQUE KEY `uk_user_username` (`username`);

CREATE TABLE `role` (
    `role_id` BIGINT NOT NULL AUTO_INCREMENT,
    `role_code` VARCHAR(32) NOT NULL,
    `role_name` VARCHAR(64) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `permission` (
    `permission_id` BIGINT NOT NULL AUTO_INCREMENT,
    `permission_code` VARCHAR(64) NOT NULL,
    `permission_name` VARCHAR(64) NOT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user_role` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_user_role_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `role_permission` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_role_permission_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`permission_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `menu`
    DROP COLUMN `user_type`,
    ADD COLUMN `permission_code` VARCHAR(64) DEFAULT NULL AFTER `parent_id`,
    ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `permission_code`,
    ADD COLUMN `status` TINYINT NOT NULL DEFAULT 1 AFTER `sort_order`,
    ADD UNIQUE KEY `uk_menu_path` (`path`);

INSERT INTO `role` (`role_code`, `role_name`) VALUES
    ('STUDENT', '学生'), ('TEACHER', '教师'), ('STAFF', '教职工'), ('ADMIN', '系统管理员');

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('teaching:read', '读取教务数据'), ('teaching:write', '维护教务数据'),
    ('student:read', '读取学生事务'), ('student:write', '维护学生事务'),
    ('office:read', '读取办公数据'), ('office:write', '维护办公数据'),
    ('base:read', '读取基础数据'), ('base:write', '维护基础数据'),
    ('admin:manage', '系统管理');

INSERT INTO `user` (`username`, `password`, `user_type`, `status`) VALUES
    ('600001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 1, 1),
    ('700001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, 1),
    ('800001', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, 1),
    ('admin', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 4, 1);

INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON
    (u.username = '600001' AND r.role_code = 'STUDENT') OR
    (u.username = '700001' AND r.role_code = 'TEACHER') OR
    (u.username = '800001' AND r.role_code = 'STAFF') OR
    (u.username = 'admin' AND r.role_code = 'ADMIN');

INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id FROM `role` r CROSS JOIN `permission` p WHERE
    r.role_code = 'ADMIN'
    OR (r.role_code = 'STUDENT' AND p.permission_code IN ('teaching:read', 'student:read', 'student:write', 'base:read'))
    OR (r.role_code = 'TEACHER' AND p.permission_code IN ('teaching:read', 'teaching:write', 'student:read', 'base:read'))
    OR (r.role_code = 'STAFF' AND p.permission_code IN ('student:read', 'student:write', 'office:read', 'office:write', 'base:read'));

INSERT INTO `menu` (`title`, `path`, `permission_code`, `sort_order`) VALUES
    ('教务核心', '/home/course-schedule', 'teaching:read', 10),
    ('学生事务', '/home/student-status', 'student:read', 20),
    ('协同办公', '/home/work-plan', 'office:read', 30),
    ('基础数据', '/home/user-management', 'base:read', 40);
