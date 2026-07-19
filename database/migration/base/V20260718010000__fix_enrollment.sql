USE school_spring;

DELETE FROM enrollment;

-- 2022-2025：计划 > 实际（根据专业热度，计划比实际多 5%-15%）
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, s.enroll_year,
       CASE WHEN d.dept_code IN ('CS','EE','EM') THEN CEIL(s.cnt * (1.04 + RAND()*0.04))
            WHEN d.dept_code IN ('ME','CE','MED') THEN CEIL(s.cnt * (1.07 + RAND()*0.06))
            ELSE CEIL(s.cnt * (1.10 + RAND()*0.08)) END AS plan,
       s.cnt AS actual,
       NULL AS rate
FROM major m
JOIN department d ON d.dept_id = m.dept_id
JOIN (SELECT major_id, enroll_year, COUNT(*) AS cnt
      FROM student GROUP BY major_id, enroll_year) s
  ON s.major_id = m.major_id
WHERE s.cnt > 0;

-- 计算报到率
UPDATE enrollment SET report_rate = ROUND(actual_count / plan_count * 100, 2) WHERE plan_count > 0;

-- 2026：只有计划数
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, 2026,
       CASE WHEN d.dept_code IN ('CS','EE','EM') THEN 120+FLOOR(RAND()*80)
            WHEN d.dept_code IN ('ME','CE','MED') THEN 80+FLOOR(RAND()*70)
            ELSE 50+FLOOR(RAND()*50) END,
       0, NULL
FROM major m JOIN department d ON d.dept_id = m.dept_id;

SELECT year, COUNT(*) AS plans, SUM(plan_count) AS 计划, SUM(actual_count) AS 实际
FROM enrollment GROUP BY year ORDER BY year;
