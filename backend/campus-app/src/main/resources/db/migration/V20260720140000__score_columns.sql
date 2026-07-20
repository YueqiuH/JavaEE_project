ALTER TABLE `score`
    ADD COLUMN `regular_score` DECIMAL(5,1) NULL COMMENT '平时成绩',
    ADD COLUMN `exam_score`    DECIMAL(5,1) NULL COMMENT '期末成绩',
    ADD COLUMN `regular_ratio` DECIMAL(3,2) NULL DEFAULT 0.30 COMMENT '平时占比',
    ADD COLUMN `exam_ratio`    DECIMAL(3,2) NULL DEFAULT 0.70 COMMENT '期末占比',
    ADD COLUMN `schedule_id`   BIGINT NULL COMMENT '排课ID',
    ADD COLUMN `teacher_id`    BIGINT NULL COMMENT '录入教师ID',
    ADD COLUMN `publish_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待录入 1=草稿 2=已发布';
