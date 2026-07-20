-- 淇锛氬欢姣曠敓鍚嶅瓧 + 闄㈢郴涓撲笟褰掑睘

-- 1. 缁欐墍鏈夌己闄㈢郴/涓撲笟鐨勫鐢熼殢鏈哄垎閰嶏紙鎸?enroll_year 鍖归厤鍚堢悊闄㈢郴锛?
UPDATE student s
JOIN (
    SELECT s2.student_id,
           CASE WHEN s2.enroll_year <= 2020 THEN
               ELT(1+FLOOR(RAND()*6),(SELECT dept_id FROM department WHERE dept_code='CS'),
                                      (SELECT dept_id FROM department WHERE dept_code='ME'),
                                      (SELECT dept_id FROM department WHERE dept_code='EM'),
                                      (SELECT dept_id FROM department WHERE dept_code='EE'),
                                      (SELECT dept_id FROM department WHERE dept_code='CE'),
                                      (SELECT dept_id FROM department WHERE dept_code='CH'))
           ELSE
               ELT(1+FLOOR(RAND()*6),(SELECT dept_id FROM department WHERE dept_code='CS'),
                                      (SELECT dept_id FROM department WHERE dept_code='EE'),
                                      (SELECT dept_id FROM department WHERE dept_code='EM'),
                                      (SELECT dept_id FROM department WHERE dept_code='ME'),
                                      (SELECT dept_id FROM department WHERE dept_code='MED'),
                                      (SELECT dept_id FROM department WHERE dept_code='LS'))
           END AS new_dept
    FROM student s2
    WHERE s2.dept_id IS NULL
) fix ON fix.student_id = s.student_id
SET s.dept_id = fix.new_dept
WHERE s.dept_id IS NULL;

-- 2. 鏍规嵁闄㈢郴鍒嗛厤涓撲笟
UPDATE student s
JOIN (
    SELECT student_id, dept_id,
           (SELECT major_id FROM major WHERE dept_id = s2.dept_id ORDER BY RAND() LIMIT 1) AS new_major
    FROM student s2 WHERE s2.major_id IS NULL
) fix ON fix.student_id = s.student_id
SET s.major_id = fix.new_major
WHERE s.major_id IS NULL;

-- 3. 淇鍋囧悕瀛楋細鐢ㄦ甯稿鍚嶆浛鎹?
-- 濮撴皬 + 甯歌鍚?
UPDATE student SET student_name = CONCAT(
    ELT(1+FLOOR(RAND()*10), '寮?,'鐜?,'鏉?,'璧?,'闄?,'鍒?,'鏉?,'榛?,'鍛?,'鍚?),
    ELT(1+FLOOR(RAND()*10), '瀛愯僵','闆ㄦ兜','娴╃劧','鎬濈惇','澶╁畤','姊︾懚','蹇楄繙','鑻ユ洣','鍗氭枃','鏅撳饯'),
    ELT(1+FLOOR(RAND()*5), '','鎬?,'閾?,'杈?,'鐒?,'妗?,'鍝?,'鐞?,'鍗?,'钀?,'鐨?,'娆?,'娉?,'閫?)
) WHERE student_name LIKE '%寤舵瘯%' OR student_name LIKE '%閫€浼?';

SELECT '淇鍚庢鏌? AS '';
SELECT '缂洪櫌绯? ', COUNT(*) FROM student WHERE dept_id IS NULL;
SELECT '缂轰笓涓? ', COUNT(*) FROM student WHERE major_id IS NULL;
SELECT '鍋囧悕瀛? ', COUNT(*) FROM student WHERE student_name LIKE '%寤舵瘯%' OR student_name LIKE '%閫€浼?';

