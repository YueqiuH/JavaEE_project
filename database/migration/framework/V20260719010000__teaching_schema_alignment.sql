-- Align the existing teaching schema with the Java entities and mapper queries.

ALTER TABLE `schedule`
    ADD COLUMN `parent_id` BIGINT DEFAULT NULL
        COMMENT 'Split schedule parent ID' AFTER `schedule_type`,
    ADD COLUMN `week_pattern` VARCHAR(16) NOT NULL DEFAULT 'every'
        COMMENT 'Week pattern: every/odd/even' AFTER `parent_id`,
    ADD COLUMN `target_grade_id` BIGINT DEFAULT NULL
        COMMENT 'Target grade for elective courses' AFTER `week_pattern`,
    ADD KEY `idx_schedule_parent` (`parent_id`),
    ADD KEY `idx_schedule_target_grade` (`target_grade_id`);

ALTER TABLE `course_capacity`
    ADD COLUMN `schedule_id` BIGINT DEFAULT NULL
        COMMENT 'Teaching schedule ID' AFTER `min_capacity`,
    ADD KEY `idx_course_capacity_schedule` (`schedule_id`);

ALTER TABLE `exam_room`
    MODIFY COLUMN `seat_no` VARCHAR(16) DEFAULT NULL COMMENT 'Seat number';

-- ScoreService treats this tuple as one logical record. Keep the oldest row
-- before adding the database-level uniqueness guarantee.
DELETE duplicate_score
FROM `score` duplicate_score
JOIN `score` retained_score
  ON retained_score.student_id = duplicate_score.student_id
 AND retained_score.course_id = duplicate_score.course_id
 AND retained_score.semester <=> duplicate_score.semester
 AND retained_score.score_id < duplicate_score.score_id;

ALTER TABLE `score`
    ADD UNIQUE KEY `uk_score_student_course_sem`
        (`student_id`, `course_id`, `semester`);
