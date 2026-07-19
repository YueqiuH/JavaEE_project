USE school_spring;

-- 教师或教职工可提交固定资产损坏/报废申请，由管理员审批并扣减对应台账库存。
ALTER TABLE `asset`
    ADD COLUMN `application_reason` VARCHAR(512) DEFAULT NULL COMMENT '申请人填写的损坏情况和报废原因' AFTER `source_asset_id`,
    MODIFY COLUMN `application_type` VARCHAR(16) DEFAULT NULL COMMENT '申请类型：PURCHASE=购买, ADD=添加, BORROW=借用, SCRAP=报废, LEGACY=历史；台账为空',
    MODIFY COLUMN `source_asset_id` BIGINT DEFAULT NULL COMMENT '借用或报废申请对应的来源资产ID';
