USE school_spring;

-- 勤工俭学任务保留原指派人、工资和结算状态；历史“指派任务”缺少可靠指派人，不自动转为可发薪任务。
ALTER TABLE `work_plan`
    ADD COLUMN `assigner_id` BIGINT DEFAULT NULL COMMENT '勤工俭学原指派人ID' AFTER `supervisor_comment`,
    ADD COLUMN `wage_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '任务工资' AFTER `assigner_id`,
    ADD COLUMN `wage_paid` TINYINT NOT NULL DEFAULT 0 COMMENT '工资是否已发放：0=否,1=是' AFTER `wage_amount`,
    ADD COLUMN `wage_paid_time` DATETIME DEFAULT NULL COMMENT '工资发放时间' AFTER `wage_paid`,
    ADD KEY `idx_work_plan_assigner` (`assigner_id`, `status`, `wage_paid`);

ALTER TABLE `work_plan`
    MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '计划人或勤工俭学学生ID',
    MODIFY COLUMN `plan_type` VARCHAR(16) NOT NULL COMMENT '类型：周计划/月计划/勤工俭学；指派任务为历史兼容值',
    MODIFY COLUMN `status` INT DEFAULT 1 COMMENT '普通计划：1=进行中,2=已完成；勤工俭学：1=进行中,2=待确认,3=已结算';

-- 每个任务最多生成一笔工资流水，唯一索引作为重复发薪的数据库兜底。
ALTER TABLE `payment`
    ADD COLUMN `work_plan_id` BIGINT DEFAULT NULL COMMENT '勤工俭学任务ID' AFTER `fee_id`,
    MODIFY COLUMN `payment_type` VARCHAR(16) DEFAULT '学费' COMMENT '类型：学费/一卡通充值/勤工俭学工资/消费',
    ADD UNIQUE KEY `uk_payment_work_plan` (`work_plan_id`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p
  ON (r.role_code = 'STUDENT' AND p.permission_code = 'work-plan:self')
  OR (r.role_code = 'TEACHER' AND p.permission_code = 'work-plan:manage')
WHERE r.role_code IN ('STUDENT', 'TEACHER');
