USE school_spring;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'payment'
       AND COLUMN_NAME = 'work_plan_id') = 0,
    'ALTER TABLE `payment`
       ADD COLUMN `work_plan_id` BIGINT DEFAULT NULL AFTER `fee_id`,
       MODIFY COLUMN `payment_type` VARCHAR(16) DEFAULT ''学费'',
       ADD UNIQUE KEY `uk_payment_work_plan` (`work_plan_id`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'asset'
       AND COLUMN_NAME = 'application_type') = 0,
    'ALTER TABLE `asset`
       ADD COLUMN `application_type` VARCHAR(16) DEFAULT NULL AFTER `apply_user_id`,
       ADD COLUMN `source_asset_id` BIGINT DEFAULT NULL AFTER `application_type`,
       ADD COLUMN `application_reason` VARCHAR(512) DEFAULT NULL AFTER `source_asset_id`,
       ADD COLUMN `approve_user_id` BIGINT DEFAULT NULL AFTER `approve_status`,
       ADD COLUMN `approve_remark` VARCHAR(256) DEFAULT NULL AFTER `approve_user_id`,
       ADD COLUMN `approve_time` DATETIME DEFAULT NULL AFTER `approve_remark`,
       ADD KEY `idx_asset_application` (`application_type`, `approve_status`, `create_time`),
       ADD KEY `idx_asset_source` (`source_asset_id`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'work_plan'
       AND COLUMN_NAME = 'assigner_id') = 0,
    'ALTER TABLE `work_plan`
       ADD COLUMN `assigner_id` BIGINT DEFAULT NULL AFTER `supervisor_comment`,
       ADD COLUMN `wage_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 AFTER `assigner_id`,
       ADD COLUMN `wage_paid` TINYINT NOT NULL DEFAULT 0 AFTER `wage_amount`,
       ADD COLUMN `wage_paid_time` DATETIME DEFAULT NULL AFTER `wage_paid`,
       ADD KEY `idx_work_plan_assigner` (`assigner_id`, `status`, `wage_paid`)',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document'
       AND COLUMN_NAME = 'workflow_id') = 0,
    'ALTER TABLE `document`
       ADD COLUMN `workflow_id` BIGINT DEFAULT NULL AFTER `approval_chain`,
       ADD COLUMN `current_step` INT DEFAULT NULL AFTER `workflow_id`,
       ADD COLUMN `approval_round` INT NOT NULL DEFAULT 1 AFTER `current_step`',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

SET @office_ddl = IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'document_approval'
       AND COLUMN_NAME = 'task_id') = 0,
    'ALTER TABLE `document_approval`
       ADD COLUMN `task_id` BIGINT DEFAULT NULL AFTER `doc_id`,
       ADD COLUMN `round_no` INT DEFAULT NULL AFTER `approver_id`,
       ADD COLUMN `step_order` INT DEFAULT NULL AFTER `round_no`,
       ADD COLUMN `step_name` VARCHAR(64) DEFAULT NULL AFTER `step_order`',
    'SELECT 1');
PREPARE office_stmt FROM @office_ddl;
EXECUTE office_stmt;
DEALLOCATE PREPARE office_stmt;

CREATE TABLE IF NOT EXISTS `document_approver` (
    `approver_config_id` BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       NOT NULL,
    `display_name`       VARCHAR(32)  NOT NULL,
    `status`             TINYINT      NOT NULL DEFAULT 1,
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_by`         BIGINT       DEFAULT NULL,
    `updated_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`approver_config_id`),
    UNIQUE KEY `uk_document_approver_user` (`user_id`),
    CONSTRAINT `fk_document_approver_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批资格配置表';

