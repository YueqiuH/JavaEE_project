-- ============================================
-- 鎴愬憳 D锛氬鐢熺彮绾т汉鏁板潎鍖€鍖?
-- 鎸?(涓撲笟,骞寸骇) 鍒嗙粍寤烘柊鐝骇, 缁勫唴姣忕彮宸窛鈮?浜?
-- 鐩爣姣忕彮绾?5浜? 鍏ㄩ噺鑼冨洿 29~40 浜?
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

