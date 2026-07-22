-- ============================================================
-- 教务教学全量测试数据
-- ============================================================

-- ==================== 1. 教室(20间) ====================
INSERT INTO classroom (classroom_name, building, capacity, type, status) VALUES
('A101', '教学楼A', 120, '普通教室', 1),
('A102', '教学楼A', 120, '普通教室', 1),
('A103', '教学楼A', 80, '普通教室', 1),
('A201', '教学楼A', 60, '多媒体教室', 1),
('A202', '教学楼A', 60, '多媒体教室', 1),
('B101', '教学楼B', 150, '阶梯教室', 1),
('B102', '教学楼B', 150, '阶梯教室', 1),
('B201', '教学楼B', 80, '多媒体教室', 1),
('B202', '教学楼B', 80, '多媒体教室', 1),
('C101', '教学楼C', 100, '普通教室', 1),
('C102', '教学楼C', 100, '普通教室', 1),
('C201', '教学楼C', 60, '多媒体教室', 1),
('D101', '实验楼D', 50, '实验室', 1),
('D102', '实验楼D', 50, '实验室', 1),
('D103', '实验楼D', 50, '实验室', 1),
('D201', '实验楼D', 40, '机房', 1),
('D202', '实验楼D', 40, '机房', 1),
('E101', '综合楼E', 200, '报告厅', 1),
('E102', '综合楼E', 120, '多媒体教室', 1),
('E103', '综合楼E', 120, '多媒体教室', 1);

-- ==================== 2. 课程(15门) ====================
INSERT INTO course (course_name, course_code, classification, credit, weekly_frequency, is_active) VALUES
('高等数学A(一)', 'MATH101', '必修', 5.0, 4, 1),
('线性代数', 'MATH201', '必修', 3.0, 3, 1),
('大学物理B', 'PHYS101', '必修', 4.0, 3, 1),
('程序设计基础(C语言)', 'CS101', '必修', 4.0, 3, 1),
('数据结构与算法', 'CS201', '必修', 4.0, 3, 1),
('计算机网络', 'CS301', '必修', 3.5, 3, 1),
('数据库原理', 'CS302', '必修', 3.0, 3, 1),
('操作系统', 'CS401', '必修', 4.0, 3, 1),
('大学英语(三)', 'ENG301', '必修', 3.0, 2, 1),
('马克思主义基本原理', 'POLI201', '必修', 3.0, 2, 1),
('电路分析基础', 'EE101', '必修', 3.5, 3, 1),
('微观经济学', 'ECON101', '必修', 3.0, 2, 1),
('管理学原理', 'MGMT201', '选修', 2.0, 2, 1),
('机械制图', 'ME101', '必修', 3.0, 3, 1),
('体育(三)', 'PE301', '必修', 1.0, 1, 1);

-- ==================== 3. 课程容量 ====================
INSERT INTO course_capacity (course_id, semester, max_capacity, current_count) VALUES
(1,'2025-2026-1',200,0),(1,'2025-2026-2',200,0),
(2,'2025-2026-1',180,0),(2,'2025-2026-2',180,0),
(3,'2025-2026-1',150,0),(3,'2025-2026-2',150,0),
(4,'2025-2026-1',120,0),(4,'2025-2026-2',120,0),
(5,'2025-2026-1',120,0),(5,'2025-2026-2',120,0),
(6,'2025-2026-1',100,0),(6,'2025-2026-2',100,0),
(7,'2025-2026-1',100,0),(7,'2025-2026-2',100,0),
(8,'2025-2026-1',100,0),(8,'2025-2026-2',100,0),
(9,'2025-2026-1',200,0),(9,'2025-2026-2',200,0),
(10,'2025-2026-1',250,0),(10,'2025-2026-2',250,0),
(11,'2025-2026-1',150,0),(11,'2025-2026-2',150,0),
(12,'2025-2026-1',150,0),(12,'2025-2026-2',150,0),
(13,'2025-2026-1',100,0),(13,'2025-2026-2',100,0),
(14,'2025-2026-1',120,0),(14,'2025-2026-2',120,0),
(15,'2025-2026-1',300,0),(15,'2025-2026-2',300,0);

