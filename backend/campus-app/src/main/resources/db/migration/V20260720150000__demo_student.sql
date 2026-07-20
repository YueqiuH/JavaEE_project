-- 演示学生 600001 档案
INSERT INTO student (student_no, student_name, gender, class_name, dept_id, major_id, enroll_year, origin_place, status, student_birth, student_age, student_address)
VALUES (600001, '演示学生', 1, '软件2501', 1, 30, 2025, '北京', 1, '2007-06-15', 19, '北京市海淀区')
ON DUPLICATE KEY UPDATE student_name = '演示学生', class_name = '软件2501', dept_id = 1, major_id = 30, status = 1;
