USE school_spring;

-- 资产模块采用单部门模式，保留 dept_id 字段用于兼容既有结构并统一固定为 1。
UPDATE `asset`
SET `dept_id` = 1
WHERE `dept_id` IS NULL OR `dept_id` <> 1;

ALTER TABLE `asset`
    MODIFY COLUMN `dept_id` BIGINT NOT NULL DEFAULT 1 COMMENT '唯一部门ID（固定为1）';
