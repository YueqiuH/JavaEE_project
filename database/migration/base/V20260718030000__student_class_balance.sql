-- ============================================
-- 成员 D：学生班级人数均匀化
-- 按 (专业,年级) 分组建新班级, 组内每班差距≤1人
-- 目标每班约35人, 全量范围 29~40 人
-- ============================================

DROP TEMPORARY TABLE IF EXISTS tmp_plan;

CREATE TEMPORARY TABLE tmp_plan (
  student_id  BIGINT PRIMARY KEY,
  new_class   VARCHAR(16)
);

INSERT INTO tmp_plan
SELECT
  student_id,
  CONCAT(
    abbr,
    grade_suffix,
    LPAD(
      CASE
        WHEN rn <= extra * (base_sz + 1)
        THEN CEIL(rn / (base_sz + 1))
        ELSE extra + CEIL((rn - extra * (base_sz + 1)) / base_sz)
      END,
      2, '0'
    )
  ) AS new_class
FROM (
  SELECT
    s.student_id,
    SUBSTRING(s.class_name, 1, 2) AS abbr,
    SUBSTRING(s.class_name, 3, 2) AS grade_suffix,
    CAST(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2)) AS SIGNED) AS total,
    CAST(CEIL(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2)) / 35.0) AS SIGNED) AS class_cnt,
    CAST(FLOOR(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2))
         / CEIL(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2)) / 35.0)) AS SIGNED) AS base_sz,
    CAST(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2))
         - FLOOR(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2))
               / CEIL(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2)) / 35.0))
           * CEIL(COUNT(*) OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2)) / 35.0) AS SIGNED) AS extra,
    ROW_NUMBER() OVER (PARTITION BY s.major_id, SUBSTRING(s.class_name, 3, 2) ORDER BY s.student_id) AS rn
  FROM student s
  WHERE s.status = 1 AND s.class_name IS NOT NULL
) t;

UPDATE student s
JOIN tmp_plan p ON s.student_id = p.student_id
SET s.class_name = p.new_class;

DROP TEMPORARY TABLE IF EXISTS tmp_plan;
