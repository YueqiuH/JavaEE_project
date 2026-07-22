-- ============================================================
-- 教学数据重建V2：使用 course_code 引用课程，避免硬编码ID
-- ============================================================

-- ==================== 0. 修复用户类型 ====================
UPDATE user SET user_type = 2, real_name = '演示辅导员' WHERE user_id = 2;
UPDATE user SET user_type = 3, real_name = '演示教师'   WHERE user_id = 3;

-- ==================== 1. 清除旧教学数据 ====================
DELETE FROM exam_room        WHERE exam_id IN (SELECT exam_id FROM exam WHERE semester IN ('2025-2026-1','2025-2026-2'));
DELETE FROM invigilation     WHERE exam_id IN (SELECT exam_id FROM exam WHERE semester IN ('2025-2026-1','2025-2026-2'));
DELETE FROM exam             WHERE semester IN ('2025-2026-1','2025-2026-2');
DELETE FROM score            WHERE semester IN ('2025-2026-1','2025-2026-2');
DELETE FROM course_selection WHERE semester IN ('2025-2026-1','2025-2026-2');
DELETE FROM schedule         WHERE semester IN ('2025-2026-1','2025-2026-2');
DELETE FROM course_capacity  WHERE semester IN ('2025-2026-1','2025-2026-2');
DELETE FROM course;

-- ==================== 2. 清理旧学生(保留演示学生id=1) ====================
DELETE FROM student WHERE student_id NOT IN (1);

-- ==================== 3. 上学期8门课程(2025-2026-1) ====================
INSERT INTO course (course_name, course_code, classification, credit, weekly_frequency, is_active) VALUES
('高等数学A(一)',  'MATH101','必修',5,4,1),
('线性代数',       'MATH201','必修',3,3,1),
('程序设计基础',   'CS101',  '必修',4,3,1),
('大学物理',       'PHYS101','必修',4,3,1),
('离散数学',       'MATH301','必修',3,2,1),
('数字逻辑',       'EE201',  '必修',3,2,1),
('数据结构',       'CS201',  '必修',4,3,1),
('计算机组成原理', 'CS202',  '必修',4,3,1);

-- 容量
INSERT INTO course_capacity (course_id, semester, max_capacity, current_count)
SELECT course_id, '2025-2026-1', 60, 0 FROM course WHERE course_code IN ('MATH101','MATH201','CS101','PHYS101','MATH301','EE201','CS201','CS202');

-- 排课: 800001(教师)教 MATH101 + MATH201
INSERT INTO schedule (course_id, classroom_id, teacher_id, semester, week_day, start_period, end_period, start_week, end_week, schedule_type, week_pattern)
SELECT course_id, 41, 3, '2025-2026-1', 1, 1, 2, 1, 16, '正常', 'every' FROM course WHERE course_code='MATH101';

INSERT INTO schedule (course_id, classroom_id, teacher_id, semester, week_day, start_period, end_period, start_week, end_week, schedule_type, week_pattern) VALUES
((SELECT course_id FROM course WHERE course_code='MATH101'),41,3,'2025-2026-1',3,1,2,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='MATH201'),42,3,'2025-2026-1',2,1,2,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='MATH201'),42,3,'2025-2026-1',4,3,4,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='CS101'),43,19,'2025-2026-1',2,3,4,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='CS101'),43,19,'2025-2026-1',5,1,2,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='PHYS101'),44,20,'2025-2026-1',3,3,4,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='PHYS101'),44,20,'2025-2026-1',5,3,4,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='MATH301'),45,21,'2025-2026-1',1,3,4,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='EE201'),46,22,'2025-2026-1',4,1,2,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='CS201'),47,23,'2025-2026-1',1,5,6,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='CS201'),47,23,'2025-2026-1',3,5,6,1,16,'正常','every'),
((SELECT course_id FROM course WHERE course_code='CS202'),48,24,'2025-2026-1',2,5,6,1,16,'正常','every');

