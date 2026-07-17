USE school_spring;

CREATE TABLE IF NOT EXISTS `document_approver` (
    `approver_config_id` BIGINT       NOT NULL AUTO_INCREMENT COMMENT '审批人配置主键ID',
    `user_id`            BIGINT       NOT NULL                COMMENT '审批用户ID',
    `display_name`       VARCHAR(32)  NOT NULL                COMMENT '审批人显示名称',
    `status`             TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1=启用, 0=停用',
    `create_time`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`approver_config_id`),
    UNIQUE KEY `uk_document_approver_user` (`user_id`),
    CONSTRAINT `fk_document_approver_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文指定审批人表';

INSERT INTO `document_approver` (`user_id`, `display_name`, `status`)
SELECT u.user_id,
       CASE u.username WHEN '700001' THEN '教学负责人' ELSE '行政负责人' END,
       1
FROM `user` u
WHERE u.username IN ('700001', '800001')
ON DUPLICATE KEY UPDATE `display_name` = VALUES(`display_name`), `status` = 1;
