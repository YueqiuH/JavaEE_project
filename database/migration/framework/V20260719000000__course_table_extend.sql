-- ============================================
-- 补全 course 表字段，与 CourseEntity 对齐
-- ============================================
ALTER TABLE `course` ADD COLUMN `course_code`      VARCHAR(16)  DEFAULT NULL            COMMENT '课程编号'                AFTER `course_name`;
ALTER TABLE `course` ADD COLUMN `classification`   VARCHAR(8)   DEFAULT NULL            COMMENT '必修/选修/限选'           AFTER `course_code`;
ALTER TABLE `course` ADD COLUMN `credit`           DECIMAL(3,1) DEFAULT NULL            COMMENT '学分 1-5'                AFTER `classification`;
ALTER TABLE `course` ADD COLUMN `weekly_frequency` INT          DEFAULT NULL            COMMENT '每周上课次数：1或2'       AFTER `credit`;
ALTER TABLE `course` ADD COLUMN `prerequisite_id`  BIGINT       DEFAULT NULL            COMMENT '先修课程ID，自关联'       AFTER `weekly_frequency`;
ALTER TABLE `course` ADD COLUMN `is_active`        TINYINT      NOT NULL DEFAULT 1      COMMENT '是否开课: 1=正常, 0=停开' AFTER `prerequisite_id`;
ALTER TABLE `course` ADD COLUMN `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP                             AFTER `is_active`;
