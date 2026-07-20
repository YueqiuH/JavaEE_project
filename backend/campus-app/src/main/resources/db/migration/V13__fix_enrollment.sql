
DELETE FROM enrollment WHERE year IN (2022, 2023, 2024, 2025);

-- 2022-2025锛氳鍒?> 瀹為檯锛堟牴鎹笓涓氱儹搴︼紝璁″垝姣斿疄闄呭 5%-15%锛?
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

-- 璁＄畻鎶ュ埌鐜?
UPDATE enrollment SET report_rate = ROUND(actual_count / plan_count * 100, 2) WHERE plan_count > 0;

-- 2026锛氬彧鏈夎鍒掓暟
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, 2026,
       CASE WHEN d.dept_code IN ('CS','EE','EM') THEN 120+FLOOR(RAND()*80)
            WHEN d.dept_code IN ('ME','CE','MED') THEN 80+FLOOR(RAND()*70)
            ELSE 50+FLOOR(RAND()*50) END,
       0, NULL
FROM major m JOIN department d ON d.dept_id = m.dept_id;

SELECT year, COUNT(*) AS plans, SUM(plan_count) AS 璁″垝, SUM(actual_count) AS 瀹為檯
FROM enrollment GROUP BY year ORDER BY year;