CREATE TABLE IF NOT EXISTS `document_workflow` (
    `workflow_id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `workflow_name` VARCHAR(64)  NOT NULL,
    `doc_type`      VARCHAR(16)  NOT NULL,
    `version`       INT          NOT NULL,
    `status`        TINYINT      NOT NULL DEFAULT 1,
    `created_by`    BIGINT       NOT NULL,
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`workflow_id`),
    UNIQUE KEY `uk_document_workflow_type_version` (`doc_type`, `version`),
    KEY `idx_document_workflow_active` (`doc_type`, `status`),
    CONSTRAINT `fk_document_workflow_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程版本表';

CREATE TABLE IF NOT EXISTS `document_workflow_step` (
    `step_id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `workflow_id` BIGINT       NOT NULL,
    `step_order`  INT          NOT NULL,
    `step_name`   VARCHAR(64)  NOT NULL,
    `approver_id` BIGINT       NOT NULL,
    PRIMARY KEY (`step_id`),
    UNIQUE KEY `uk_document_workflow_step_order` (`workflow_id`, `step_order`),
    KEY `idx_document_workflow_step_approver` (`approver_id`),
    CONSTRAINT `fk_document_workflow_step_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_workflow_step_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程步骤表';

CREATE TABLE IF NOT EXISTS `document_approval_task` (
    `task_id`      BIGINT       NOT NULL AUTO_INCREMENT,
    `doc_id`       BIGINT       NOT NULL,
    `workflow_id`  BIGINT       DEFAULT NULL,
    `round_no`     INT          NOT NULL DEFAULT 1,
    `step_order`   INT          NOT NULL,
    `step_name`    VARCHAR(64)  NOT NULL,
    `approver_id`  BIGINT       NOT NULL,
    `status`       TINYINT      NOT NULL DEFAULT 0,
    `handled_time` DATETIME     DEFAULT NULL,
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_document_task_round_step` (`doc_id`, `round_no`, `step_order`),
    KEY `idx_document_task_pending` (`approver_id`, `status`),
    CONSTRAINT `fk_document_task_document` FOREIGN KEY (`doc_id`) REFERENCES `document` (`doc_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_task_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`),
    CONSTRAINT `fk_document_task_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文逐级审批任务快照表';

INSERT INTO `role` (`role_code`, `role_name`, `status`) VALUES
    ('STUDENT', '学生', 1),
    ('TEACHER', '教师', 1),
    ('STAFF', '教职工', 1),
    ('COUNSELOR', '辅导员', 1),
    ('LEADER', '校领导', 1),
    ('ADMIN', '系统管理员', 1)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`), `status` = 1;

INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('office:read', '读取办公数据', 1),
    ('office:write', '维护办公数据', 1),
    ('fee:self:read', '查询本人账单与流水', 1),
    ('fee:self:pay', '支付本人账单', 1),
    ('fee:manage', '管理费用账单', 1),
    ('fee:overview:read', '查询学生缴费概览', 1),
    ('asset:read', '读取资产台账', 1),
    ('asset:apply', '提交资产申请', 1),
    ('asset:manage', '管理和审批资产', 1),
    ('work-plan:self', '维护本人工作计划', 1),
    ('work-plan:manage', '管理和点评工作计划', 1),
    ('document:self', '发起并查看本人公文', 1),
    ('document:approve', '审批流转至本人的公文', 1),
    ('document:manage', '管理公文审批资格与流程', 1),
    ('meeting:self', '查看并反馈本人会议', 1),
    ('meeting:manage', '发布和管理会议', 1),
    ('notification:self:read', '读取本人通知', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
CROSS JOIN `permission` p
WHERE r.role_code = 'ADMIN'
   OR (r.role_code = 'STUDENT' AND p.permission_code IN (
       'fee:self:read', 'fee:self:pay', 'work-plan:self', 'document:self',
       'notification:self:read'))
   OR (r.role_code = 'TEACHER' AND p.permission_code IN (
       'office:read', 'fee:overview:read', 'asset:read', 'asset:apply',
       'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
       'meeting:self', 'meeting:manage', 'notification:self:read'))
   OR (r.role_code = 'STAFF' AND p.permission_code IN (
       'office:read', 'office:write', 'fee:self:read', 'fee:self:pay', 'fee:manage',
       'fee:overview:read', 'asset:read', 'asset:apply', 'asset:manage',
       'work-plan:self', 'work-plan:manage', 'document:self', 'document:approve',
       'document:manage', 'meeting:self', 'meeting:manage', 'notification:self:read'))
   OR (r.role_code = 'LEADER' AND p.permission_code IN (
       'office:read', 'fee:overview:read'));

INSERT INTO `menu` (`title`, `path`, `permission_code`, `sort_order`, `status`) VALUES
    ('学杂费交纳', '/home/fee-payment', 'fee:self:read', 31, 1),
    ('固定资产管理', '/home/asset-management', 'asset:read', 32, 1),
    ('工作计划', '/home/work-plan', 'work-plan:self', 33, 1),
    ('公文流转 OA', '/home/document-oa', 'document:self', 34, 1),
    ('会议与通知', '/home/meeting-notice', 'notification:self:read', 35, 1)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`),
    `permission_code` = VALUES(`permission_code`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);
