-- ============================================
-- 成员 D：基础档案扩展字段（对已建库执行一次）
-- 影响说明：
--   user/student 为共享表，本变更为“新增可空列”，不修改既有列与约束，
--   不影响 A/B/C 已有读写；dept_code/major_code/enrollment(major_id,year)
--   增加唯一键用于种子数据幂等与业务去重。
-- ============================================

USE school_spring;

-- 教职工档案字段（D1 教职工信息库）
ALTER TABLE `user`
    ADD COLUMN `real_name` VARCHAR(32) DEFAULT NULL COMMENT '姓名'   AFTER `user_type`,
    ADD COLUMN `gender`    TINYINT     DEFAULT NULL COMMENT '性别: 1=男, 2=女' AFTER `real_name`,
    ADD COLUMN `phone`     VARCHAR(20) DEFAULT NULL COMMENT '联系电话' AFTER `gender`,
    ADD COLUMN `email`     VARCHAR(64) DEFAULT NULL COMMENT '邮箱'   AFTER `phone`,
    ADD COLUMN `title`     VARCHAR(32) DEFAULT NULL COMMENT '职称'   AFTER `email`,
    ADD COLUMN `position`  VARCHAR(32) DEFAULT NULL COMMENT '职务'   AFTER `title`,
    ADD COLUMN `dept_id`   BIGINT      DEFAULT NULL COMMENT '所属院系ID' AFTER `position`;

-- 学生档案字段（D1 学生信息库 / D2 生源地分布 / D3 多维穿透统计）
ALTER TABLE `student`
    ADD COLUMN `gender`       TINYINT     DEFAULT NULL COMMENT '性别: 1=男, 2=女' AFTER `student_age`,
    ADD COLUMN `dept_id`      BIGINT      DEFAULT NULL COMMENT '所属院系ID'  AFTER `gender`,
    ADD COLUMN `major_id`     BIGINT      DEFAULT NULL COMMENT '所属专业ID'  AFTER `dept_id`,
    ADD COLUMN `class_name`   VARCHAR(32) DEFAULT NULL COMMENT '班级'        AFTER `major_id`,
    ADD COLUMN `origin_place` VARCHAR(32) DEFAULT NULL COMMENT '生源地(省份)' AFTER `class_name`,
    ADD COLUMN `enroll_year`  INT         DEFAULT NULL COMMENT '入学年份'    AFTER `origin_place`,
    ADD COLUMN `status`       TINYINT     NOT NULL DEFAULT 1 COMMENT '学籍状态: 1=在读, 2=休学, 3=毕业, 0=退学' AFTER `enroll_year`,
    ADD UNIQUE KEY `uk_student_no` (`student_no`),
    ADD KEY `idx_student_dept_major` (`dept_id`, `major_id`);

-- D 自有表唯一键
ALTER TABLE `department` ADD UNIQUE KEY `uk_dept_code` (`dept_code`);
ALTER TABLE `major`      ADD UNIQUE KEY `uk_major_code` (`major_code`);
ALTER TABLE `enrollment` ADD UNIQUE KEY `uk_enrollment_major_year` (`major_id`, `year`);
