USE school_spring;

-- 固定资产拆分为购买、添加入库、借用三类申请，并完整保存管理员审批审计信息。
ALTER TABLE `asset`
    ADD COLUMN `application_type` VARCHAR(16) DEFAULT NULL COMMENT '申请类型：PURCHASE=购买, ADD=添加入库, BORROW=借用, LEGACY=历史；台账为空' AFTER `apply_user_id`,
    ADD COLUMN `source_asset_id` BIGINT DEFAULT NULL COMMENT '借用申请对应的来源资产ID' AFTER `application_type`,
    ADD COLUMN `approve_user_id` BIGINT DEFAULT NULL COMMENT '管理员审批人ID' AFTER `approve_status`,
    ADD COLUMN `approve_remark` VARCHAR(256) DEFAULT NULL COMMENT '管理员审批意见' AFTER `approve_user_id`,
    ADD COLUMN `approve_time` DATETIME DEFAULT NULL COMMENT '管理员审批时间' AFTER `approve_remark`,
    ADD KEY `idx_asset_application` (`application_type`, `approve_status`, `create_time`),
    ADD KEY `idx_asset_source` (`source_asset_id`);

ALTER TABLE `asset`
    MODIFY COLUMN `user_id` BIGINT DEFAULT NULL COMMENT '借用人ID',
    MODIFY COLUMN `status` INT DEFAULT 1 COMMENT '状态：1=在库, 2=已借出, 3=报废';

-- 旧版未区分已完成的购置和领用，避免错误归类；仅仍待审批且有来源痕迹的记录按借用兼容。
UPDATE `asset`
SET `source_asset_id` = CASE
        WHEN `apply_user_id` IS NOT NULL AND `approve_status` = 0 AND `user_id` IS NOT NULL THEN `user_id`
        ELSE NULL
    END,
    `application_type` = CASE
        WHEN `apply_user_id` IS NULL THEN NULL
        WHEN `approve_status` = 0 AND `user_id` IS NOT NULL THEN 'BORROW'
        WHEN `approve_status` = 0 THEN 'PURCHASE'
        ELSE 'LEGACY'
    END;

-- 旧台账记录视为已入库资产；申请记录继续保留原审批状态。
UPDATE `asset`
SET `approve_status` = 1
WHERE `apply_user_id` IS NULL;

UPDATE `permission`
SET `permission_name` = '管理员管理和审批资产', `status` = 1
WHERE `permission_code` = 'asset:manage';

-- 审批和资产管理只授予 ADMIN；教师、教职工仍可提交资产申请。
DELETE rp
FROM `role_permission` rp
JOIN `role` r ON r.`role_id` = rp.`role_id`
JOIN `permission` p ON p.`permission_id` = rp.`permission_id`
WHERE p.`permission_code` = 'asset:manage'
  AND r.`role_code` <> 'ADMIN';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.`role_id`, p.`permission_id`
FROM `role` r
JOIN `permission` p ON p.`permission_code` = 'asset:manage'
WHERE r.`role_code` = 'ADMIN';
