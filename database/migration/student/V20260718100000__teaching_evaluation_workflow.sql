USE school_spring;

ALTER TABLE `evaluation`
    ADD COLUMN `schedule_id` BIGINT DEFAULT NULL AFTER `student_id`,
    ADD UNIQUE KEY `uk_evaluation_student_schedule` (`student_id`, `schedule_id`),
    ADD KEY `idx_evaluation_teacher_semester` (`teacher_id`, `semester`),
    ADD KEY `idx_evaluation_teacher_course` (`teacher_id`, `course_id`, `semester`);

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('evaluation:task:read-self', '查看本人评教任务'),
    ('evaluation:submit-self', '提交本人匿名评教'),
    ('evaluation:result:read-self', '查看个人评教结果')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code IN (
    'evaluation:task:read-self', 'evaluation:submit-self')
WHERE r.role_code = 'STUDENT';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code = 'evaluation:result:read-self'
WHERE r.role_code = 'TEACHER';
