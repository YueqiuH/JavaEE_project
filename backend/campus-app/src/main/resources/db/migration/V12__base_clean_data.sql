-- ============================================
-- 骞插噣閲嶅缓锛?022-2025 绾э紝鐘舵€佸垎绂伙紙4=寤舵瘯锛?
-- 鐪熷疄姣斾緥锛氬湪璇?95%锛屾瘯涓?2022)~90%锛屼紤瀛︽瀬灏戯紝閫€瀛︽瀬灏?
-- ============================================

-- Guard: only wipe on fresh DB (idempotency safety)
SET @existing := (SELECT COUNT(*) FROM student WHERE student_no < 2022100000);

DELETE FROM student WHERE @existing = 0 OR student_no >= 2022100000;
DELETE FROM enrollment;

-- 骞寸骇
DELETE FROM grade;
INSERT INTO grade (grade_name) VALUES ('2022绾?),('2023绾?),('2024绾?),('2025绾?);
SET @g22 := (SELECT grade_id FROM grade WHERE grade_name='2022绾?);
SET @g23 := (SELECT grade_id FROM grade WHERE grade_name='2023绾?);
SET @g24 := (SELECT grade_id FROM grade WHERE grade_name='2024绾?);
SET @g25 := (SELECT grade_id FROM grade WHERE grade_name='2025绾?);

-- 闄㈢郴 ID
SET @d_cs  := (SELECT dept_id FROM department WHERE dept_code='CS');
SET @d_ee  := (SELECT dept_id FROM department WHERE dept_code='EE');
SET @d_em  := (SELECT dept_id FROM department WHERE dept_code='EM');
SET @d_me  := (SELECT dept_id FROM department WHERE dept_code='ME');
SET @d_ce  := (SELECT dept_id FROM department WHERE dept_code='CE');
SET @d_law := (SELECT dept_id FROM department WHERE dept_code='LAW');
SET @d_ch  := (SELECT dept_id FROM department WHERE dept_code='CH');
SET @d_med := (SELECT dept_id FROM department WHERE dept_code='MED');
SET @d_ls  := (SELECT dept_id FROM department WHERE dept_code='LS');
SET @d_fl  := (SELECT dept_id FROM department WHERE dept_code='FL');
SET @d_edu := (SELECT dept_id FROM department WHERE dept_code='EDU');
SET @d_jr  := (SELECT dept_id FROM department WHERE dept_code='JR');
SET @d_ms  := (SELECT dept_id FROM department WHERE dept_code='MS');
SET @d_ad  := (SELECT dept_id FROM department WHERE dept_code='AD');

-- 濮撴皬姹狅紙100涓湡瀹炲姘忥級
SET @surnames = '寮?鐜?鏉?璧?闄?鍒?鏉?榛?鍛?鍚?寰?瀛?鑳?鏈?楂?鏋?浣?閮?椹?缃?姊?瀹?閮?璋?闊?鍞?鍐?浜?钁?钀?绋?鏇?琚?閭?璁?鍌?娌?鏇?褰?鍚?鑻?鍗?钂?钄?璐?涓?榄?钖?鍙?闃?浣?娼?鏉?鎴?澶?閽?姹?鐢?浠?濮?鑼?鏂?鐭?濮?璋?寤?閭?鐔?閲?闄?閮?瀛?鐧?宕?搴?姣?閭?绉?姹?鍙?椤?渚?閭?瀛?榫?涓?娈?闆?閽?姹?灏?鏄?甯?姝?涔?璐?璧?榫?鏂?;

SET @male_names  = '浼?寮?纾?娑?楣?鍐?鍕?鏉?鏄?杈?瀹?娴?娲?鍗?缈?宄?鏂?鍑?瓒?浜?椋?寤?鍥?蹇?鍒?瀹?甯?鏃?榫?濞?鎭?姣?杩?杈?鍝?鏅?鐫?鐨?杞?閾?;
SET @female_names = '濞?鏁?闈?涓?濠?闆?鑺?濞?闇?鐜?鐕?钀?绾?鑾?鑹?鎱?棰?鍊?娲?涓?鐞?浣?鐟?钖?鎬?鐞?濯?濡?褰?钑?鎬?钀?钀?娑?濠?鑿?;
SET @origins = '灞变笢,娌冲崡,姹熻嫃,娴欐睙,骞夸笢,鍥涘窛,婀栧寳,瀹夊窘,婀栧崡,娌冲寳,闄曡タ,绂忓缓,杈藉畞,姹熻タ,灞辫タ,骞胯タ,浜戝崡,璐靛窞,鍚夋灄,榛戦緳姹?鐢樿們,鍐呰挋鍙?鏂扮枂,娴峰崡,瀹佸,闈掓捣,瑗胯棌,鍖椾含,涓婃捣,澶╂触,閲嶅簡';

DROP PROCEDURE IF EXISTS gen_real;
DELIMITER //
CREATE PROCEDURE gen_real(
    IN base_no BIGINT, IN ey INT, IN gid BIGINT,
    IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE sn INT;
    DECLARE nm VARCHAR(64);
    DECLARE g CHAR(1);
    DECLARE sv INT;
    DECLARE ori VARCHAR(32);
    WHILE i <= cnt DO
        SET g = IF(RAND()<0.48, '2', '1');
        SET nm = CONCAT(
            SUBSTRING_INDEX(SUBSTRING_INDEX(@surnames, ',', 1+FLOOR(RAND()*100)), ',', -1),
            IF(g='1',
                SUBSTRING_INDEX(SUBSTRING_INDEX(@male_names, ',', 1+FLOOR(RAND()*40)), ',', -1),
                SUBSTRING_INDEX(SUBSTRING_INDEX(@female_names, ',', 1+FLOOR(RAND()*36)), ',', -1)),
            IF(RAND()<0.35, SUBSTRING_INDEX(SUBSTRING_INDEX(@male_names, ',', 1+FLOOR(RAND()*20)), ',', -1), '')
        );
        SET ori = SUBSTRING_INDEX(SUBSTRING_INDEX(@origins, ',', 1+FLOOR(RAND()*31)), ',', -1);

        -- 0=閫€瀛?1=鍦ㄨ(鍚欢姣? 2=浼戝 3=姣曚笟
        IF ey = 2022 THEN
            SET sv = CASE
                WHEN i <= cnt * 0.93 THEN 3     -- 93% 姝ｅ父姣曚笟
                WHEN i <= cnt * 0.97 THEN 1     -- 4% 寤舵瘯=浠嶅湪璇?
                WHEN i <= cnt * 0.99 THEN 2     -- 2% 浼戝
                ELSE 0 END;                      -- 1% 閫€瀛?
        ELSE
            SET sv = 1;                          -- 2023-2025 鍏ㄩ儴鍦ㄨ
        END IF;

        INSERT INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i, nm, IF(g='1',1,2),
            -- 鍑虹敓骞翠唤=鍏ュ骞?18锛?鏈堝紑瀛﹀墠鍑虹敓鐨勭暐鏃?
            DATE_ADD(CONCAT(ey-18,'-09-01'), INTERVAL (i*23%365)-180 DAY),
            0,  -- 绮剧‘骞撮緞绋嶅悗鐢?TIMESTAMPDIFF 璁＄畻
            CONCAT(ori, '鍖?),
            gid, did, mid,
            CONCAT(cprefix, LPAD(1+(i-1)DIV 40, 2, '0')),
            ori, ey, sv
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- ============================================================
-- 鎸夐櫌绯荤敓鎴愶細姣忎釜闄㈢郴姣忕骇浜烘暟绗﹀悎瀹為檯鐑棬搴?
-- ============================================================

-- CS 姣忓勾绾?350锛堟渶澶ч櫌锛?
CALL gen_real(2022100000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢22',130);
CALL gen_real(2022102000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘22',110);
CALL gen_real(2022103000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁22',100);
CALL gen_real(2023100000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢23',140);
CALL gen_real(2023102000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘23',120);
CALL gen_real(2023103000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁23',110);
CALL gen_real(2024100000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢24',150);
CALL gen_real(2024102000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘24',130);
CALL gen_real(2024103000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁24',120);
CALL gen_real(2025100000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢25',160);
CALL gen_real(2025102000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘25',140);
CALL gen_real(2025103000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁25',130);

-- EE 姣忓勾绾?280
CALL gen_real(2022200000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊22',130);
CALL gen_real(2022202000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊22', 90);
CALL gen_real(2022203000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈22', 80);
CALL gen_real(2023200000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊23',140);
CALL gen_real(2023202000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊23',100);
CALL gen_real(2023203000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈23', 90);
CALL gen_real(2024200000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊24',150);
CALL gen_real(2024202000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊24',110);
CALL gen_real(2024203000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈24',100);
CALL gen_real(2025200000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊25',160);
CALL gen_real(2025202000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊25',120);
CALL gen_real(2025203000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈25',110);

-- EM 姣忓勾绾?270
CALL gen_real(2022300000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳22',110);
CALL gen_real(2022302000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ22', 80);
CALL gen_real(2022303000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺22', 70);
CALL gen_real(2023300000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳23',120);
CALL gen_real(2023302000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ23', 85);
CALL gen_real(2023303000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺23', 75);
CALL gen_real(2024300000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳24',130);
CALL gen_real(2024302000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ24', 90);
CALL gen_real(2024303000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺24', 80);
CALL gen_real(2025300000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳25',140);
CALL gen_real(2025302000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ25', 95);
CALL gen_real(2025303000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺25', 85);

-- ME 姣忓勾绾?260
CALL gen_real(2022400000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘22',120);
CALL gen_real(2022402000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締22', 80);
CALL gen_real(2022403000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?2', 60);
CALL gen_real(2023400000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘23',130);
CALL gen_real(2023402000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締23', 85);
CALL gen_real(2023403000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?3', 65);
CALL gen_real(2024400000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘24',140);
CALL gen_real(2024402000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締24', 90);
CALL gen_real(2024403000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?4', 70);
CALL gen_real(2025400000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘25',150);
CALL gen_real(2025402000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締25', 95);
CALL gen_real(2025403000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?5', 75);

-- CE 姣忓勾绾?180
CALL gen_real(2022500000,2022,@g22,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪22',110);
CALL gen_real(2022502000,2022,@g22,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョ22', 70);
CALL gen_real(2023500000,2023,@g23,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪23',120);
CALL gen_real(2023502000,2023,@g23,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョ23', 75);
CALL gen_real(2024500000,2024,@g24,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪24',130);
CALL gen_real(2024502000,2024,@g24,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョ24', 80);
CALL gen_real(2025500000,2025,@g25,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪25',140);
CALL gen_real(2025502000,2025,@g25,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョ25', 85);

-- Law 姣忓勾绾?160
CALL gen_real(2022600000,2022,@g22,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞22',110);
CALL gen_real(2022602000,2022,@g22,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇22', 50);
CALL gen_real(2023600000,2023,@g23,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞23',120);
CALL gen_real(2023602000,2023,@g23,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇23', 55);
CALL gen_real(2024600000,2024,@g24,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞24',130);
CALL gen_real(2024602000,2024,@g24,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇24', 60);
CALL gen_real(2025600000,2025,@g25,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞25',140);
CALL gen_real(2025602000,2025,@g25,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇25', 65);

-- 鍏朵綑 8 涓櫌绯绘瘡骞寸骇 100-150
-- Med (120/绾?
CALL gen_real(2022700000,2022,@g22,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥22',80);
CALL gen_real(2022702000,2022,@g22,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊22',50);
CALL gen_real(2023700000,2023,@g23,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥23',85);
CALL gen_real(2023702000,2023,@g23,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊23',55);
CALL gen_real(2024700000,2024,@g24,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥24',90);
CALL gen_real(2024702000,2024,@g24,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊24',60);
CALL gen_real(2025700000,2025,@g25,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥25',95);
CALL gen_real(2025702000,2025,@g25,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊25',65);

-- CH (110/绾?
CALL gen_real(2022800000,2022,@g22,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐22',70);
CALL gen_real(2022802000,2022,@g22,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲22',40);
CALL gen_real(2023800000,2023,@g23,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐23',75);
CALL gen_real(2023802000,2023,@g23,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲23',45);
CALL gen_real(2024800000,2024,@g24,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐24',80);
CALL gen_real(2024802000,2024,@g24,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲24',50);
CALL gen_real(2025800000,2025,@g25,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐25',85);
CALL gen_real(2025802000,2025,@g25,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲25',55);

-- LS (100/绾?
CALL gen_real(2022900000,2022,@g22,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧22',60);
CALL gen_real(2022902000,2022,@g22,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧22',40);
CALL gen_real(2023900000,2023,@g23,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧23',65);
CALL gen_real(2023902000,2023,@g23,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧23',45);
CALL gen_real(2024900000,2024,@g24,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧24',70);
CALL gen_real(2024902000,2024,@g24,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧24',50);
CALL gen_real(2025900000,2025,@g25,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧25',75);
CALL gen_real(2025902000,2025,@g25,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧25',55);

-- FL (100/绾?
CALL gen_real(2023000000,2022,@g22,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫22',60);
CALL gen_real(2023002000,2022,@g22,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ22',40);
CALL gen_real(2024000000,2023,@g23,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫23',65);
CALL gen_real(2024002000,2023,@g23,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ23',45);
CALL gen_real(2025000000,2024,@g24,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫24',70);
CALL gen_real(2025002000,2024,@g24,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ24',50);
CALL gen_real(2026000000,2025,@g25,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫25',75);
CALL gen_real(2026002000,2025,@g25,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ25',55);

-- EDU (100/绾?
CALL gen_real(2026100000,2022,@g22,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛22',60);
CALL gen_real(2026102000,2022,@g22,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠22',40);
CALL gen_real(2027100000,2023,@g23,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛23',65);
CALL gen_real(2027102000,2023,@g23,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠23',45);
CALL gen_real(2028100000,2024,@g24,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛24',70);
CALL gen_real(2028102000,2024,@g24,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠24',50);
CALL gen_real(2029100000,2025,@g25,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛25',75);
CALL gen_real(2029102000,2025,@g25,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠25',55);

-- JR (90/绾?
CALL gen_real(2030100000,2022,@g22,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈22',55);
CALL gen_real(2030102000,2022,@g22,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊22',35);
CALL gen_real(2031100000,2023,@g23,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈23',60);
CALL gen_real(2031102000,2023,@g23,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊23',40);
CALL gen_real(2032100000,2024,@g24,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈24',65);
CALL gen_real(2032102000,2024,@g24,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊24',45);
CALL gen_real(2033100000,2025,@g25,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈25',70);
CALL gen_real(2033102000,2025,@g25,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊25',50);

-- MS (90/绾?
CALL gen_real(2034100000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板22',40);
CALL gen_real(2034102000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻22',30);
CALL gen_real(2034103000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌22',25);
CALL gen_real(2035100000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板23',45);
CALL gen_real(2035102000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻23',35);
CALL gen_real(2035103000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌23',30);
CALL gen_real(2036100000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板24',50);
CALL gen_real(2036102000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻24',40);
CALL gen_real(2036103000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌24',35);
CALL gen_real(2037100000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板25',55);
CALL gen_real(2037102000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻25',45);
CALL gen_real(2037103000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌25',40);

-- AD (90/绾?
CALL gen_real(2038100000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶22',40);
CALL gen_real(2038102000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜22',30);
CALL gen_real(2038103000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟22',25);
CALL gen_real(2039100000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶23',45);
CALL gen_real(2039102000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜23',35);
CALL gen_real(2039103000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟23',30);
CALL gen_real(2040100000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶24',50);
CALL gen_real(2040102000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜24',40);
CALL gen_real(2040103000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟24',35);
CALL gen_real(2041100000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶25',55);
CALL gen_real(2041102000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜25',45);
CALL gen_real(2041103000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟25',40);

-- 闅忔満鎾掓瀬灏戜紤瀛?閫€瀛?
UPDATE student SET status = 2 WHERE status = 1 AND enroll_year >= 2023 ORDER BY RAND() LIMIT 10;
UPDATE student SET status = 0 WHERE status = 1 AND enroll_year >= 2023 ORDER BY RAND() LIMIT 8;
-- 绮剧‘璁＄畻骞撮緞锛堟寜 2026-07-17锛?
UPDATE student SET student_age = TIMESTAMPDIFF(YEAR, student_birth, '2026-07-17');
-- ~3% 瀛︾敓澶т竴宀?鍏ュ鏃?9宀?
UPDATE student SET student_birth = DATE_SUB(student_birth, INTERVAL 1 YEAR), student_age = student_age + 1
WHERE RAND() < 0.03;
-- 閲嶆柊绠楀勾榫?
UPDATE student SET student_age = TIMESTAMPDIFF(YEAR, student_birth, '2026-07-17');

-- ============================================================
-- 鎷涚敓璁″垝
-- ============================================================
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, y.year,
       CASE WHEN m.major_code IN ('CS01','CS02','CS03','EE01','EE02','EE03','EM01','EM03')
            THEN 100+FLOOR(RAND()*100)
            WHEN m.major_code IN ('ME01','ME02','CE01','MED01')
            THEN 70+FLOOR(RAND()*80)
            ELSE 40+FLOOR(RAND()*60) END AS plan,
       0, NULL
FROM major m CROSS JOIN (SELECT 2022 AS year UNION ALL SELECT 2023 UNION ALL SELECT 2024) y;

-- 鏍规嵁鐪熷疄瀛︾敓鏁板悓姝?
UPDATE enrollment e
JOIN (SELECT major_id, enroll_year AS year, COUNT(*) AS cnt FROM student WHERE status=1 GROUP BY major_id, enroll_year) s
    ON s.major_id=e.major_id AND s.year=e.year
SET e.actual_count=s.cnt, e.report_rate=ROUND(s.cnt/e.plan_count*100,2)
WHERE e.plan_count>0;

-- 娓呯悊
DROP PROCEDURE IF EXISTS gen_real;

SELECT '========== 鏈€缁堢粺璁?==========' AS '';
SELECT '骞寸骇', enroll_year, '鎬绘暟', COUNT(*),
       '鍦ㄨ', SUM(status=1), '姣曚笟', SUM(status=3),
       '浼戝', SUM(status=2), '閫€瀛?, SUM(status=0)
FROM student GROUP BY enroll_year ORDER BY enroll_year;

