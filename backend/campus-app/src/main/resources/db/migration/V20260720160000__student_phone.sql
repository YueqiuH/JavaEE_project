ALTER TABLE `student` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '联系电话' AFTER `student_address`;
UPDATE `student` SET `phone` = '12345678900' WHERE `student_no` = 600001;
