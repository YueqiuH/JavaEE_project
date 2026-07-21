-- ============================================
-- 鐪熷疄鏁版嵁鎵撶（锛氭姤鍒扮巼鍒嗗寲 / 鎷涚敓璁″垝瀵归綈 / 寤舵瘯浼戝鏍囩
-- ============================================

-- 1. 姣忎釜涓撲笟姣忓勾鐨勮鍒掓暟瀵归綈瀹為檯鎶ュ埌鐜囷紙鏍规嵁涓撲笟鐑害 75%-98%锛?
--    鐑棬: CS/EE/EM 绫?93-98%  宸ョ▼: ME/CE 88-95%
--    涓撲笟: Med/Law 85-92%      鐞嗗: CH/LS/MS 80-88%
--    鏂囩: FL/EDU/JR/AD 75-85%

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

-- 2026 骞翠粎鏈夎鍒掓暟锛堟牴鎹巻骞寸儹搴︽帹绠楋紝鎶ュ埌鏁颁负0锛?
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

-- 2. 寤舵瘯瀛︾敓鏍囪涓?status=2 浼戝(寤舵瘯)锛屽悓鏃朵繚鎸佷竴浜涢€€浼嶅瀛︾殑鎯呭喌
--    宸叉湁鐨勫欢姣?浼戝涓嶅彉锛屽姞涓€浜涢€€浼嶅瀛︾殑(澶т簩浼戝涓ゅ勾)

-- 3. 琛ュ厖灏戦噺閫€浼嶅瀛﹀鐢燂紙瀛﹀彿骞翠唬涔呰繙浣?status=1 涓?enroll_year 鏃╋級
INSERT IGNORE INTO student (student_no, student_name, gender, student_birth, student_age, student_address, grade_id, dept_id, major_id, class_name, origin_place, enroll_year, status)
SELECT 2019200000 + seq, CONCAT('閫€浼嶇敓', seq), IF(seq%3=0,2,1),
       '2001-01-15', 25, '鏍″唴', NULL,
       (SELECT dept_id FROM department WHERE dept_code = ELT(1+FLOOR(RAND()*6),'CS','EE','EM','ME','CE','CH')),
       (SELECT major_id FROM major WHERE major_code = ELT(1+FLOOR(RAND()*3),'CS01','EE01','EM01')),
       CONCAT('鍘熺彮绾?, seq), '鏈渷', 2019, 1
FROM (SELECT 1 AS seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
      UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14 UNION ALL SELECT 15) t
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = 2019200000 + t.seq);

-- 4. 鏁欒亴宸ュ鍔犻€€浼?绂昏亴鐘舵€侊紙閮ㄥ垎鑰佹暀鑱屽伐鏍囪涓虹鑱岋級
UPDATE `user` SET status = 2 WHERE user_type IN (2,3) AND status = 1 AND user_id % 100 < 12;
-- 缁?admin 鍜屽嚑涓牳蹇冩暀鑱屽伐鎭㈠
UPDATE `user` SET status = 1 WHERE username IN ('admin','700001','700002','700003','800001');

-- 5. 鐘舵€佽鏄庢敞閲婏紙MySQL COMMENT锛?
SELECT '---------- 鎵撶（缁撴灉 ----------' AS '';
SELECT '2026 鎷涚敓璁″垝(鎶ュ埌=0): ', COUNT(*) FROM enrollment WHERE year = 2026;
SELECT '2023-2025 鎷涚敓璁″垝: ', COUNT(*) FROM enrollment WHERE year BETWEEN 2023 AND 2025;
SELECT '鍚勪笓涓氭姤鍒扮巼鑼冨洿 2025: ' AS '';
SELECT m.major_name, e.plan_count, e.actual_count, e.report_rate
FROM enrollment e JOIN major m ON m.major_id = e.major_id
WHERE e.year = 2025 ORDER BY e.report_rate DESC LIMIT 10;
SELECT '鍏ㄦ牎鍦ㄨ: ', COUNT(*) FROM student WHERE status = 1;
SELECT '宸叉瘯涓? ', COUNT(*) FROM student WHERE status = 3;
SELECT '浼戝/寤舵瘯: ', COUNT(*) FROM student WHERE status = 2;
SELECT '閫€瀛?鑲勪笟: ', COUNT(*) FROM student WHERE status = 0;