-- ==================== 4. 排课 ====================
-- 获取教师ID列表
SET @t1 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=1);
SET @t2 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=1 ORDER BY user_id LIMIT 1 OFFSET 1);
SET @t3 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=1 ORDER BY user_id LIMIT 1 OFFSET 2);
SET @t4 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=1 ORDER BY user_id LIMIT 1 OFFSET 3);
SET @t5 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=2 ORDER BY user_id LIMIT 1);
SET @t6 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=3 ORDER BY user_id LIMIT 1);
SET @t7 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=4 ORDER BY user_id LIMIT 1);
SET @t8 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=5 ORDER BY user_id LIMIT 1);
SET @t9 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=6 ORDER BY user_id LIMIT 1);
SET @t10 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=7 ORDER BY user_id LIMIT 1);
SET @t11 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=8 ORDER BY user_id LIMIT 1);
SET @t12 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=9 ORDER BY user_id LIMIT 1);
SET @t13 = (SELECT user_id FROM user WHERE user_type=2 AND dept_id=10 ORDER BY user_id LIMIT 1);
SET @t14 = (SELECT user_id FROM user WHERE user_type=2 ORDER BY user_id LIMIT 1 OFFSET 20);
SET @t15 = (SELECT user_id FROM user WHERE user_type=2 ORDER BY user_id LIMIT 1 OFFSET 25);

INSERT INTO schedule (course_id, classroom_id, teacher_id, semester, week_day, start_period, end_period, start_week, end_week, schedule_type, week_pattern) VALUES
(1, 1, @t1, '2025-2026-1', 1, 1, 2, 1, 16, 'manual', 'every'),
(1, 1, @t1, '2025-2026-1', 3, 1, 2, 1, 16, 'manual', 'every'),
(2, 2, @t2, '2025-2026-1', 2, 1, 2, 1, 16, 'manual', 'every'),
(2, 2, @t2, '2025-2026-1', 4, 3, 4, 1, 16, 'manual', 'every'),
(3, 3, @t3, '2025-2026-1', 1, 3, 4, 1, 16, 'manual', 'every'),
(3, 3, @t3, '2025-2026-1', 3, 3, 4, 1, 16, 'manual', 'every'),
(4, 4, @t4, '2025-2026-1', 2, 3, 4, 1, 16, 'manual', 'every'),
(4, 4, @t4, '2025-2026-1', 5, 1, 2, 1, 16, 'manual', 'every'),
(5, 5, @t5, '2025-2026-1', 1, 5, 6, 1, 16, 'manual', 'every'),
(5, 5, @t5, '2025-2026-1', 3, 5, 6, 1, 16, 'manual', 'every'),
(6, 6, @t6, '2025-2026-1', 2, 1, 2, 1, 16, 'manual', 'every'),
(6, 6, @t6, '2025-2026-1', 4, 1, 2, 1, 16, 'manual', 'every'),
(7, 7, @t7, '2025-2026-1', 1, 3, 4, 1, 16, 'manual', 'every'),
(7, 7, @t7, '2025-2026-1', 5, 3, 4, 1, 16, 'manual', 'every'),
(8, 8, @t8, '2025-2026-1', 2, 5, 6, 1, 16, 'manual', 'every'),
(8, 8, @t8, '2025-2026-1', 4, 5, 6, 1, 16, 'manual', 'every'),
(9, 10, @t9, '2025-2026-1', 3, 1, 2, 1, 16, 'manual', 'every'),
(10, 12, @t10, '2025-2026-1', 4, 3, 4, 1, 16, 'manual', 'every'),
(11, 13, @t11, '2025-2026-1', 5, 1, 2, 1, 16, 'manual', 'every'),
(12, 15, @t12, '2025-2026-1', 2, 3, 4, 1, 16, 'manual', 'every'),
(13, 16, @t13, '2025-2026-1', 4, 5, 6, 1, 16, 'manual', 'every'),
(14, 17, @t14, '2025-2026-1', 1, 1, 2, 1, 16, 'manual', 'every'),
(15, 19, @t15, '2025-2026-1', 5, 3, 4, 1, 16, 'manual', 'every');

