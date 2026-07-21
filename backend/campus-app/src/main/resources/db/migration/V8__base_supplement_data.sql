-- 琛ュ厖瀛︾敓鑷崇渷灞為噸鐐瑰ぇ瀛﹁妯★紙~1.8 涓囷級
-- 鍦ㄥ凡鏈?7084 浜哄熀纭€涓婅拷鍔?~11000 浜?

SET @d_cs := (SELECT dept_id FROM department WHERE dept_code = 'CS');
SET @d_ee := (SELECT dept_id FROM department WHERE dept_code = 'EE');
SET @d_em := (SELECT dept_id FROM department WHERE dept_code = 'EM');
SET @d_me := (SELECT dept_id FROM department WHERE dept_code = 'ME');
SET @d_ce := (SELECT dept_id FROM department WHERE dept_code = 'CE');
SET @d_law := (SELECT dept_id FROM department WHERE dept_code = 'LAW');
SET @d_ch := (SELECT dept_id FROM department WHERE dept_code = 'CH');
SET @d_med := (SELECT dept_id FROM department WHERE dept_code = 'MED');
SET @d_ls := (SELECT dept_id FROM department WHERE dept_code = 'LS');
SET @d_fl := (SELECT dept_id FROM department WHERE dept_code = 'FL');
SET @d_edu := (SELECT dept_id FROM department WHERE dept_code = 'EDU');
SET @d_jr := (SELECT dept_id FROM department WHERE dept_code = 'JR');
SET @d_ms := (SELECT dept_id FROM department WHERE dept_code = 'MS');
SET @d_ad := (SELECT dept_id FROM department WHERE dept_code = 'AD');

