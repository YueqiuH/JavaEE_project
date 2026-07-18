-- ============================================
-- 真实数据打磨：报到率分化 / 招生计划对齐 / 延毕休学标签
-- ============================================
USE school_spring;

-- 1. 每个专业每年的计划数对齐实际报到率（根据专业热度 75%-98%）
--    热门: CS/EE/EM 类 93-98%  工程: ME/CE 88-95%
--    专业: Med/Law 85-92%      理学: CH/LS/MS 80-88%
--    文科: FL/EDU/JR/AD 75-85%

UPDATE enrollment e
JOIN (SELECT major_id, year, cnt FROM (
    SELECT major_id, enroll_year AS year, COUNT(*) AS cnt
    FROM student WHERE status = 1 GROUP BY major_id, enroll_year
) t) s ON s.major_id = e.major_id AND s.year = e.year
JOIN major m ON m.major_id = e.major_id
SET e.actual_count = s.cnt,
    e.plan_count = CASE
        WHEN m.major_code IN ('CS01','CS02','CS03','EE01','EE02','EE03','EM01','EM03')
            THEN CEIL(s.cnt / (0.93 + RAND()*0.05))
        WHEN m.major_code IN ('ME01','ME02','ME03','CE01')
            THEN CEIL(s.cnt / (0.88 + RAND()*0.07))
        WHEN m.major_code IN ('MED01','MED02','LAW01')
            THEN CEIL(s.cnt / (0.85 + RAND()*0.07))
        WHEN m.major_code IN ('CH01','CH02','LS01','LS02','MS01','MS02','MS03')
            THEN CEIL(s.cnt / (0.80 + RAND()*0.08))
        ELSE CEIL(s.cnt / (0.75 + RAND()*0.10))
    END,
    e.report_rate = ROUND(s.cnt / CASE
        WHEN m.major_code IN ('CS01','CS02','CS03','EE01','EE02','EE03','EM01','EM03')
            THEN CEIL(s.cnt / (0.93 + RAND()*0.05))
        ELSE CEIL(s.cnt / (0.78 + RAND()*0.15))
    END * 100, 2)
WHERE s.cnt > 0 AND e.year IN (2023, 2024, 2025);

-- 2026 年仅有计划数（根据历年热度推算，报到数为0）
UPDATE enrollment e
JOIN major m ON m.major_id = e.major_id
SET e.plan_count = CASE
        WHEN m.major_code IN ('CS01','CS02','CS03','EE01','EE02','EE03','EM01','EM03')
            THEN 100 + FLOOR(RAND()*80)
        WHEN m.major_code IN ('ME01','ME02','ME03','CE01')
            THEN 80 + FLOOR(RAND()*60)
        ELSE 40 + FLOOR(RAND()*50)
    END,
    e.actual_count = 0,
    e.report_rate = NULL
WHERE e.year = 2026;

-- 2. 延毕学生标记为 status=2 休学(延毕)，同时保持一些退伍复学的情况
--    已有的延毕/休学不变，加一些退伍复学的(大二休学两年)

-- 3. 补充少量退伍复学学生（学号年代久远但 status=1 且 enroll_year 早）
INSERT IGNORE INTO student (student_no, student_name, gender, student_birth, student_age, student_address, grade_id, dept_id, major_id, class_name, origin_place, enroll_year, status)
SELECT 2019200000 + seq, CONCAT('退伍生', seq), IF(seq%3=0,2,1),
       '2001-01-15', 25, '校内', NULL,
       (SELECT dept_id FROM department WHERE dept_code = ELT(1+FLOOR(RAND()*6),'CS','EE','EM','ME','CE','CH')),
       (SELECT major_id FROM major WHERE major_code = ELT(1+FLOOR(RAND()*3),'CS01','EE01','EM01')),
       CONCAT('原班级', seq), '本省', 2019, 1
FROM (SELECT 1 AS seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
      UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) t
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = 2019200000 + t.seq);

-- 4. 教职工增加退休/离职状态（部分老教职工标记为离职）
UPDATE `user` SET status = 2 WHERE user_type IN (2,3) AND status = 1 AND RAND() < 0.12;
-- 给 admin 和几个核心教职工恢复
UPDATE `user` SET status = 1 WHERE username IN ('admin','700001','700002','700003','800001');

-- 5. 状态说明注释（MySQL COMMENT）
SELECT '---------- 打磨结果 ----------' AS '';
SELECT '2026 招生计划(报到=0): ', COUNT(*) FROM enrollment WHERE year = 2026;
SELECT '2023-2025 招生计划: ', COUNT(*) FROM enrollment WHERE year BETWEEN 2023 AND 2025;
SELECT '各专业报到率范围 2025: ' AS '';
SELECT m.major_name, e.plan_count, e.actual_count, e.report_rate
FROM enrollment e JOIN major m ON m.major_id = e.major_id
WHERE e.year = 2025 ORDER BY e.report_rate DESC LIMIT 10;
SELECT '全校在读: ', COUNT(*) FROM student WHERE status = 1;
SELECT '已毕业: ', COUNT(*) FROM student WHERE status = 3;
SELECT '休学/延毕: ', COUNT(*) FROM student WHERE status = 2;
SELECT '退学/肄业: ', COUNT(*) FROM student WHERE status = 0;