-- ==================== 4. 创建10名学生 ====================
UPDATE student SET class_name='软件1班', dept_id=1, major_id=30, grade_id=5, enroll_year=2025, status=1, counselor_id=2 WHERE student_id=1;

INSERT INTO student (student_id, student_name, student_no, class_name, dept_id, major_id, grade_id, enroll_year, status, counselor_id, gender) VALUES
(2,'赵一明','202501001','软件1班',1,30,5,2025,1,2,1),
(3,'钱二亮','202501002','软件1班',1,30,5,2025,1,2,1),
(4,'孙三杰','202501003','软件1班',1,30,5,2025,1,2,1),
(5,'李四维','202501004','软件1班',1,30,5,2025,1,2,2),
(6,'周五峰','202501005','软件1班',1,30,5,2025,1,2,1),
(7,'吴六合','202502001','软件2班',1,30,5,2025,1,NULL,1),
(8,'郑七星','202502002','软件2班',1,30,5,2025,1,NULL,2),
(9,'王八方','202502003','软件2班',1,30,5,2025,1,NULL,1),
(10,'冯九思','202502004','软件2班',1,30,5,2025,1,NULL,2),
(11,'陈十全','202502005','软件2班',1,30,5,2025,1,NULL,1);

-- ==================== 5. 上学期选课 ====================
INSERT INTO course_selection (student_id, course_id, semester, status)
SELECT s.student_id, c.course_id, '2025-2026-1', 1
FROM student s CROSS JOIN course c
WHERE s.student_id BETWEEN 1 AND 11
  AND c.course_code IN ('MATH101','MATH201','CS101','PHYS101','MATH301','EE201','CS201','CS202');

-- ==================== 6. 上学期成绩 ====================
-- 1班(学生1-6):
INSERT INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, publish_status, teacher_id)
SELECT s.student_id, c.course_id,
  CASE WHEN c.course_code='MATH101' THEN ELT(s.student_id,85,78,66,45,93,74)
       WHEN c.course_code='MATH201' THEN ELT(s.student_id,72,68,75,52,88,61)
       WHEN c.course_code='CS101'   THEN ELT(s.student_id,90,92,80,88,76,50)
       WHEN c.course_code='PHYS101' THEN ELT(s.student_id,78,81,62,72,84,86)
       WHEN c.course_code='MATH301' THEN ELT(s.student_id,65,55,73,60,70,77)
       WHEN c.course_code='EE201'   THEN ELT(s.student_id,58,71,84,67,59,68)
       WHEN c.course_code='CS201'   THEN ELT(s.student_id,88,85,69,55,95,83)
       ELSE                             ELT(s.student_id,82,76,91,78,82,71)
  END,
  '2025-2026-1', 0.0, 1,
  FLOOR(40+RAND()*50), FLOOR(40+RAND()*50), 0.30, 0.70, 1,
  CASE WHEN c.course_code IN ('MATH101','MATH201') THEN 3
       WHEN c.course_code='CS101' THEN 19 WHEN c.course_code='PHYS101' THEN 20
       WHEN c.course_code='MATH301' THEN 21 WHEN c.course_code='EE201' THEN 22
       WHEN c.course_code='CS201' THEN 23 ELSE 24 END
FROM student s CROSS JOIN course c
WHERE s.student_id BETWEEN 1 AND 6 AND c.course_code IN ('MATH101','MATH201','CS101','PHYS101','MATH301','EE201','CS201','CS202');