SET @g2023 := (SELECT grade_id FROM grade WHERE grade_name = '2023绾? LIMIT 1);
SET @g2024 := (SELECT grade_id FROM grade WHERE grade_name = '2024绾? LIMIT 1);
SET @g2025 := (SELECT grade_id FROM grade WHERE grade_name = '2025绾? LIMIT 1);
SET @g2026 := (SELECT grade_id FROM grade WHERE grade_name = '2026绾? LIMIT 1);

DROP PROCEDURE IF EXISTS add_stu;
DELIMITER //
CREATE PROCEDURE add_stu(
    IN base_no BIGINT, IN gid BIGINT, IN ey INT,
    IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT,
    IN oris TEXT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total_ori INT;
    SET total_ori = CHAR_LENGTH(oris) - CHAR_LENGTH(REPLACE(oris, ',', '')) + 1;
    WHILE i <= cnt DO
        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i,
            CONCAT(ELT(1+FLOOR(RAND()*8),'寮?,'鐜?,'鏉?,'璧?,'闄?,'鍒?,'鏉?,'榛?,'鍛?,'鍚?,'瀛?,'鏈?),
                   ELT(1+FLOOR(RAND()*6),'瀛愯僵','闆ㄦ兜','娴╃劧','鎬濈惇','澶╁畤','姊︾懚','蹇楄繙','鑻ユ洣','鍗氭枃','鏅撳饯'),
                   ELT(1+FLOOR(RAND()*4),'','','','鎬?,'閾?,'杈?,'鐒?,'妗?,'鍝?,'鐞?,'鍗?,'钀?,'鐨?,'娆?,'娉?,'閫?)),
            IF(i%4=0,2,1),
            DATE_ADD('2006-06-01', INTERVAL (i*23 % 365) DAY),
            19 + ey - 2024,
            CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(oris, ',', 1 + ((i-1) % total_ori)), ',', -1), '鍖?),
            gid, did, mid,
            CONCAT(cprefix, LPAD(3 + (i-1) DIV 45, 2, '0')),
            SUBSTRING_INDEX(SUBSTRING_INDEX(oris, ',', 1 + ((i-1) % total_ori)), ',', -1),
            ey,
            CASE WHEN i <= cnt-2 THEN 1 WHEN RAND() < 0.3 THEN 0 ELSE 2 END
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- ============================================================
-- 澶ч櫌缈?2-3 鍊嶏細CS/EE/EM/ME 鍔犲ぇ閲?
-- ============================================================

-- CS (+3200: 姣忕骇+800)
CALL add_stu(2023200000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢23A',200,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,涓存矀,娴庡畞,娣勫崥,娉板畨,閮戝窞,娲涢槼,鏂颁埂,瀹夐槼,寮€灏?鍗楅槼,璁告槍');
CALL add_stu(2023201000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘23A',150,'鍖椾含,涓婃捣,娣卞湷,骞垮窞,鏉窞,鍗椾含,姝︽眽,鎴愰兘,瑗垮畨,閲嶅簡,澶╂触,鑻忓窞');
CALL add_stu(2023202000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁23A',100,'闀挎矙,鍚堣偉,鍗楁槍,绂忓窞,鍗楀畞,鏄嗘槑,璐甸槼,鍏板窞,澶師');
CALL add_stu(2024200000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢24A',220,'娴庡崡,闈掑矝,閮戝窞,瑗垮畨,姝︽眽,鎴愰兘,鍗椾含,鍚堣偉,闀挎矙,鐭冲搴?);
CALL add_stu(2024201000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘24A',160,'鍖椾含,涓婃捣,鏉窞,娣卞湷,骞垮窞,鍗椾含,鑻忓窞,鏃犻敗,瀹佹尝,鍘﹂棬');
CALL add_stu(2024202000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁24A',110,'鎴愰兘,閲嶅簡,姝︽眽,闀挎矙,瑗垮畨,鍗楁槍,绂忓窞,鍗楀畞,鏄嗘槑,璐甸槼');
CALL add_stu(2025200000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢25A',240,'鍗椾含,鏉窞,鑻忓窞,鏃犻敗,鍚堣偉,姝︽眽,鎴愰兘,瑗垮畨,娴庡崡,闈掑矝');
CALL add_stu(2025201000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘25A',170,'鍖椾含,涓婃捣,骞垮窞,娣卞湷,澶╂触,閲嶅簡,姝︽眽,鎴愰兘,鏉窞,鍗椾含');
CALL add_stu(2025202000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁25A',120,'瑗垮畨,鍏板窞,瑗垮畞,閾跺窛,涔岄瞾鏈ㄩ綈,澶師,鍛煎拰娴╃壒,娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?);
CALL add_stu(2026200000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'杞欢26A',260,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,鎯犲窞,涓北,姹熼棬,婀涙睙,姹曞ご');
CALL add_stu(2026201000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'鏅鸿兘26A',180,'涓婃捣,鏉窞,鍗椾含,鑻忓窞,鏃犻敗,瀹佹尝,鍚堣偉,姝︽眽,闀挎矙,鎴愰兘,閲嶅簡');
CALL add_stu(2026202000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'鏁版嵁26A',130,'鍖椾含,澶╂触,鐭冲搴?娴庡崡,闈掑矝,閮戝窞,瑗垮畨,娌堥槼,澶ц繛,鍝堝皵婊?闀挎槬');

-- EM (+1800: 姣忕骇+450)
CALL add_stu(2023203000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳23A',130,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹熼棬,鑲囧簡,姹曞ご,鎻槼');
CALL add_stu(2023204000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ23A', 90,'鏉窞,瀹佹尝,娓╁窞,鍢夊叴,缁嶅叴,閲戝崕,鍙板窞,婀栧窞,琛㈠窞,涓芥按,鑸熷北');
CALL add_stu(2023205000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺23A', 80,'涓婃捣,鍖椾含,娣卞湷,骞垮窞,鏉窞,鍗椾含,鑻忓窞,鎴愰兘,姝︽眽,澶╂触,閲嶅簡');
CALL add_stu(2024203000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳24A',140,'閮戝窞,娲涢槼,寮€灏?鏂颁埂,瀹夐槼,鍟嗕笜,鍗楅槼,璁告槍,骞抽《灞?鐒︿綔,鍛ㄥ彛');
CALL add_stu(2024204000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ24A', 95,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?閾滈櫟,瀹夊簡,闃滈槼,瀹垮窞,婊佸窞,瀹ｅ煄');
CALL add_stu(2024205000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺24A', 85,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄,寰愬窞');
CALL add_stu(2025203000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳25A',150,'鎴愰兘,缁甸槼,寰烽槼,瀹滃,鍗楀厖,娉稿窞,杈惧窞,涔愬北,鐪夊北,鑷础,鍐呮睙');
CALL add_stu(2025204000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ25A',100,'鍗楁槍,涔濇睙,璧ｅ窞,鍚夊畨,瀹滄槬,鎶氬窞,涓婇ザ,鏅痉闀?钀嶄埂');
CALL add_stu(2025205000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺25A', 90,'绂忓窞,鍘﹂棬,娉夊窞,婕冲窞,榫欏博,涓夋槑,鍗楀钩,瀹佸痉,鑾嗙敯');
CALL add_stu(2026203000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'浼氳26A',160,'姝︽眽,榛勭煶,瀹滄槍,瑗勯槼,鑽嗗窞,鍗佸牥,瀛濇劅,榛勫唸,鍜稿畞,鎭╂柦,闅忓窞');
CALL add_stu(2026204000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'宸ョ26A',105,'闀挎矙,鏍床,琛￠槼,宀抽槼,甯稿痉,閮村窞,姘稿窞,閭甸槼,鐩婇槼,鎬€鍖?濞勫簳');
CALL add_stu(2026205000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'閲戣瀺26A', 95,'鏄嗘槑,鏇查潠,鐜夋邯,澶х悊,淇濆北,绾㈡渤,鏂囧北,鏅幢,瑗垮弻鐗堢撼');

-- EE (+1500: 姣忕骇+375)
CALL add_stu(2023206000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊23A',120,'鎴愰兘,缁甸槼,寰烽槼,瀹滃,鍗楀厖,娉稿窞,鍐呮睙,鑷础,鐪夊北,骞垮厓,閬傚畞,杈惧窞');
CALL add_stu(2023207000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊23A', 80,'瑗垮畨,鍜搁槼,瀹濋浮,娓崡,姹変腑,寤跺畨,姒嗘灄,瀹夊悍,鍟嗘礇,閾滃窛');
CALL add_stu(2023208000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈23A', 70,'娣卞湷,骞垮窞,鏉窞,鍗椾含,姝︽眽,鎴愰兘,鍖椾含,涓婃捣,鑻忓窞,鏃犻敗');
CALL add_stu(2024206000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊24A',130,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,瀛濇劅,榛勭煶,鍗佸牥,鍜稿畞,鎭╂柦,鑽嗛棬');
CALL add_stu(2024207000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊24A', 90,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,寰愬窞,鍗楅€?鎵窞,闀囨睙,鐩愬煄,娉板窞');
CALL add_stu(2024208000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈24A', 75,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?瀹夊簡,閾滈櫟,闃滈槼,婊佸窞,鍏畨,瀹垮窞');
CALL add_stu(2025206000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊25A',140,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,娣勫崥,娉板畨,娴庡畞,涓存矀,婊ㄥ窞,涓滆惀,濞佹捣');
CALL add_stu(2025207000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊25A', 95,'闀挎矙,鏍床,婀樻江,宀抽槼,琛￠槼,甯稿痉,閮村窞,姘稿窞,閭甸槼,濞勫簳,鎬€鍖?);
CALL add_stu(2025208000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈25A', 80,'閮戝窞,娲涢槼,鏂颁埂,瀹夐槼,寮€灏?鍟嗕笜,鍗楅槼,璁告槍,鍛ㄥ彛,骞抽《灞?);
CALL add_stu(2026206000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'鐢典俊26A',150,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹曞ご,姹熼棬,婀涙睙,鑼傚悕');
CALL add_stu(2026207000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'閫氫俊26A',100,'鎴愰兘,閲嶅簡,瑗垮畨,鍏板窞,瑗垮畞,閾跺窛,涔岄瞾鏈ㄩ綈,鎷夎惃');
CALL add_stu(2026208000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'鐗╄仈26A', 85,'鍖椾含,澶╂触,鐭冲搴?澶師,鍛煎拰娴╃壒,娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?);

-- ME (+1400: 姣忕骇+350)
CALL add_stu(2023209000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘23A',130,'娌堥槼,澶ц繛,闉嶅北,鎶氶『,鏈邯,涓逛笢,閿﹀窞,钀ュ彛,闃滄柊,杈介槼,鐩橀敠');
CALL add_stu(2023210000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締23A', 80,'闀挎槬,鍚夋灄,鍥涘钩,杈芥簮,閫氬寲,鐧藉北,鏉惧師,鐧藉煄,寤惰竟');
CALL add_stu(2023211000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?3A', 60,'鍝堝皵婊?榻愰綈鍝堝皵,鐗′腹姹?浣虫湪鏂?澶у簡,楦¤タ,楣ゅ矖,浼婃槬,榛戞渤');
CALL add_stu(2024209000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘24A',140,'娴庡崡,闈掑矝,娣勫崥,鏋ｅ簞,涓滆惀,鐑熷彴,娼嶅潑,娴庡畞,娉板畨,濞佹捣,鏃ョ収');
CALL add_stu(2024210000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締24A', 85,'閮戝窞,娲涢槼,寮€灏?骞抽《灞?瀹夐槼,楣ゅ,鏂颁埂,鐒︿綔,婵槼,璁告槍');
CALL add_stu(2024211000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?4A', 65,'闀挎矙,鏍床,婀樻江,琛￠槼,閭甸槼,宀抽槼,甯稿痉,鐩婇槼,閮村窞,姘稿窞');
CALL add_stu(2025209000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘25A',150,'姝︽眽,榛勭煶,瀹滄槍,瑗勯槼,閯傚窞,鑽嗛棬,瀛濇劅,鑽嗗窞,榛勫唸,鍜稿畞,鍗佸牥,闅忓窞');
CALL add_stu(2025210000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締25A', 90,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?娣寳,閾滈櫟,瀹夊簡,闃滈槼,瀹垮窞,婊佸窞');
CALL add_stu(2025211000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?5A', 70,'鎴愰兘,鑷础,鏀€鏋濊姳,娉稿窞,寰烽槼,缁甸槼,骞垮厓,閬傚畞,鍐呮睙,涔愬北');
CALL add_stu(2026209000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'鏈烘26A',160,'鍗椾含,鏃犻敗,鑻忓窞,甯稿窞,鍗楅€?寰愬窞,鎵窞,闀囨睙,娉板窞,鐩愬煄,杩炰簯娓?);
CALL add_stu(2026210000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'杞﹁締26A', 95,'鍗楁槍,鏅痉闀?钀嶄埂,涔濇睙,鏂颁綑,楣版江,璧ｅ窞,鍚夊畨,瀹滄槬,鎶氬窞');
CALL add_stu(2026211000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'鏅洪€?6A', 75,'瑗垮畨,瀹濋浮,鍜搁槼,娓崡,寤跺畨,姹変腑,瀹夊悍,鍟嗘礇,姒嗘灄,閾滃窛');

-- ============================================================
-- 涓櫌缈诲€嶏細CE/Law/CH/Med/LS 鍔犻€傞噺
-- ============================================================

-- CE (+600: 姣忕骇+150)
CALL add_stu(2023213000,@g2023,2023,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪23A',80,'閲嶅簡,涓囧窞,娑櫟,娓濅腑,姹熷寳,娌欏潽鍧?鍗楀哺,鍖楃,宸村崡');
CALL add_stu(2023214000,@g2023,2023,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョCE23A',50,'璐甸槼,閬典箟,鍏洏姘?瀹夐『,姣曡妭,閾滀粊,鍏翠箟,鍑噷');
CALL add_stu(2024213000,@g2024,2024,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪24A',85,'鏄嗘槑,鏇查潠,鐜夋邯,淇濆北,鏄€?涓芥睙,鏅幢,涓存钵');
CALL add_stu(2024214000,@g2024,2024,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョCE24A',55,'鍗楀畞,鏌冲窞,妗傛灄,姊у窞,鍖楁捣,閽﹀窞,璐垫腐,鐜夋灄');
CALL add_stu(2025213000,@g2025,2025,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪25A',90,'鎴愰兘,缁甸槼,寰烽槼,鍗楀厖,杈惧窞,瀹滃,娉稿窞,涔愬北,鑷础,鍐呮睙');
CALL add_stu(2025214000,@g2025,2025,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョCE25A',60,'瑗垮畨,瀹濋浮,鍜搁槼,娓崡,寤跺畨,姹変腑,姒嗘灄,瀹夊悍,鍟嗘礇,閾滃窛');
CALL add_stu(2026213000,@g2026,2026,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'鍦熸湪26A',95,'姝︽眽,瀹滄槍,瑗勯槼,鍗佸牥,鑽嗗窞,榛勫唸,瀛濇劅,鎭╂柦,鍜稿畞,榛勭煶');
CALL add_stu(2026214000,@g2026,2026,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'宸ョCE26A',65,'闀挎矙,鏍床,琛￠槼,宀抽槼,甯稿痉,閮村窞,姘稿窞,閭甸槼,鐩婇槼,鎬€鍖?);

-- Law (+500: 姣忕骇+125)
CALL add_stu(2023216000,@g2023,2023,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞23A',60,'鍖椾含,涓婃捣,骞垮窞,娣卞湷,鏉窞,鍗椾含,姝︽眽,鎴愰兘,瑗垮畨,娴庡崡');
CALL add_stu(2023217000,@g2023,2023,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇23A',35,'澶╂触,閲嶅簡,鐭冲搴?娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?闈掑矝,鍘﹂棬');
CALL add_stu(2024216000,@g2024,2024,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞24A',65,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,濞佹捣,鏃ョ収,涓存矀,寰峰窞,娣勫崥,娉板畨');
CALL add_stu(2024217000,@g2024,2024,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇24A',40,'姝︽眽,闀挎矙,鍗楁槍,鍚堣偉,绂忓窞,鍗楀畞,娴峰彛,鏄嗘槑,璐甸槼');
CALL add_stu(2025216000,@g2025,2025,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞25A',70,'鍗椾含,鑻忓窞,鏃犻敗,鏉窞,瀹佹尝,娓╁窞,鍘﹂棬,闈掑矝,澶ц繛,瑗垮畨');
CALL add_stu(2025217000,@g2025,2025,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇25A',45,'鎴愰兘,閲嶅簡,瑗垮畨,鍏板窞,瑗垮畞,閾跺窛,涔岄瞾鏈ㄩ綈');
CALL add_stu(2026216000,@g2026,2026,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'娉曞26A',75,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹熼棬,姹曞ご,婀涙睙');
CALL add_stu(2026217000,@g2026,2026,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'鐭ヤ骇26A',50,'閮戝窞,娲涢槼,寮€灏?鏂颁埂,瀹夐槼,鍟嗕笜,鍗楅槼,璁告槍,骞抽《灞?);

-- CH (+400: 姣忕骇+100)
CALL add_stu(2023219000,@g2023,2023,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐23A',55,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄');
CALL add_stu(2023220000,@g2023,2023,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲23A',35,'鏉窞,瀹佹尝,娓╁窞,鍢夊叴,缁嶅叴,閲戝崕,鍙板窞,婀栧窞,琛㈠窞');
CALL add_stu(2024219000,@g2024,2024,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐24A',60,'娴庡崡,闈掑矝,娣勫崥,涓滆惀,鐑熷彴,娼嶅潑,娴庡畞,娉板畨,濞佹捣,鏃ョ収');
CALL add_stu(2024220000,@g2024,2024,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲24A',40,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?娣寳,閾滈櫟,瀹夊簡,婊佸窞,闃滈槼');
CALL add_stu(2025219000,@g2025,2025,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐25A',65,'姝︽眽,榛勭煶,瀹滄槍,瑗勯槼,瀛濇劅,鑽嗗窞,榛勫唸,閯傚窞,鍗佸牥');
CALL add_stu(2025220000,@g2025,2025,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲25A',45,'闀挎矙,琛￠槼,鏍床,婀樻江,宀抽槼,甯稿痉,閮村窞,姘稿窞,閭甸槼');
CALL add_stu(2026219000,@g2026,2026,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'鍖栧伐26A',70,'鎴愰兘,寰烽槼,缁甸槼,鍗楀厖,瀹滃,娉稿窞,涔愬北,鑷础,鐪夊北,鍐呮睙');
CALL add_stu(2026220000,@g2026,2026,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'搴斿寲26A',50,'瑗垮畨,瀹濋浮,鍜搁槼,寤跺畨,姹変腑,姒嗘灄,娓崡,瀹夊悍,鍟嗘礇,閾滃窛');

-- Med (+350: 姣忕骇+90)
CALL add_stu(2023222000,@g2023,2023,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥23A',55,'閲嶅簡,鎴愰兘,璐甸槼,鏄嗘槑,瑗垮畨,姝︽眽,闀挎矙,骞垮窞,鍗楀畞');
CALL add_stu(2023223000,@g2023,2023,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊23A',40,'娴庡崡,闈掑矝,閮戝窞,鍚堣偉,鍗楁槍,绂忓窞,鏉窞,鍗椾含,涓婃捣');
CALL add_stu(2024222000,@g2024,2024,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥24A',60,'鎴愰兘,缁甸槼,寰烽槼,鍗楀厖,瀹滃,娉稿窞,鑷础,鍐呮睙,鐪夊北');
CALL add_stu(2024223000,@g2024,2024,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊24A',45,'姝︽眽,闀挎矙,鍗楁槍,鍚堣偉,鍗椾含,鏉窞,绂忓窞,鍘﹂棬,骞垮窞');
CALL add_stu(2025222000,@g2025,2025,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥25A',65,'鍖椾含,澶╂触,鐭冲搴?澶師,鍛煎拰娴╃壒,娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?);
CALL add_stu(2025223000,@g2025,2025,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊25A',50,'瑗垮畨,鍏板窞,瑗垮畞,閾跺窛,涔岄瞾鏈ㄩ綈,鎷夎惃,鏄嗘槑,璐甸槼,鍗楀畞');
CALL add_stu(2026222000,@g2026,2026,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'涓村簥26A',70,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,姹熼棬,婀涙睙,鑼傚悕,姹曞ご');
CALL add_stu(2026223000,@g2026,2026,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'鎶ょ悊26A',55,'涓婃捣,鍖椾含,澶╂触,閲嶅簡,鏉窞,鍗椾含,姝︽眽,鎴愰兘,瑗垮畨,闀挎矙');

-- LS (+350: 姣忕骇+90)
CALL add_stu(2023225000,@g2023,2023,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧23A',55,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,瀛濇劅,鍜稿畞,鎭╂柦,榛勭煶');
CALL add_stu(2023226000,@g2023,2023,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧23A',40,'闀挎矙,鏍床,琛￠槼,宀抽槼,甯稿痉,婀樻江,閮村窞,姘稿窞,閭甸槼');
CALL add_stu(2024225000,@g2024,2024,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧24A',60,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,寰愬窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄');
CALL add_stu(2024226000,@g2024,2024,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧24A',45,'鏉窞,瀹佹尝,娓╁窞,鍢夊叴,缁嶅叴,閲戝崕,鍙板窞,婀栧窞,琛㈠窞');
CALL add_stu(2025225000,@g2025,2025,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧25A',65,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,娣勫崥,娉板畨,娴庡畞,涓存矀,濞佹捣,鏃ョ収');
CALL add_stu(2025226000,@g2025,2025,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧25A',50,'鎴愰兘,缁甸槼,寰烽槼,瀹滃,鍗楀厖,娉稿窞,涔愬北,鑷础,鍐呮睙');
CALL add_stu(2026225000,@g2026,2026,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'鐢熸妧26A',70,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?瀹夊簡,闃滈槼,婊佸窞,鍏畨,瀹ｅ煄');
CALL add_stu(2026226000,@g2026,2026,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'椋熷搧26A',55,'鍗楁槍,涔濇睙,璧ｅ窞,鍚夊畨,瀹滄槬,涓婇ザ,鎶氬窞,鏅痉闀?钀嶄埂');

-- ============================================================
-- 灏忛櫌鍔犲皯璁?
-- ============================================================
-- FL (+300: 姣忕骇+75)
CALL add_stu(2023228000,@g2023,2023,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫23A',45,'涓婃捣,鍖椾含,骞垮窞,娣卞湷,鏉窞,鍗椾含,鎴愰兘,閲嶅簡,姝︽眽,闀挎矙');
CALL add_stu(2023229000,@g2023,2023,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ23A',30,'澶ц繛,闈掑矝,鍘﹂棬,鑻忓窞,鏃犻敗,瀹佹尝,澶╂触,瑗垮畨,娌堥槼');
CALL add_stu(2024228000,@g2024,2024,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫24A',50,'娴庡崡,闈掑矝,鐑熷彴,濞佹捣,鏃ョ収,娼嶅潑,娣勫崥,娉板畨,娴庡畞,涓存矀');
CALL add_stu(2024229000,@g2024,2024,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ24A',35,'绂忓窞,鍘﹂棬,娉夊窞,婕冲窞,榫欏博,涓夋槑,鍗楀钩,鑾嗙敯,瀹佸痉');
CALL add_stu(2025228000,@g2025,2025,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫25A',55,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄,寰愬窞');
CALL add_stu(2025229000,@g2025,2025,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ25A',40,'骞垮窞,娣卞湷,鐝犳捣,涓滆帪,浣涘北,涓北,鎯犲窞,姹曞ご,婀涙睙');
CALL add_stu(2026228000,@g2026,2026,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'鑻辫26A',60,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,瀛濇劅,榛勭煶,鍗佸牥,鍜稿畞');
CALL add_stu(2026229000,@g2026,2026,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'鏃ヨ26A',45,'鎴愰兘,閲嶅簡,鏄嗘槑,璐甸槼,鍗楀畞,娴峰彛,鎷夎惃,瑗垮畨,鍏板窞');

-- EDU (+300: 姣忕骇+75)
CALL add_stu(2023231000,@g2023,2023,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛23A',45,'閮戝窞,娲涢槼,寮€灏?鏂颁埂,瀹夐槼,鍟嗕笜,鍗楅槼,淇￠槼,鍛ㄥ彛');
CALL add_stu(2023232000,@g2023,2023,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠23A',35,'姝︽眽,闀挎矙,鍗楁槍,鍚堣偉,鍗椾含,鏉窞,绂忓窞,娴庡崡');
CALL add_stu(2024231000,@g2024,2024,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛24A',50,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,娣勫崥,娉板畨,娴庡畞,涓存矀,濞佹捣,鏃ョ収');
CALL add_stu(2024232000,@g2024,2024,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠24A',40,'鎴愰兘,閲嶅簡,璐甸槼,鏄嗘槑,瑗垮畨,鍏板窞,瑗垮畞,閾跺窛');
CALL add_stu(2025231000,@g2025,2025,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛25A',55,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?瀹夊簡,闃滈槼,婊佸窞,鍏畨,瀹ｅ煄');
CALL add_stu(2025232000,@g2025,2025,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠25A',45,'鐭冲搴?鍞愬北,閭兏,淇濆畾,绉︾殗宀?娌у窞,寤婂潑,寮犲鍙?);
CALL add_stu(2026231000,@g2026,2026,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'鏁欒偛26A',60,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹曞ご,婀涙睙');
CALL add_stu(2026232000,@g2026,2026,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'瀛﹀墠26A',50,'鏄嗘槑,鏇查潠,鐜夋邯,澶х悊,淇濆北,绾㈡渤,鏅幢,鏂囧北');

-- JR (+250: 姣忕骇+60)
CALL add_stu(2023234000,@g2023,2023,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈23A',35,'鍖椾含,涓婃捣,骞垮窞,娣卞湷,鏉窞,鍗椾含,姝︽眽,鎴愰兘,瑗垮畨');
CALL add_stu(2023235000,@g2023,2023,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊23A',25,'闀挎矙,閲嶅簡,澶╂触,閮戝窞,娴庡崡,闈掑矝,鍚堣偉,鍗楁槍');
CALL add_stu(2024234000,@g2024,2024,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈24A',40,'绂忓窞,鍘﹂棬,娉夊窞,婕冲窞,榫欏博,涓夋槑,鍗楀钩,鑾嗙敯,瀹佸痉');
CALL add_stu(2024235000,@g2024,2024,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊24A',30,'娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?鍚夋灄,鐗′腹姹?澶у簡');
CALL add_stu(2025234000,@g2025,2025,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈25A',45,'鎴愰兘,缁甸槼,寰烽槼,瀹滃,鍗楀厖,娉稿窞,涔愬北,鑷础,鐪夊北');
CALL add_stu(2025235000,@g2025,2025,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊25A',35,'鍗楁槍,涔濇睙,璧ｅ窞,鍚夊畨,涓婇ザ,鏅痉闀?瀹滄槬,鎶氬窞');
CALL add_stu(2026234000,@g2026,2026,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'鏂伴椈26A',50,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,瀛濇劅,榛勭煶,鍗佸牥,鍜稿畞');
CALL add_stu(2026235000,@g2026,2026,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'缃戞柊26A',40,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?瀹夊簡,闃滈槼,婊佸窞,鍏畨');

-- MS (+250: 姣忕骇+60)
CALL add_stu(2023237000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板23A',35,'鍗椾含,鏉窞,鍚堣偉,娴庡崡,姝︽眽,闀挎矙,鎴愰兘');
CALL add_stu(2023238000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻23A',25,'涓婃捣,鍖椾含,娣卞湷,骞垮窞,瑗垮畨,閮戝窞,鍗楁槍');
CALL add_stu(2023239000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌23A',20,'鑻忓窞,鏃犻敗,瀹佹尝,鍘﹂棬,绂忓窞,闈掑矝,澶ц繛');
CALL add_stu(2024237000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板24A',40,'娴庡崡,闈掑矝,鐑熷彴,娼嶅潑,娣勫崥,娉板畨,娴庡畞,涓存矀');
CALL add_stu(2024238000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻24A',30,'姝︽眽,瀹滄槍,瑗勯槼,鑽嗗窞,榛勫唸,瀛濇劅,鍜稿畞,鍗佸牥');
CALL add_stu(2024239000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌24A',25,'闀挎矙,鏍床,琛￠槼,宀抽槼,甯稿痉,婀樻江,閮村窞,姘稿窞');
CALL add_stu(2025237000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板25A',45,'鎴愰兘,缁甸槼,寰烽槼,鍗楀厖,瀹滃,娉稿窞,涔愬北,鑷础,鐪夊北');
CALL add_stu(2025238000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻25A',35,'閮戝窞,娲涢槼,寮€灏?鏂颁埂,瀹夐槼,鍟嗕笜,鍗楅槼,璁告槍,鍛ㄥ彛');
CALL add_stu(2025239000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌25A',30,'鍚堣偉,鑺滄箹,铓屽煚,椹瀺灞?瀹夊簡,闃滈槼,婊佸窞,瀹ｅ煄');
CALL add_stu(2026237000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'鏁板26A',50,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鎯犲窞,姹曞ご,婀涙睙');
CALL add_stu(2026238000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'缁熻26A',40,'鍗椾含,鑻忓窞,鏃犻敗,甯稿窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄');
CALL add_stu(2026239000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'鏁拌26A',35,'鍗楁槍,涔濇睙,璧ｅ窞,鍚夊畨,瀹滄槬,涓婇ザ,鏅痉闀?鎶氬窞');

-- AD (+250: 姣忕骇+60)
CALL add_stu(2023241000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶23A',35,'涓婃捣,鍖椾含,鏉窞,鎴愰兘,閲嶅簡,姝︽眽,鍗椾含,骞垮窞,娣卞湷');
CALL add_stu(2023242000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜23A',25,'鑻忓窞,鏃犻敗,瀹佹尝,鍘﹂棬,闈掑矝,澶ц繛,闀挎矙,瑗垮畨');
CALL add_stu(2023243000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟23A',20,'澶╂触,娌堥槼,娴庡崡,鍚堣偉,鍗楁槍,绂忓窞,鏄嗘槑,鍗楀畞');
CALL add_stu(2024241000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶24A',40,'鎴愰兘,閲嶅簡,瑗垮畨,鏄嗘槑,璐甸槼,鍗楀畞,闀挎矙,姝︽眽,閮戝窞');
CALL add_stu(2024242000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜24A',30,'鏉窞,鍗椾含,鑻忓窞,鏃犻敗,瀹佹尝,娓╁窞,鍢夊叴,缁嶅叴');
CALL add_stu(2024243000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟24A',25,'骞垮窞,娣卞湷,涓滆帪,浣涘北,鐝犳捣,涓北,鍘﹂棬,绂忓窞');
CALL add_stu(2025241000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶25A',45,'鍖椾含,涓婃捣,娣卞湷,鏉窞,鍗椾含,鎴愰兘,閲嶅簡,姝︽眽,闀挎矙,骞垮窞');
CALL add_stu(2025242000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜25A',35,'瑗垮畨,閮戝窞,娴庡崡,闈掑矝,鍚堣偉,鍗楁槍,绂忓窞,鍘﹂棬,澶ц繛');
CALL add_stu(2025243000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟25A',30,'鑻忓窞,鏃犻敗,甯稿窞,鍗楅€?鎵窞,闀囨睙,娉板窞,鐩愬煄,寰愬窞');
CALL add_stu(2026241000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'瑙嗕紶26A',50,'涓婃捣,鍖椾含,娣卞湷,鏉窞,鍗椾含,鎴愰兘,閲嶅簡,姝︽眽,闀挎矙,骞垮窞');
CALL add_stu(2026242000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'鐜26A',40,'瑗垮畨,閮戝窞,娴庡崡,闈掑矝,鍚堣偉,鍗楁槍,绂忓窞,鍘﹂棬');
CALL add_stu(2026243000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'鏁板獟26A',35,'娌堥槼,澶ц繛,闀挎槬,鍝堝皵婊?澶╂触,鐭冲搴?澶師');

-- ============================================================
-- 鏇存柊鎷涚敓璁″垝
-- ============================================================
UPDATE enrollment e
JOIN (
    SELECT major_id, enroll_year AS year, COUNT(*) AS cnt
    FROM student WHERE status = 1
    GROUP BY major_id, enroll_year
) s ON s.major_id = e.major_id AND s.year = e.year
SET e.actual_count = s.cnt,
    e.report_rate = ROUND(s.cnt / e.plan_count * 100, 2)
WHERE e.plan_count > 0;

DROP PROCEDURE IF EXISTS add_stu;
SELECT '杩藉姞鍚庡鐢熸€绘暟: ', COUNT(*) FROM student;

