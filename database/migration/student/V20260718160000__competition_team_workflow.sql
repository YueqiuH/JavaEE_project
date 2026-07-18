USE school_spring;

ALTER TABLE `competition`
    ADD COLUMN `competition_no` VARCHAR(32) DEFAULT NULL AFTER `competition_id`,
    ADD COLUMN `requirements` TEXT DEFAULT NULL AFTER `description`,
    ADD COLUMN `min_members` INT NOT NULL DEFAULT 1 AFTER `deadline`,
    ADD COLUMN `max_members` INT NOT NULL DEFAULT 5 AFTER `min_members`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `create_time`,
    ADD COLUMN `published_at` DATETIME DEFAULT NULL AFTER `updated_at`,
    ADD COLUMN `closed_at` DATETIME DEFAULT NULL AFTER `published_at`;

UPDATE `competition`
SET `competition_no` = CONCAT('LEGACYJS', LPAD(`competition_id`, 10, '0')),
    `requirements` = COALESCE(NULLIF(`description`, ''), '请联系竞赛发布教师确认参赛要求'),
    `status` = CASE WHEN `status` IN (2, 3) THEN 2 ELSE `status` END
WHERE `competition_no` IS NULL;

ALTER TABLE `competition`
    MODIFY COLUMN `competition_no` VARCHAR(32) NOT NULL COMMENT '竞赛编号',
    MODIFY COLUMN `requirements` TEXT NOT NULL COMMENT '参赛要求',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 0 COMMENT '0=草稿,1=报名中,2=已关闭',
    ADD UNIQUE KEY `uk_competition_no` (`competition_no`),
    ADD KEY `idx_competition_publisher_status` (`publisher_id`, `status`),
    ADD KEY `idx_competition_status_deadline` (`status`, `deadline`);

ALTER TABLE `competition_team`
    ADD COLUMN `registration_no` VARCHAR(32) DEFAULT NULL AFTER `team_id`,
    ADD COLUMN `material_url` VARCHAR(256) DEFAULT NULL AFTER `leader_id`,
    ADD COLUMN `material_description` VARCHAR(1000) DEFAULT NULL AFTER `material_url`,
    ADD COLUMN `review_opinion` VARCHAR(500) DEFAULT NULL AFTER `reviewer_id`,
    ADD COLUMN `submitted_at` DATETIME DEFAULT NULL AFTER `apply_time`,
    ADD COLUMN `reviewed_at` DATETIME DEFAULT NULL AFTER `submitted_at`,
    ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `reviewed_at`;

UPDATE `competition_team`
SET `registration_no` = CONCAT('LEGACYTD', LPAD(`team_id`, 10, '0')),
    `status` = CASE `status` WHEN 0 THEN 1 WHEN 1 THEN 2 WHEN 2 THEN 4 ELSE `status` END,
    `submitted_at` = COALESCE(`submitted_at`, `apply_time`)
WHERE `registration_no` IS NULL;

ALTER TABLE `competition_team`
    MODIFY COLUMN `registration_no` VARCHAR(32) NOT NULL COMMENT '报名编号',
    MODIFY COLUMN `status` INT NOT NULL DEFAULT 0 COMMENT '0=组队中,1=待审核,2=已通过,3=已退回,4=已拒绝',
    ADD UNIQUE KEY `uk_competition_registration_no` (`registration_no`),
    ADD UNIQUE KEY `uk_competition_team_name` (`competition_id`, `team_name`),
    ADD KEY `idx_competition_team_leader_status` (`leader_id`, `status`),
    ADD KEY `idx_competition_team_competition_status` (`competition_id`, `status`);

ALTER TABLE `competition_member`
    ADD COLUMN `invitation_status` INT NOT NULL DEFAULT 1 AFTER `role`,
    ADD COLUMN `invited_by` BIGINT DEFAULT NULL AFTER `invitation_status`,
    ADD COLUMN `invited_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `invited_by`,
    ADD COLUMN `responded_at` DATETIME DEFAULT NULL AFTER `invited_at`,
    ADD UNIQUE KEY `uk_competition_team_student` (`team_id`, `student_id`),
    ADD KEY `idx_competition_member_student_status` (`student_id`, `invitation_status`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('competition:read', '查看学科竞赛'),
    ('competition:publish', '发布学科竞赛'),
    ('competition:manage-self', '维护本人发布的竞赛'),
    ('competition:team:read-self', '查看本人竞赛队伍'),
    ('competition:team:create', '发起竞赛组队'),
    ('competition:team:manage-self', '维护本人负责的竞赛队伍'),
    ('competition:team:submit-self', '提交本人竞赛报名'),
    ('competition:invitation:respond-self', '处理本人组队邀请'),
    ('competition:review:read-self', '查看本人竞赛的审核队列'),
    ('competition:review:submit-self', '审核本人竞赛的参赛队伍')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'competition:read', 'competition:team:read-self', 'competition:team:create',
    'competition:team:manage-self', 'competition:team:submit-self',
    'competition:invitation:respond-self')
WHERE r.role_code = 'STUDENT';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'competition:read', 'competition:publish', 'competition:manage-self',
    'competition:review:read-self', 'competition:review:submit-self')
WHERE r.role_code = 'TEACHER';
