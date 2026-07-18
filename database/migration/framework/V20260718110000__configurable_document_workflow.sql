USE school_spring;

INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
    ('document:manage', '管理公文审批资格与流程', 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
JOIN `permission` p ON p.permission_code = 'document:manage'
WHERE r.role_code = 'ADMIN';

ALTER TABLE `document_approver`
    ADD COLUMN `updated_by` BIGINT DEFAULT NULL COMMENT '最后操作管理员ID' AFTER `create_time`,
    ADD COLUMN `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间' AFTER `updated_by`;

CREATE TABLE IF NOT EXISTS `document_workflow` (
    `workflow_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流程版本主键ID',
    `workflow_name` VARCHAR(64)  NOT NULL                COMMENT '流程名称',
    `doc_type`      VARCHAR(16)  NOT NULL                COMMENT '公文类型',
    `version`       INT          NOT NULL                COMMENT '同类型流程版本号',
    `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=当前启用, 0=历史版本',
    `created_by`    BIGINT       NOT NULL                COMMENT '配置管理员ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`workflow_id`),
    UNIQUE KEY `uk_document_workflow_type_version` (`doc_type`, `version`),
    KEY `idx_document_workflow_active` (`doc_type`, `status`),
    CONSTRAINT `fk_document_workflow_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程版本表';

CREATE TABLE IF NOT EXISTS `document_workflow_step` (
    `step_id`     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '流程步骤主键ID',
    `workflow_id` BIGINT       NOT NULL                COMMENT '流程版本ID',
    `step_order`  INT          NOT NULL                COMMENT '步骤顺序，从1开始',
    `step_name`   VARCHAR(64)  NOT NULL                COMMENT '步骤名称',
    `approver_id` BIGINT       NOT NULL                COMMENT '管理员固定指定的审批人ID',
    PRIMARY KEY (`step_id`),
    UNIQUE KEY `uk_document_workflow_step_order` (`workflow_id`, `step_order`),
    KEY `idx_document_workflow_step_approver` (`approver_id`),
    CONSTRAINT `fk_document_workflow_step_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_workflow_step_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文审批流程步骤表';

ALTER TABLE `document`
    ADD COLUMN `workflow_id` BIGINT DEFAULT NULL COMMENT '启动时采用的流程版本ID' AFTER `approval_chain`,
    ADD COLUMN `current_step` INT DEFAULT NULL COMMENT '当前审批步骤' AFTER `workflow_id`,
    ADD COLUMN `approval_round` INT NOT NULL DEFAULT 1 COMMENT '审批轮次' AFTER `current_step`,
    ADD KEY `idx_document_workflow` (`workflow_id`);

CREATE TABLE IF NOT EXISTS `document_approval_task` (
    `task_id`      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审批任务主键ID',
    `doc_id`       BIGINT       NOT NULL                COMMENT '公文ID',
    `workflow_id`  BIGINT       DEFAULT NULL            COMMENT '流程版本ID，旧公文可为空',
    `round_no`     INT          NOT NULL DEFAULT 1      COMMENT '审批轮次',
    `step_order`   INT          NOT NULL                COMMENT '步骤顺序',
    `step_name`    VARCHAR(64)  NOT NULL                COMMENT '步骤名称快照',
    `approver_id`  BIGINT       NOT NULL                COMMENT '审批人快照',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '0=等待,1=待审批,2=同意,3=拒绝,4=退回,5=取消',
    `handled_time` DATETIME     DEFAULT NULL            COMMENT '处理时间',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`task_id`),
    UNIQUE KEY `uk_document_task_round_step` (`doc_id`, `round_no`, `step_order`),
    KEY `idx_document_task_pending` (`approver_id`, `status`),
    CONSTRAINT `fk_document_task_document` FOREIGN KEY (`doc_id`) REFERENCES `document` (`doc_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_document_task_workflow` FOREIGN KEY (`workflow_id`) REFERENCES `document_workflow` (`workflow_id`),
    CONSTRAINT `fk_document_task_approver` FOREIGN KEY (`approver_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文逐级审批任务快照表';

ALTER TABLE `document_approval`
    ADD COLUMN `task_id` BIGINT DEFAULT NULL COMMENT '对应审批任务ID' AFTER `doc_id`,
    ADD COLUMN `round_no` INT DEFAULT NULL COMMENT '审批轮次' AFTER `approver_id`,
    ADD COLUMN `step_order` INT DEFAULT NULL COMMENT '审批步骤' AFTER `round_no`,
    ADD COLUMN `step_name` VARCHAR(64) DEFAULT NULL COMMENT '步骤名称快照' AFTER `step_order`,
    ADD KEY `idx_document_approval_task` (`task_id`);

INSERT IGNORE INTO `document_approval_task`
    (`doc_id`, `workflow_id`, `round_no`, `step_order`, `step_name`, `approver_id`, `status`, `create_time`)
SELECT d.doc_id, NULL, 1, 1, '原单步审批', d.current_approver_id, 1, d.create_time
FROM `document` d
WHERE d.status = 0 AND d.current_approver_id IS NOT NULL;

UPDATE `document`
SET `current_step` = 1, `approval_round` = 1
WHERE `status` = 0 AND `current_approver_id` IS NOT NULL AND `current_step` IS NULL;
