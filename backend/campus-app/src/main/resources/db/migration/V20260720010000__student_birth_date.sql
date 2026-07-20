-- ============================================================
-- student_birth VARCHAR(16) → DATE 类型修正
-- 数据现状：全部为 yyyy-MM-dd 格式字符串，可直接转换
-- 影响：已有 DATE_ADD/DATE_SUB/TIMESTAMPDIFF 操作不再依赖隐式转换
-- ============================================================

ALTER TABLE student
    MODIFY COLUMN student_birth DATE DEFAULT NULL COMMENT '出生日期';