-- ==================== 5. 选课 ====================
INSERT INTO course_selection (student_id, course_id, semester, status)
SELECT s.student_id, c.course_id, '2025-2026-1', 1
FROM student s
CROSS JOIN course c
WHERE s.student_id BETWEEN 1 AND 800
  AND s.status = 1
  AND MOD(s.student_id + c.course_id, 5) = 0;

-- ==================== 6. 成绩 ====================
INSERT INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, schedule_id, teacher_id, publish_status)
SELECT cs.student_id, cs.course_id,
       40 + MOD(cs.student_id * 7 + cs.course_id * 13, 60) AS score_score,
       '2025-2026-1', 0.0 AS gpa, 1,
       10.0 + MOD(cs.student_id * 3, 90) AS regular_score,
       30.0 + MOD(cs.student_id * 5 + cs.course_id, 70) AS exam_score,
       0.30, 0.70,
       (SELECT MIN(s2.schedule_id) FROM schedule s2 WHERE s2.course_id = cs.course_id AND s2.semester = '2025-2026-1') AS schedule_id,
       (SELECT MIN(s3.teacher_id) FROM schedule s3 WHERE s3.course_id = cs.course_id AND s3.semester = '2025-2026-1') AS teacher_id,
       CASE WHEN MOD(cs.student_id, 5) != 0 THEN 1 ELSE 0 END
FROM course_selection cs
WHERE cs.semester = '2025-2026-1' AND cs.status = 1
AND NOT EXISTS (SELECT 1 FROM score sc WHERE sc.student_id = cs.student_id AND sc.course_id = cs.course_id);

-- 更新GPA
UPDATE score SET gpa = CASE
  WHEN score_score >= 90 THEN 4.0
  WHEN score_score >= 85 THEN 3.7
  WHEN score_score >= 80 THEN 3.3
  WHEN score_score >= 75 THEN 3.0
  WHEN score_score >= 70 THEN 2.7
  WHEN score_score >= 65 THEN 2.3
  WHEN score_score >= 60 THEN 2.0
  ELSE 1.0
END WHERE gpa = 0.0;

-- ==================== 7. 考试 ====================
INSERT INTO exam (course_id, exam_name, exam_type, exam_date, start_time, end_time, semester) VALUES
(1, '高等数学A(一)期末考试', '期末考试', '2026-01-12', '09:00:00', '11:00:00', '2025-2026-1'),
(2, '线性代数期末考试', '期末考试', '2026-01-13', '09:00:00', '11:00:00', '2025-2026-1'),
(3, '大学物理B期末考试', '期末考试', '2026-01-14', '09:00:00', '11:00:00', '2025-2026-1'),
(4, '程序设计基础期末考试', '期末考试', '2026-01-15', '09:00:00', '11:00:00', '2025-2026-1'),
(5, '数据结构与算法期末考试', '期末考试', '2026-01-16', '09:00:00', '11:00:00', '2025-2026-1'),
(6, '计算机网络期末考试', '期末考试', '2026-01-12', '14:00:00', '16:00:00', '2025-2026-1'),
(7, '数据库原理期末考试', '期末考试', '2026-01-13', '14:00:00', '16:00:00', '2025-2026-1'),
(8, '操作系统期末考试', '期末考试', '2026-01-14', '14:00:00', '16:00:00', '2025-2026-1'),
(9, '大学英语(三)期末考试', '期末考试', '2026-01-15', '14:00:00', '16:00:00', '2025-2026-1'),
(10, '马克思主义基本原理期末考试', '期末考试', '2026-01-16', '14:00:00', '16:00:00', '2025-2026-1'),
(11, '电路分析基础期末考试', '期末考试', '2026-01-19', '09:00:00', '11:00:00', '2025-2026-1'),
(12, '微观经济学期末考试', '期末考试', '2026-01-19', '14:00:00', '16:00:00', '2025-2026-1'),
(13, '管理学原理期末考试', '期末考试', '2026-01-20', '09:00:00', '11:00:00', '2025-2026-1'),
(14, '机械制图期末考试', '期末考试', '2026-01-20', '14:00:00', '16:00:00', '2025-2026-1'),
(15, '体育(三)期末考试', '期末考试', '2026-01-21', '09:00:00', '10:00:00', '2025-2026-1');