-- 2班(学生7-11):
INSERT INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, publish_status, teacher_id)
SELECT s.student_id, c.course_id,
  CASE WHEN c.course_code='MATH101' THEN ELT(s.student_id-6,80,42,95,86,68)
       WHEN c.course_code='MATH201' THEN ELT(s.student_id-6,75,68,82,74,80)
       WHEN c.course_code='CS101'   THEN ELT(s.student_id-6,88,76,73,91,54)
       WHEN c.course_code='PHYS101' THEN ELT(s.student_id-6,70,58,88,64,77)
       WHEN c.course_code='MATH301' THEN ELT(s.student_id-6,65,90,60,79,83)
       WHEN c.course_code='EE201'   THEN ELT(s.student_id-6,55,73,56,85,70)
       WHEN c.course_code='CS201'   THEN ELT(s.student_id-6,82,66,78,72,59)
       ELSE                             ELT(s.student_id-6,90,81,69,57,85)
  END,
  '2025-2026-1', 0.0, 1,
  FLOOR(40+RAND()*50), FLOOR(40+RAND()*50), 0.30, 0.70, 1,
  CASE WHEN c.course_code IN ('MATH101','MATH201') THEN 3
       WHEN c.course_code='CS101' THEN 19 WHEN c.course_code='PHYS101' THEN 20
       WHEN c.course_code='MATH301' THEN 21 WHEN c.course_code='EE201' THEN 22
       WHEN c.course_code='CS201' THEN 23 ELSE 24 END
FROM student s CROSS JOIN course c
WHERE s.student_id BETWEEN 7 AND 11 AND c.course_code IN ('MATH101','MATH201','CS101','PHYS101','MATH301','EE201','CS201','CS202');

-- 更新GPA
UPDATE score SET gpa = CASE
  WHEN score_score>=90 THEN 4.0 WHEN score_score>=85 THEN 3.7
  WHEN score_score>=80 THEN 3.3 WHEN score_score>=75 THEN 3.0
  WHEN score_score>=70 THEN 2.7 WHEN score_score>=65 THEN 2.3
  WHEN score_score>=60 THEN 2.0 ELSE 0.0
END WHERE semester='2025-2026-1';

-- 更新结果状态
UPDATE score SET status = CASE WHEN score_score>=60 THEN 1 ELSE 0 END WHERE semester='2025-2026-1';

-- ==================== 7. 本学期10门新课(2025-2026-2) ====================
INSERT INTO course (course_name, course_code, classification, credit, weekly_frequency, is_active) VALUES
('高等数学A(二)',      'MATH102','必修',5,4,1),
('概率论与数理统计',   'MATH302','必修',3,3,1),
('操作系统',           'CS301',  '必修',4,3,1),
('计算机网络',         'CS302',  '必修',3,3,1),
('数据库原理',         'CS303',  '必修',3,3,1),
('软件工程',           'SE201',  '必修',3,2,1),
('编译原理',           'CS401',  '必修',3,3,1),
('人工智能导论',       'AI201',  '必修',3,2,1),
('Java程序设计',       'CS304',  '必修',4,3,1),
('Web前端开发',        'CS305',  '必修',3,2,1);

INSERT INTO course_capacity (course_id, semester, max_capacity, current_count)
SELECT course_id, '2025-2026-2', 60, 0 FROM course WHERE course_code IN ('MATH102','MATH302','CS301','CS302','CS303','SE201','CS401','AI201','CS304','CS305');

-- ==================== 8. 验收 ====================
SELECT '=== 数据验收 ===' AS '';
SELECT '上学期课程' AS label, COUNT(*) AS cnt FROM course_capacity WHERE semester='2025-2026-1'
UNION ALL SELECT '下学期课程', COUNT(*) FROM course_capacity WHERE semester='2025-2026-2'
UNION ALL SELECT '1班学生', COUNT(*) FROM student WHERE class_name='软件1班'
UNION ALL SELECT '2班学生', COUNT(*) FROM student WHERE class_name='软件2班'
UNION ALL SELECT '上学期成绩', COUNT(*) FROM score WHERE semester='2025-2026-1'
UNION ALL SELECT '成绩55-59分', COUNT(*) FROM score WHERE score_score>=55 AND score_score<60
UNION ALL SELECT '成绩<55分', COUNT(*) FROM score WHERE score_score<55
UNION ALL SELECT '=== 完成 ===', 0;

SELECT '教师800001' AS role, real_name FROM user WHERE user_id=3
UNION ALL SELECT '辅导员700001', real_name FROM user WHERE user_id=2;
