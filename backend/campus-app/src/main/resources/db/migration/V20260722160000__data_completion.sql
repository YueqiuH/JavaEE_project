-- ============================================================
-- 数据完善V2：辅导员分配、班级规范化、成绩补充
-- ============================================================

-- ==================== 1. 规范化年级名称 ====================
UPDATE grade SET grade_name = '2022级' WHERE grade_id = 2;
UPDATE grade SET grade_name = '2023级' WHERE grade_id = 3;
UPDATE grade SET grade_name = '2024级' WHERE grade_id = 4;
UPDATE grade SET grade_name = '2025级' WHERE grade_id = 5;
UPDATE grade SET grade_name = '2026级' WHERE grade_id = 6;

-- ==================== 2. 辅导员分配(按院系+年级) ====================
-- 预查辅导员ID列表
SET @c1 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=1);
SET @c2 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=2);
SET @c3 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=3);
SET @c4 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=4);
SET @c5 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=5);
SET @c6 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=6);
SET @c7 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=7);
SET @c8 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=8);
SET @c9 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=9);
SET @c10 = (SELECT MIN(user_id) FROM user WHERE user_type=2 AND dept_id=10);

UPDATE student SET counselor_id = @c1 WHERE dept_id=1 AND grade_id=2 AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c1 WHERE dept_id=1 AND grade_id=3 AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c2 WHERE dept_id=2 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c3 WHERE dept_id=3 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c4 WHERE dept_id=4 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c5 WHERE dept_id=5 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c6 WHERE dept_id=6 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c7 WHERE dept_id=7 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c8 WHERE dept_id=8 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c9 WHERE dept_id=9 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c10 WHERE dept_id=10 AND (grade_id=2 OR grade_id=3) AND counselor_id IS NULL;

-- 4/5年级也分配
UPDATE student SET counselor_id = @c1 WHERE dept_id=1 AND grade_id IN (4,5) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c4 WHERE dept_id=4 AND grade_id IN (4,5) AND counselor_id IS NULL;
UPDATE student SET counselor_id = @c6 WHERE dept_id=6 AND grade_id IN (4,5) AND counselor_id IS NULL;

-- ==================== 3. 补充班级名 ====================
UPDATE student SET class_name = CONCAT(
    SUBSTRING(class_name, 1, 2),
    CASE grade_id WHEN 2 THEN '22' WHEN 3 THEN '23' WHEN 4 THEN '24' WHEN 5 THEN '25' WHEN 6 THEN '26' ELSE '00' END,
    LPAD(MOD(student_id, 4) + 1, 2, '0'))
WHERE class_name IS NOT NULL AND class_name LIKE '班级%';

-- ==================== 4. 补充成绩(2022级:完整4学期成绩) ====================
INSERT IGNORE INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, publish_status, teacher_id)
SELECT s.student_id, c.course_id,
       50 + MOD(s.student_id * 13 + c.course_id * 17, 50),
       '2024-2025-1', 0.0, 1,
       30 + MOD(s.student_id * 3 + c.course_id, 70),
       40 + MOD(s.student_id * 7 + c.course_id * 5, 60),
       0.30, 0.70, 1,
       (SELECT MIN(teacher_id) FROM schedule sc2 WHERE sc2.course_id = c.course_id LIMIT 1)
FROM student s CROSS JOIN course c
WHERE s.grade_id = 2 AND s.status = 1 AND c.course_id <= 12
LIMIT 3000;

INSERT IGNORE INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, publish_status, teacher_id)
SELECT s.student_id, c.course_id,
       50 + MOD(s.student_id * 19 + c.course_id * 7, 50),
       '2024-2025-2', 0.0, 1,
       30 + MOD(s.student_id * 5 + c.course_id, 70),
       40 + MOD(s.student_id * 11 + c.course_id * 3, 60),
       0.30, 0.70, 1,
       (SELECT MIN(teacher_id) FROM schedule sc2 WHERE sc2.course_id = c.course_id LIMIT 1)
FROM student s CROSS JOIN course c
WHERE s.grade_id = 2 AND s.status = 1 AND c.course_id <= 12
LIMIT 3000;

-- ==================== 5. 补充成绩(2023/2024/2025级:本学期) ====================
INSERT IGNORE INTO score (student_id, course_id, score_score, semester, gpa, status, regular_score, exam_score, regular_ratio, exam_ratio, publish_status, teacher_id)
SELECT s.student_id, c.course_id,
       45 + MOD(s.student_id * 11 + c.course_id * 23, 55),
       '2025-2026-1', 0.0, 1,
       20 + MOD(s.student_id * 7, 80),
       35 + MOD(s.student_id * 13 + c.course_id, 65),
       0.30, 0.70, 1,
       (SELECT MIN(teacher_id) FROM schedule sc2 WHERE sc2.course_id = c.course_id LIMIT 1)
FROM student s CROSS JOIN course c
WHERE s.grade_id IN (3,4,5) AND s.status = 1 AND c.course_id <= 8
LIMIT 4000;

-- ==================== 6. 更新GPA ====================
UPDATE score SET gpa = CASE
  WHEN score_score >= 90 THEN 4.0 WHEN score_score >= 85 THEN 3.7
  WHEN score_score >= 80 THEN 3.3 WHEN score_score >= 75 THEN 3.0
  WHEN score_score >= 70 THEN 2.7 WHEN score_score >= 65 THEN 2.3
  WHEN score_score >= 60 THEN 2.0 ELSE 0.0
END WHERE gpa = 0.0;

-- ==================== 统计验收 ====================
SELECT '=== 数据完善验收 ===' AS '';
SELECT '学生总数' AS label, COUNT(*) AS cnt FROM student
UNION ALL SELECT '已分配辅导员', COUNT(*) FROM student WHERE counselor_id IS NOT NULL
UNION ALL SELECT '未分配辅导员', COUNT(*) FROM student WHERE counselor_id IS NULL
UNION ALL SELECT '班级数', COUNT(DISTINCT class_name) FROM student
UNION ALL SELECT '成绩总数', COUNT(*) FROM score
UNION ALL SELECT '已发布成绩', COUNT(*) FROM score WHERE publish_status=1
UNION ALL SELECT '未通过成绩', COUNT(*) FROM score WHERE score_score < 60
UNION ALL SELECT '=== 完成 ===', 0;
