-- ============================================
-- 鐪熷疄鏃堕棿绾夸慨姝ｏ紙2026-07-17锛?
-- 2026 绾ф湭鎶ュ埌 / 2022 绾у凡姣曚笟 / 寤舵瘯鏁版嵁 / 鎷涚敓璁″垝瀵归綈
-- ============================================

-- 1. 鍒犻櫎鎵€鏈?2026 绾у鐢燂紙8 鏈堝簳鎵嶆姤鍒帮級
DELETE FROM student WHERE enroll_year = 2026;

-- 2. 2022 绾цˉ鍏咃紙鍒氭瘯涓氾紝鐘舵€?3锛? 2023-2025 涓嶅彉
-- 鍏堢‘淇濇湁 2022 绾у勾绾?
INSERT IGNORE INTO grade (grade_name) VALUES ('2022绾?);
SET @g2022 := (SELECT grade_id FROM grade WHERE grade_name = '2022绾? LIMIT 1);

DROP PROCEDURE IF EXISTS gen_grad;
DELIMITER //
CREATE PROCEDURE gen_grad(
    IN base_no BIGINT, IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT, IN oris TEXT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total_ori INT;
    DECLARE g CHAR(1);
    DECLARE st INT;
    SET total_ori = CHAR_LENGTH(oris) - CHAR_LENGTH(REPLACE(oris, ',', '')) + 1;
    WHILE i <= cnt DO
        SET g = IF(i%4=0,'2','1');
        -- 90% 姝ｅ父姣曚笟, 7% 寤舵瘯(鐘舵€佷粛涓哄湪璇?浼戝), 3% 鑲勪笟
        SET st = CASE WHEN i <= cnt*0.9 THEN 3
                      WHEN i <= cnt*0.97 THEN IF(RAND()<0.6, 2, 1)
                      ELSE 0 END;
        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i,
            CONCAT(ELT(1+FLOOR(RAND()*8),'寮?,'鐜?,'鏉?,'璧?,'闄?,'鍒?,'鏉?,'榛?),
                   ELT(1+FLOOR(RAND()*6),'浼?,'濞?,'寮?,'娲?,'闆?,'鏁?,'楣?,'濠?,'鏄?,'鑺?,'纾?),
                   ELT(1+FLOOR(RAND()*4),'杞?,'娑?,'閾?,'鐟?,'杈?,'鐒?,'瀹?,'妗?,'鍝?)),
            IF(g='1',1,2),
            DATE_ADD('2004-06-15', INTERVAL (i*19 % 365) DAY),
            22,
            CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(oris,',',1+((i-1)%total_ori)),',',-1),'鍖?),
            @g2022, did, mid,
            CONCAT(cprefix, LPAD(1+(i-1) DIV 40, 2, '0')),
            SUBSTRING_INDEX(SUBSTRING_INDEX(oris,',',1+((i-1)%total_ori)),',',-1),
            2022, st
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- 涓烘瘡涓櫌绯荤敓鎴?2022 绾ф瘯涓氱敓锛堣妯′笌涔嬪墠绫讳技锛?
CALL gen_grad(2022100000,@d_cs,@m_cs01,'杞欢22',120,'娴庡崡,闈掑矝,鐑熷彴,閮戝窞,娲涢槼,鐭冲搴?澶師,瑗垮畨,姝︽眽,鎴愰兘,鍗椾含,鍚堣偉');
CALL gen_grad(2022100200,@d_cs,@m_cs02,'鏅鸿兘22', 80,'鍖椾含,涓婃捣,娣卞湷,骞垮窞,鏉窞,鍗椾含,姝︽眽,鎴愰兘,閲嶅簡,澶╂触');
CALL gen_grad(2022100300,@d_cs,@m_cs03,'鏁版嵁22', 60,'闀挎矙,鍗楁槍,绂忓窞,鍗楀畞,鏄嗘槑,璐甸槼,鍏板窞,瑗垮畞,閾跺窛');
CALL gen_grad(2022100500,@d_ee,@m_ee01,'鐢典俊22', 90,'鎴愰兘,缁甸槼,寰烽槼,鍗楀厖,瀹滃,娉稿窞,鑷础,閲嶅簡,璐甸槼');
CALL gen_grad(2022100600,@d_ee,@m_ee02,'閫氫俊22', 60,'瑗垮畨,鍜搁槼,瀹濋浮,娓崡,姹変腑,寤跺畨,鍏板窞,澶╂按');
CALL gen_grad(2022100700,@d_ee,@m_ee03,'鐗╄仈22', 50,'娣卞湷,骞垮窞,鏉窞,鍗椾含,姝︽眽,鎴愰兘,鍖椾含,涓婃捣');
CALL gen_grad(2022100800,@d_em,@m_em01,'浼氳22',100,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹熼棬,鑲囧簡');
CALL gen_grad(2022100900,@d_em,@m_em02,'宸ョ22', 70,'鏉窞,瀹佹尝,娓╁窞,鍢夊叴,缁嶅叴,閲戝崕,鍙板窞,婀栧窞');
CALL gen_grad(2022101000,@d_em,@m_em03,'閲戣瀺22', 55,'涓婃捣,鍖椾含,娣卞湷,骞垮窞,鏉窞,鍗椾含,鑻忓窞,鎴愰兘');
CALL gen_grad(2022101100,@d_me,@m_me01,'鏈烘22',100,'娌堥槼,澶ц繛,闉嶅北,闀挎槬,鍚夋灄,鍝堝皵婊?澶у簡,榻愰綈鍝堝皵');
CALL gen_grad(2022101200,@d_me,@m_me02,'杞﹁締22', 65,'娴庡崡,闈掑矝,娣勫崥,鐑熷彴,娼嶅潑,娴庡畞,娉板畨,濞佹捣');
CALL gen_grad(2022101300,@d_me,@m_me03,'鏅洪€?2', 45,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,鍗佸牥');
CALL gen_grad(2022101400,@d_ce,@m_ce01,'鍦熸湪22', 70,'閲嶅簡,鎴愰兘,璐甸槼,鏄嗘槑,鍗楀畞,瑗垮畨,姝︽眽,闀挎矙');
CALL gen_grad(2022101500,@d_ce,@m_ce02,'宸ョ22', 45,'鍗椾含,鑻忓窞,鏃犻敗,鍚堣偉,鍗楁槍,鏉窞,瀹佹尝');
CALL gen_grad(2022101600,@d_law,@m_law01,'娉曞22', 55,'娴庡崡,闈掑矝,閮戝窞,姝︽眽,闀挎矙,鎴愰兘,瑗垮畨');
CALL gen_grad(2022101700,@d_law,@m_law02,'鐭ヤ骇22', 35,'鍖椾含,涓婃捣,骞垮窞,娣卞湷,鍗椾含,鏉窞,澶╂触');
CALL gen_grad(2022101800,@d_ch,@m_ch01,'鍖栧伐22', 50,'鍗椾含,鑻忓窞,鏃犻敗,鍚堣偉,姝︽眽,闀挎矙,鍗楁槍');
CALL gen_grad(2022101900,@d_ch,@m_ch02,'搴斿寲22', 35,'娴庡崡,闈掑矝,鏉窞,瀹佹尝,鍘﹂棬,绂忓窞,骞垮窞');
CALL gen_grad(2022102000,@d_med,@m_med01,'涓村簥22', 55,'鎴愰兘,閲嶅簡,瑗垮畨,鍏板窞,鏄嗘槑,璐甸槼,鍗楀畞');
CALL gen_grad(2022102100,@d_med,@m_med02,'鎶ょ悊22', 45,'閮戝窞,姝︽眽,闀挎矙,鍗楁槍,鍚堣偉,鍗椾含,涓婃捣');
CALL gen_grad(2022102200,@d_ls,@m_ls01,'鐢熸妧22', 45,'娴庡崡,闈掑矝,鐑熷彴,姝︽眽,闀挎矙,鍚堣偉,鍗楁槍');
CALL gen_grad(2022102300,@d_ls,@m_ls02,'椋熷搧22', 35,'鎴愰兘,閲嶅簡,鏄嗘槑,璐甸槼,鍗楀畞,骞垮窞,绂忓窞');
CALL gen_grad(2022102400,@d_fl,@m_fl01,'鑻辫22', 40,'澶ц繛,闈掑矝,鍘﹂棬,鑻忓窞,鏃犻敗,瀹佹尝,绂忓窞');
CALL gen_grad(2022102500,@d_fl,@m_fl02,'鏃ヨ22', 30,'娌堥槼,闀挎槬,鍝堝皵婊?寤惰竟,澶╂触,娴庡崡,瑗垮畨');
CALL gen_grad(2022102600,@d_edu,@m_edu01,'鏁欒偛22', 40,'閮戝窞,娴庡崡,姝︽眽,闀挎矙,鎴愰兘,瑗垮畨,鍚堣偉');
CALL gen_grad(2022102700,@d_edu,@m_edu02,'瀛﹀墠22', 35,'鍗椾含,鏉窞,绂忓窞,骞垮窞,鍗楀畞,鏄嗘槑,璐甸槼');
CALL gen_grad(2022102800,@d_jr,@m_jr01,'鏂伴椈22', 35,'鍖椾含,涓婃捣,骞垮窞,娣卞湷,鏉窞,鍗椾含,姝︽眽');
CALL gen_grad(2022102900,@d_jr,@m_jr02,'缃戞柊22', 30,'鎴愰兘,閲嶅簡,瑗垮畨,闀挎矙,閮戝窞,娴庡崡,鍚堣偉');
CALL gen_grad(2022103000,@d_ms,@m_ms01,'鏁板22', 35,'娴庡崡,闈掑矝,閮戝窞,姝︽眽,闀挎矙,鍚堣偉,鍗楁槍');
CALL gen_grad(2022103100,@d_ms,@m_ms02,'缁熻22', 25,'鍗椾含,鏉窞,涓婃捣,鑻忓窞,鏃犻敗,瀹佹尝,鍘﹂棬');
CALL gen_grad(2022103200,@d_ms,@m_ms03,'鏁拌22', 20,'鎴愰兘,閲嶅簡,瑗垮畨,鏄嗘槑,璐甸槼,鍗楀畞,骞垮窞');
CALL gen_grad(2022103300,@d_ad,@m_ad01,'瑙嗕紶22', 30,'鍖椾含,涓婃捣,鏉窞,鎴愰兘,閲嶅簡,姝︽眽,鍗椾含');
CALL gen_grad(2022103400,@d_ad,@m_ad02,'鐜22', 25,'鑻忓窞,鏃犻敗,瀹佹尝,鍘﹂棬,闈掑矝,澶ц繛,闀挎矙');
CALL gen_grad(2022103500,@d_ad,@m_ad03,'鏁板獟22', 20,'澶╂触,娌堥槼,娴庡崡,鍚堣偉,鍗楁槍,绂忓窞,鏄嗘槑');

-- 3. 琛ュ厖灏戦噺 2019-2021 绾у欢姣曞鐢燂紙璇讳簡 5-7 骞磋繕娌℃瘯涓氾級
DROP PROCEDURE IF EXISTS gen_delay;
DELIMITER //
CREATE PROCEDURE gen_delay(
    IN base_no BIGINT, IN ey INT, IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= cnt DO
        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i,
            CONCAT(ELT(1+FLOOR(RAND()*5),'寮?,'鐜?,'鏉?,'璧?,'闄?),'寤舵瘯',i),
            IF(i%3=0,2,1),
            DATE_ADD(CONCAT(ey,'-09-01'), INTERVAL (i*31 % 365) DAY),
            2026 - ey + 18,
            '鏍″唴',
            NULL, did, mid,
            CONCAT(cprefix, '01'), '鏈渷', ey,
            IF(RAND()<0.6, 2, 1)
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- 姣忎釜澶ч櫌鐣?15-25 涓欢姣曠敓
CALL gen_delay(2019100000,2019,@d_cs,@m_cs01,'杞欢19寤?,25);
CALL gen_delay(2019100100,2019,@d_me,@m_me01,'鏈烘19寤?,20);
CALL gen_delay(2019100200,2019,@d_em,@m_em01,'浼氳19寤?,15);
CALL gen_delay(2019100300,2019,@d_ee,@m_ee01,'鐢典俊19寤?,15);
CALL gen_delay(2020100000,2020,@d_cs,@m_cs01,'杞欢20寤?,20);
CALL gen_delay(2020100100,2020,@d_me,@m_me01,'鏈烘20寤?,15);
CALL gen_delay(2021100000,2021,@d_cs,@m_cs01,'杞欢21寤?,15);

DROP PROCEDURE IF EXISTS gen_grad;
DROP PROCEDURE IF EXISTS gen_delay;

-- 4. 鏇存柊鎷涚敓璁″垝锛?026 绾ф姤鍒版暟涓?0
UPDATE enrollment SET actual_count = 0, report_rate = 0 WHERE year = 2026;

-- 5. 鏍规嵁鐪熷疄 student 琛ㄦ洿鏂板叾浠栧勾搴︽姤鍒版暟
UPDATE enrollment e
JOIN (
    SELECT major_id, enroll_year AS year, COUNT(*) AS cnt
    FROM student WHERE status = 1
    GROUP BY major_id, enroll_year
) s ON s.major_id = e.major_id AND s.year = e.year
SET e.actual_count = s.cnt,
    e.report_rate = ROUND(s.cnt / e.plan_count * 100, 2)
WHERE e.plan_count > 0 AND e.year != 2026;

SELECT '---------- 淇鍚庣粺璁?----------' AS '';
SELECT '2026绾у湪璇?搴斾负0): ', COUNT(*) FROM student WHERE enroll_year = 2026 AND status = 1;
SELECT '2022绾ф瘯涓? ', COUNT(*) FROM student WHERE enroll_year = 2022 AND status = 3;
SELECT '寤舵瘯/浼戝: ', COUNT(*) FROM student WHERE enroll_year <= 2021 AND status IN (1,2);
SELECT '鍦ㄨ瀛︾敓(2023-2025): ', COUNT(*) FROM student WHERE enroll_year BETWEEN 2023 AND 2025 AND status = 1;
SELECT '瀛︾敓鎬绘暟: ', COUNT(*) FROM student;