-- ==================== 8. 考场分配 ====================
INSERT INTO exam_room (exam_id, classroom_id, seat_no, student_id)
SELECT e.exam_id, m.crid, CONCAT('R', ROW_NUMBER() OVER (PARTITION BY e.exam_id ORDER BY s.student_id)) AS seat_no, s.student_id
FROM exam e
JOIN course_selection cs ON cs.course_id = e.course_id AND cs.semester = e.semester AND cs.status = 1
JOIN student s ON s.student_id = cs.student_id
JOIN (SELECT 1 AS crid UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
      UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15
      UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20) m
WHERE MOD(s.student_id + e.course_id, 20) + 1 = m.crid;

-- ==================== 9. 监考指派 ====================
INSERT INTO invigilation (exam_id, teacher_id, classroom_id, duty)
SELECT e.exam_id, t.uid, c.clid, IF(ROW_NUMBER() OVER (PARTITION BY e.exam_id ORDER BY t.uid) = 1, '主监考', '副监考')
FROM exam e
JOIN (SELECT user_id AS uid, ROW_NUMBER() OVER (ORDER BY user_id) AS rn FROM user WHERE user_type = 2 LIMIT 30) t ON t.rn <= 30
JOIN (SELECT classroom_id AS clid, ROW_NUMBER() OVER (ORDER BY classroom_id) AS rn FROM classroom) c ON c.rn <= 30
WHERE (e.course_id + t.rn) % 30 + 1 = c.rn
GROUP BY e.exam_id, t.uid, c.clid;

-- ==================== 10. 评教 ====================
INSERT INTO evaluation (student_id, schedule_id, teacher_id, course_id, semester, score_teaching, score_content, score_method, comment)
SELECT cs.student_id, s.schedule_id, s.teacher_id, cs.course_id, cs.semester,
       70 + MOD(cs.student_id + cs.course_id, 30) AS score_teaching,
       65 + MOD(cs.student_id * 2 + cs.course_id, 35) AS score_content,
       70 + MOD(cs.student_id * 3 + cs.course_id, 30) AS score_method,
       CASE WHEN MOD(cs.student_id, 3) = 0 THEN '课程质量优秀，教师认真负责' ELSE NULL END AS comment
FROM course_selection cs
JOIN schedule s ON s.course_id = cs.course_id AND s.semester = cs.semester
WHERE cs.status = 1 AND cs.semester = '2025-2026-1'
AND MOD(cs.student_id, 3) = 0
AND NOT EXISTS (SELECT 1 FROM evaluation e WHERE e.student_id = cs.student_id AND e.course_id = cs.course_id);

-- ==================== 统计 ====================
SELECT '=== 数据验收 ===' AS '';
SELECT '课程' AS label, COUNT(*) AS cnt FROM course
UNION ALL SELECT '教室', COUNT(*) FROM classroom
UNION ALL SELECT '排课', COUNT(*) FROM schedule
UNION ALL SELECT '选课', COUNT(*) FROM course_selection
UNION ALL SELECT '成绩', COUNT(*) FROM score
UNION ALL SELECT '考试', COUNT(*) FROM exam
UNION ALL SELECT '考场分配', COUNT(*) FROM exam_room
UNION ALL SELECT '监考', COUNT(*) FROM invigilation
UNION ALL SELECT '评教', COUNT(*) FROM evaluation
UNION ALL SELECT '=== 完成 ===', 0;
