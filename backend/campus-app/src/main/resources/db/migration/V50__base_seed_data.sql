-- ============================================
-- 鎴愬憳 D锛氬熀纭€鏁版嵁绉嶅瓙锛堝宸插缓搴撴墽琛屼竴娆★紱涓?baseline/init.sql 鏈熬绉嶅瓙涓€鑷达級
-- 瑕嗙洊锛歞epartment / major / grade / user(鏁欒亴宸ユ。妗? / student / enrollment / news / forum_*
-- 渚濊禆锛歏20260717100000__base_profile_columns.sql
-- ============================================


-- ---------- 闄㈢郴 ----------
INSERT INTO `department` (`dept_name`, `dept_code`, `description`) VALUES
    ('璁＄畻鏈轰笌浜哄伐鏅鸿兘瀛﹂櫌', 'CS', '璁炬湁杞欢宸ョ▼銆佷汉宸ユ櫤鑳界瓑涓撲笟锛屽缓鏈夌渷绾ч噸鐐瑰疄楠屽銆?),
    ('缁忔祹绠＄悊瀛﹂櫌', 'EM', '娑电洊浼氳瀛︺€佸伐鍟嗙鐞嗙瓑涓撲笟锛屾敞閲嶄骇鏁欒瀺鍚堛€?),
    ('澶栧浗璇闄?, 'FL', '寮€璁捐嫳璇€佹棩璇瓑涓撲笟锛屽煿鍏诲鍚堝瀷澶栬浜烘墠銆?),
    ('鏈烘宸ョ▼瀛﹂櫌', 'ME', '浠ユ櫤鑳藉埗閫犱负鐗硅壊锛岃鏈夋満姊拌璁°€佽溅杈嗗伐绋嬬瓑涓撲笟銆?),
    ('鏁板涓庣粺璁″闄?, 'MS', '鍩虹瀛︾瀛﹂櫌锛岃鏈夋暟瀛︿笌搴旂敤鏁板銆佺粺璁″涓撲笟銆?),
    ('鑹烘湳璁捐瀛﹂櫌', 'AD', '璁炬湁瑙嗚浼犺揪璁捐銆佺幆澧冭璁＄瓑涓撲笟銆?)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`), `description` = VALUES(`description`);

SET @d_cs := (SELECT dept_id FROM department WHERE dept_code = 'CS');
SET @d_em := (SELECT dept_id FROM department WHERE dept_code = 'EM');
SET @d_fl := (SELECT dept_id FROM department WHERE dept_code = 'FL');
SET @d_me := (SELECT dept_id FROM department WHERE dept_code = 'ME');
SET @d_ms := (SELECT dept_id FROM department WHERE dept_code = 'MS');
SET @d_ad := (SELECT dept_id FROM department WHERE dept_code = 'AD');

-- ---------- 涓撲笟 ----------
INSERT INTO `major` (`dept_id`, `major_name`, `major_code`, `cultivation_plan`) VALUES
    (@d_cs, '杞欢宸ョ▼', 'CS01', '鍩瑰吇鎺屾彙杞欢寮€鍙戝叏娴佺▼鑳藉姏鐨勯珮绾у伐绋嬩汉鎵嶃€?),
    (@d_cs, '浜哄伐鏅鸿兘', 'CS02', '鍩瑰吇鍏峰鏈哄櫒瀛︿範涓庢櫤鑳界郴缁熺爺鍙戣兘鍔涚殑浜烘墠銆?),
    (@d_em, '浼氳瀛?, 'EM01', '鍩瑰吇鐔熸倝璐㈠姟浼氳涓庡璁″疄鍔＄殑搴旂敤鍨嬩汉鎵嶃€?),
    (@d_em, '宸ュ晢绠＄悊', 'EM02', '鍩瑰吇鍏峰鐜颁唬浼佷笟绠＄悊鑳藉姏鐨勫鍚堝瀷浜烘墠銆?),
    (@d_fl, '鑻辫', 'FL01', '鍩瑰吇鍏锋湁鎵庡疄鑻辫鍔熷簳鐨勭炕璇戜笌鏁欏浜烘墠銆?),
    (@d_fl, '鏃ヨ', 'FL02', '鍩瑰吇闈㈠悜鍥介檯浜ゆ祦鐨勬棩璇簲鐢ㄤ汉鎵嶃€?),
    (@d_me, '鏈烘璁捐鍒堕€犲強鍏惰嚜鍔ㄥ寲', 'ME01', '鍩瑰吇闈㈠悜鏅鸿兘鍒堕€犵殑鏈烘宸ョ▼浜烘墠銆?),
    (@d_me, '杞﹁締宸ョ▼', 'ME02', '鍩瑰吇鏂拌兘婧愭苯杞︽柟鍚戠殑宸ョ▼鎶€鏈汉鎵嶃€?),
    (@d_ms, '鏁板涓庡簲鐢ㄦ暟瀛?, 'MS01', '鍩瑰吇鍏峰鎵庡疄鏁板鍩虹鐨勭爺绌朵笌搴旂敤浜烘墠銆?),
    (@d_ms, '缁熻瀛?, 'MS02', '鍩瑰吇鎺屾彙鏁版嵁鍒嗘瀽鏂规硶鐨勭粺璁′汉鎵嶃€?),
    (@d_ad, '瑙嗚浼犺揪璁捐', 'AD01', '鍩瑰吇鍝佺墝涓庢暟瀛楀獟浣撴柟鍚戠殑璁捐浜烘墠銆?),
    (@d_ad, '鐜璁捐', 'AD02', '鍩瑰吇绌洪棿涓庢櫙瑙傛柟鍚戠殑璁捐浜烘墠銆?)
ON DUPLICATE KEY UPDATE `major_name` = VALUES(`major_name`), `dept_id` = VALUES(`dept_id`);

SET @m_cs01 := (SELECT major_id FROM major WHERE major_code = 'CS01');
SET @m_cs02 := (SELECT major_id FROM major WHERE major_code = 'CS02');
SET @m_em01 := (SELECT major_id FROM major WHERE major_code = 'EM01');
SET @m_em02 := (SELECT major_id FROM major WHERE major_code = 'EM02');
SET @m_fl01 := (SELECT major_id FROM major WHERE major_code = 'FL01');
SET @m_fl02 := (SELECT major_id FROM major WHERE major_code = 'FL02');
SET @m_me01 := (SELECT major_id FROM major WHERE major_code = 'ME01');
SET @m_me02 := (SELECT major_id FROM major WHERE major_code = 'ME02');
SET @m_ms01 := (SELECT major_id FROM major WHERE major_code = 'MS01');
SET @m_ms02 := (SELECT major_id FROM major WHERE major_code = 'MS02');
SET @m_ad01 := (SELECT major_id FROM major WHERE major_code = 'AD01');
SET @m_ad02 := (SELECT major_id FROM major WHERE major_code = 'AD02');

-- ---------- 骞寸骇 ----------
INSERT INTO `grade` (`grade_name`)
SELECT t.grade_name FROM (
    SELECT '2024绾? AS grade_name UNION ALL SELECT '2025绾? UNION ALL SELECT '2026绾?
) t
WHERE NOT EXISTS (SELECT 1 FROM grade g WHERE g.grade_name = t.grade_name);

SET @g2024 := (SELECT grade_id FROM grade WHERE grade_name = '2024绾? LIMIT 1);
SET @g2025 := (SELECT grade_id FROM grade WHERE grade_name = '2025绾? LIMIT 1);
SET @g2026 := (SELECT grade_id FROM grade WHERE grade_name = '2026绾? LIMIT 1);

-- ---------- 鏁欒亴宸ユ。妗堬紙缁熶竴瀵嗙爜: 123321锛?----------
UPDATE `user` SET `real_name` = '鍒樹竴楦?, `gender` = 1, `title` = '璁插笀',   `position` = NULL,     `dept_id` = @d_cs, `phone` = '13800001001', `email` = 'liuym@campus.edu'  WHERE `username` = '700001';
UPDATE `user` SET `real_name` = '瀹嬪缓鍥?, `gender` = 1, `title` = NULL,     `position` = '鏁欏姟骞蹭簨', `dept_id` = @d_cs, `phone` = '13800001002', `email` = 'songjg@campus.edu' WHERE `username` = '800001';
UPDATE `user` SET `real_name` = '绯荤粺绠＄悊鍛? WHERE `username` = 'admin';

INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
    ('700002', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '闄堥潤',   2, '13800001003', 'chenj@campus.edu',   '鏁欐巿',   '闄㈤暱',     @d_cs, 1),
    ('700003', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '鐜嬫捣娑?, 1, '13800001004', 'wanght@campus.edu',  '鍓暀鎺?, NULL,       @d_cs, 1),
    ('700004', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '鏉庢枃涓?, 2, '13800001005', 'liwl@campus.edu',    '鏁欐巿',   '绯讳富浠?,   @d_em, 1),
    ('700005', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '寮犲浗寮?, 1, '13800001006', 'zhanggq@campus.edu', '璁插笀',   NULL,       @d_em, 1),
    ('700006', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '璧甸洩姊?, 2, '13800001007', 'zhaoxm@campus.edu',  '鍓暀鎺?, NULL,       @d_fl, 1),
    ('700007', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '瀛欏織鍒?, 1, '13800001008', 'sunzg@campus.edu',   '鏁欐巿',   '鍓櫌闀?,   @d_me, 1),
    ('700008', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '鍛ㄦ晱',   2, '13800001009', 'zhoum@campus.edu',   '璁插笀',   NULL,       @d_ms, 1),
    ('700009', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '鍚存鑺?, 2, '13800001010', 'wugf@campus.edu',    '鍓暀鎺?, NULL,       @d_ad, 1),
    ('800002', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, '閮戠珛鏂?, 1, '13800001011', 'zhenglx@campus.edu', NULL,     '杈呭鍛?,   @d_cs, 1),
    ('800003', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, '鍐槬鐕?, 2, '13800001012', 'fengcy@campus.edu',  NULL,     '杈呭鍛?,   @d_em, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `dept_id` = VALUES(`dept_id`), `title` = VALUES(`title`), `position` = VALUES(`position`);

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON (
    (u.user_type = 2 AND r.role_code = 'TEACHER') OR
    (u.user_type = 3 AND r.role_code = 'STAFF')
)
WHERE u.username IN ('700002','700003','700004','700005','700006','700007','700008','700009','800002','800003');

-- ---------- 瀛︾敓妗ｆ锛?0 鍚嶏紝瑕嗙洊 3 涓勾绾?/ 10 涓笓涓?/ 12 涓渷浠斤級 ----------
INSERT INTO `student` (`student_no`, `student_name`, `gender`, `student_birth`, `student_age`, `student_address`, `grade_id`, `dept_id`, `major_id`, `class_name`, `origin_place`, `enroll_year`, `status`) VALUES
    (2024100001, '寮犱紵',   1, '2006-03-12', 20, '娴庡崡甯傚巻涓嬪尯', @g2024, @d_cs, @m_cs01, '杞欢2401', '灞变笢', 2024, 1),
    (2024100002, '鏉庡',   2, '2006-07-08', 20, '閮戝窞甯傞噾姘村尯', @g2024, @d_cs, @m_cs01, '杞欢2401', '娌冲崡', 2024, 1),
    (2024100003, '鐜嬪己',   1, '2005-11-23', 21, '鍗椾含甯傞紦妤煎尯', @g2024, @d_cs, @m_cs01, '杞欢2401', '姹熻嫃', 2024, 1),
    (2024100004, '鍒樻磱',   1, '2006-01-15', 20, '鏉窞甯傝タ婀栧尯', @g2024, @d_cs, @m_cs01, '杞欢2402', '娴欐睙', 2024, 1),
    (2024100005, '闄堥洩',   2, '2006-05-30', 20, '骞垮窞甯傚ぉ娌冲尯', @g2024, @d_cs, @m_cs01, '杞欢2402', '骞夸笢', 2024, 1),
    (2024100006, '鏉ㄥ厜',   1, '2006-09-02', 20, '鎴愰兘甯傛渚尯', @g2024, @d_cs, @m_cs01, '杞欢2402', '鍥涘窛', 2024, 1),
    (2024100007, '璧垫晱',   2, '2006-04-18', 20, '姝︽眽甯傛椽灞卞尯', @g2024, @d_cs, @m_cs02, '鏅鸿兘2401', '婀栧寳', 2024, 1),
    (2024100008, '榛勯箯',   1, '2005-12-09', 21, '鍚堣偉甯傝渶灞卞尯', @g2024, @d_cs, @m_cs02, '鏅鸿兘2401', '瀹夊窘', 2024, 1),
    (2024100009, '鍛ㄥ┓',   2, '2006-06-21', 20, '闀挎矙甯傚渤楹撳尯', @g2024, @d_cs, @m_cs02, '鏅鸿兘2401', '婀栧崡', 2024, 1),
    (2024100010, '鍚存槉',   1, '2006-02-14', 20, '鐭冲搴勫競妗ヨタ鍖?, @g2024, @d_cs, @m_cs02, '鏅鸿兘2401', '娌冲寳', 2024, 1),
    (2024100011, '寰愯姵',   2, '2006-08-25', 20, '瑗垮畨甯傞泚濉斿尯', @g2024, @d_em, @m_em01, '浼氳2401', '闄曡タ', 2024, 1),
    (2024100012, '瀛欑',   1, '2006-10-11', 20, '绂忓窞甯傞紦妤煎尯', @g2024, @d_em, @m_em01, '浼氳2401', '绂忓缓', 2024, 1),
    (2024100013, '椹附',   2, '2006-03-05', 20, '闈掑矝甯傚競鍗楀尯', @g2024, @d_em, @m_em01, '浼氳2401', '灞变笢', 2024, 1),
    (2024100014, '鏈卞啗',   1, '2005-12-28', 21, '娲涢槼甯傛锭瑗垮尯', @g2024, @d_em, @m_em02, '宸ョ2401', '娌冲崡', 2024, 1),
    (2024100015, '鑳¤澏',   2, '2006-07-17', 20, '鑻忓窞甯傚鑻忓尯', @g2024, @d_em, @m_em02, '宸ョ2401', '姹熻嫃', 2024, 1),
    (2024100016, '閮稕',   1, '2006-01-09', 20, '瀹佹尝甯傛捣鏇欏尯', @g2024, @d_fl, @m_fl01, '鑻辫2401', '娴欐睙', 2024, 1),
    (2024100017, '鏋楅湠',   2, '2006-05-22', 20, '娣卞湷甯傚崡灞卞尯', @g2024, @d_fl, @m_fl01, '鑻辫2401', '骞夸笢', 2024, 1),
    (2024100018, '浣曞钩',   1, '2006-09-14', 20, '缁甸槼甯傛丢鍩庡尯', @g2024, @d_fl, @m_fl02, '鏃ヨ2401', '鍥涘窛', 2024, 1),
    (2024100019, '楂樼繑',   1, '2006-04-01', 20, '瑗勯槼甯傛▕鍩庡尯', @g2024, @d_me, @m_me01, '鏈烘2401', '婀栧寳', 2024, 1),
    (2024100020, '缃楅潤',   2, '2006-11-19', 20, '鑺滄箹甯傞暅婀栧尯', @g2024, @d_me, @m_me01, '鏈烘2401', '瀹夊窘', 2024, 1),
    (2024100021, '閮戝嚡',   1, '2006-06-07', 20, '鏍床甯傚ぉ鍏冨尯', @g2024, @d_me, @m_me01, '鏈烘2401', '婀栧崡', 2024, 1),
    (2024100022, '姊佺埥',   2, '2006-02-26', 20, '鍞愬北甯傝矾鍖楀尯', @g2024, @d_me, @m_me02, '杞﹁締2401', '娌冲寳', 2024, 1),
    (2024100023, '瀹嬩匠',   2, '2006-08-13', 20, '鍜搁槼甯傜Е閮藉尯', @g2024, @d_ms, @m_ms01, '鏁板2401', '闄曡タ', 2024, 1),
    (2024100024, '鍞愮',   1, '2006-10-04', 20, '鍘﹂棬甯傛€濇槑鍖?, @g2024, @d_ad, @m_ad01, '瑙嗕紶2401', '绂忓缓', 2024, 1),
    (2025100001, '璁告櫞',   2, '2007-03-16', 19, '鐑熷彴甯傝姖缃樺尯', @g2025, @d_cs, @m_cs01, '杞欢2501', '灞变笢', 2025, 1),
    (2025100002, '閭撹秴',   1, '2007-07-29', 19, '寮€灏佸競榫欎涵鍖?, @g2025, @d_cs, @m_cs01, '杞欢2501', '娌冲崡', 2025, 1),
    (2025100003, '鍐獩',   2, '2007-01-08', 19, '鏃犻敗甯傛婧尯', @g2025, @d_cs, @m_cs01, '杞欢2501', '姹熻嫃', 2025, 1),
    (2025100004, '鏇归槼',   1, '2007-05-20', 19, '娓╁窞甯傞箍鍩庡尯', @g2025, @d_cs, @m_cs01, '杞欢2502', '娴欐睙', 2025, 1),
    (2025100005, '褰',   1, '2007-09-11', 19, '浣涘北甯傜鍩庡尯', @g2025, @d_cs, @m_cs01, '杞欢2502', '骞夸笢', 2025, 1),
    (2025100006, '钁ｉ洦',   2, '2007-04-03', 19, '寰烽槼甯傛棇闃冲尯', @g2025, @d_cs, @m_cs02, '鏅鸿兘2501', '鍥涘窛', 2025, 1),
    (2025100007, '琚侀噹',   1, '2007-12-15', 19, '瀹滄槍甯傝タ闄靛尯', @g2025, @d_cs, @m_cs02, '鏅鸿兘2501', '婀栧寳', 2025, 1),
    (2025100008, '钂嬫',   2, '2007-06-26', 19, '瀹夊簡甯傝繋姹熷尯', @g2025, @d_cs, @m_cs02, '鏅鸿兘2501', '瀹夊窘', 2025, 1),
    (2025100009, '闊╅洩',   2, '2007-02-17', 19, '琛￠槼甯傞泚宄板尯', @g2025, @d_cs, @m_cs02, '鏅鸿兘2501', '婀栧崡', 2025, 1),
    (2025100010, '娌堟稕',   1, '2007-08-08', 19, '淇濆畾甯傜珵绉€鍖?, @g2025, @d_em, @m_em01, '浼氳2501', '娌冲寳', 2025, 1),
    (2025100011, '濮氭槑杞?, 1, '2007-10-30', 19, '瀹濋浮甯傛腑婊ㄥ尯', @g2025, @d_em, @m_em01, '浼氳2501', '闄曡タ', 2025, 1),
    (2025100012, '璋腹',   2, '2007-03-21', 19, '娉夊窞甯備赴娉藉尯', @g2025, @d_em, @m_em01, '浼氳2501', '绂忓缓', 2025, 1),
    (2025100013, '閽熼福',   1, '2007-11-12', 19, '娼嶅潑甯傚鏂囧尯', @g2025, @d_em, @m_em02, '宸ョ2501', '灞变笢', 2025, 1),
    (2025100014, '宕旂惓',   2, '2007-05-04', 19, '鏂颁埂甯傜孩鏃楀尯', @g2025, @d_em, @m_em02, '宸ョ2501', '娌冲崡', 2025, 1),
    (2025100015, '娼樿秺',   1, '2007-09-25', 19, '寰愬窞甯備簯榫欏尯', @g2025, @d_fl, @m_fl01, '鑻辫2501', '姹熻嫃', 2025, 1),
    (2025100016, '闄嗙懚',   2, '2007-01-27', 19, '鍢夊叴甯傚崡婀栧尯', @g2025, @d_fl, @m_fl01, '鑻辫2501', '娴欐睙', 2025, 1),
    (2025100017, '钄℃槑',   1, '2007-07-06', 19, '涓滆帪甯傚崡鍩庡尯', @g2025, @d_fl, @m_fl02, '鏃ヨ2501', '骞夸笢', 2025, 1),
    (2025100018, '涓侀',   2, '2007-04-14', 19, '涔愬北甯傚競涓尯', @g2025, @d_me, @m_me01, '鏈烘2501', '鍥涘窛', 2025, 1),
    (2025100019, '浠绘澃',   1, '2007-12-01', 19, '榛勭煶甯傞粍鐭虫腐鍖?, @g2025, @d_me, @m_me01, '鏈烘2501', '婀栧寳', 2025, 1),
    (2025100020, '鏂瑰渾',   1, '2007-06-18', 19, '铓屽煚甯傞緳瀛愭箹鍖?, @g2025, @d_me, @m_me02, '杞﹁締2501', '瀹夊窘', 2025, 1),
    (2026100001, '鐭崇',   1, '2008-02-09', 18, '涓存矀甯傚叞灞卞尯', @g2026, @d_cs, @m_cs01, '杞欢2601', '灞变笢', 2026, 1),
    (2026100002, '璐鹃潤闆?, 2, '2008-08-19', 18, '鍗楅槼甯傚崸榫欏尯', @g2026, @d_cs, @m_cs01, '杞欢2601', '娌冲崡', 2026, 1),
    (2026100003, '瀛熸旦',   1, '2008-10-22', 18, '甯稿窞甯傚ぉ瀹佸尯', @g2026, @d_cs, @m_cs01, '杞欢2601', '姹熻嫃', 2026, 1),
    (2026100004, '绉﹀矚',   2, '2008-03-31', 18, '閲戝崕甯傚┖鍩庡尯', @g2026, @d_cs, @m_cs01, '杞欢2601', '娴欐睙', 2026, 1),
    (2026100005, '姹熸稕',   1, '2008-11-05', 18, '鐝犳捣甯傞娲插尯', @g2026, @d_cs, @m_cs02, '鏅鸿兘2601', '骞夸笢', 2026, 1),
    (2026100006, '灏规ⅵ',   2, '2008-05-13', 18, '鍗楀厖甯傞『搴嗗尯', @g2026, @d_cs, @m_cs02, '鏅鸿兘2601', '鍥涘窛', 2026, 1),
    (2026100007, '钖涘嘲',   1, '2008-09-27', 18, '鑽嗗窞甯傛矙甯傚尯', @g2026, @d_cs, @m_cs02, '鏅鸿兘2601', '婀栧寳', 2026, 1),
    (2026100008, '闂Ξ',   2, '2008-01-24', 18, '闃滈槼甯傞宸炲尯', @g2026, @d_em, @m_em01, '浼氳2601', '瀹夊窘', 2026, 1),
    (2026100009, '榫氬畤',   1, '2008-07-16', 18, '宀抽槼甯傚渤闃虫ゼ鍖?, @g2026, @d_em, @m_em01, '浼氳2601', '婀栧崡', 2026, 1),
    (2026100010, '甯歌繙',   1, '2008-04-08', 18, '閭兏甯備笡鍙板尯', @g2026, @d_fl, @m_fl01, '鑻辫2601', '娌冲寳', 2026, 1),
    (2026100011, '鍊Ξ',   2, '2008-12-11', 18, '娓崡甯備复娓尯', @g2026, @d_fl, @m_fl01, '鑻辫2601', '闄曡タ', 2026, 1),
    (2026100012, '涓ュ郴',   1, '2008-06-02', 18, '婕冲窞甯傝姉鍩庡尯', @g2026, @d_me, @m_me01, '鏈烘2601', '绂忓缓', 2026, 1),
    (2026100013, '鐗涜帀',   2, '2008-02-28', 18, '娣勫崥甯傚紶搴楀尯', @g2026, @d_me, @m_me01, '鏈烘2601', '灞变笢', 2026, 1),
    (2026100014, '渚寒',   1, '2008-08-06', 18, '鍟嗕笜甯傛鍥尯', @g2026, @d_ms, @m_ms01, '鏁板2601', '娌冲崡', 2026, 1),
    (2026100015, '閭靛叺',   1, '2008-10-17', 18, '鎵窞甯傚箍闄靛尯', @g2026, @d_ad, @m_ad01, '瑙嗕紶2601', '姹熻嫃', 2026, 1),
    (2026100016, '涓囪寽',   2, '2008-05-09', 18, '缁嶅叴甯傝秺鍩庡尯', @g2026, @d_cs, @m_cs01, '杞欢2601', '娴欐睙', 2026, 1)
ON DUPLICATE KEY UPDATE `student_name` = VALUES(`student_name`), `dept_id` = VALUES(`dept_id`), `major_id` = VALUES(`major_id`);

-- ---------- 鎷涚敓璁″垝锛?024-2026锛?026 杩庢柊杩涜涓級 ----------
INSERT INTO `enrollment` (`major_id`, `year`, `plan_count`, `actual_count`, `report_rate`) VALUES
    (@m_cs01, 2024, 120, 118, 98.33), (@m_cs02, 2024,  90,  88, 97.78),
    (@m_em01, 2024, 100,  97, 97.00), (@m_em02, 2024,  80,  76, 95.00),
    (@m_fl01, 2024,  60,  58, 96.67), (@m_fl02, 2024,  40,  36, 90.00),
    (@m_me01, 2024, 110, 105, 95.45), (@m_me02, 2024,  70,  66, 94.29),
    (@m_ms01, 2024,  50,  47, 94.00), (@m_ms02, 2024,  45,  41, 91.11),
    (@m_ad01, 2024,  55,  52, 94.55), (@m_ad02, 2024,  50,  46, 92.00),
    (@m_cs01, 2025, 130, 127, 97.69), (@m_cs02, 2025, 100,  97, 97.00),
    (@m_em01, 2025, 100,  95, 95.00), (@m_em02, 2025,  75,  70, 93.33),
    (@m_fl01, 2025,  60,  57, 95.00), (@m_fl02, 2025,  40,  37, 92.50),
    (@m_me01, 2025, 105,  99, 94.29), (@m_me02, 2025,  65,  60, 92.31),
    (@m_ms01, 2025,  50,  46, 92.00), (@m_ms02, 2025,  45,  42, 93.33),
    (@m_ad01, 2025,  55,  51, 92.73), (@m_ad02, 2025,  48,  44, 91.67),
    (@m_cs01, 2026, 140,  96, 68.57), (@m_cs02, 2026, 110,  74, 67.27),
    (@m_em01, 2026, 100,  61, 61.00), (@m_em02, 2026,  75,  40, 53.33),
    (@m_fl01, 2026,  60,  35, 58.33), (@m_fl02, 2026,  40,  19, 47.50),
    (@m_me01, 2026, 100,  58, 58.00), (@m_me02, 2026,  60,  31, 51.67),
    (@m_ms01, 2026,  50,  28, 56.00), (@m_ms02, 2026,  45,  22, 48.89),
    (@m_ad01, 2026,  55,  30, 54.55), (@m_ad02, 2026,  48,  21, 43.75)
ON DUPLICATE KEY UPDATE `plan_count` = VALUES(`plan_count`), `actual_count` = VALUES(`actual_count`), `report_rate` = VALUES(`report_rate`);

-- ---------- 鏂伴椈鍏憡 ----------
SET @u_admin := (SELECT user_id FROM `user` WHERE username = 'admin');
SET @u_t1    := (SELECT user_id FROM `user` WHERE username = '700001');
SET @u_s1    := (SELECT user_id FROM `user` WHERE username = '600001');

INSERT INTO `news` (`title`, `content`, `news_type`, `publisher_id`, `is_pinned`, `create_time`)
SELECT t.title, t.content, t.news_type, @u_admin, t.is_pinned, t.create_time FROM (
    SELECT '鍏充簬2026绾ф柊鐢熸姤鍒板畨鎺掔殑閫氱煡' AS title, '2026绾ф柊鐢熻浜?鏈?8鏃?29鏃ユ寔褰曞彇閫氱煡涔﹀埌鍚勫闄㈣繋鏂扮偣鍔炵悊鎶ュ埌鎵嬬画锛屽鑸嶅垎閰嶇粨鏋滃彲鍦ㄨ繋鏂扮郴缁熶腑鏌ヨ銆? AS content, '鍏憡' AS news_type, 1 AS is_pinned, '2026-07-10 09:00:00' AS create_time
    UNION ALL SELECT '2026-2027瀛﹀勾绗竴瀛︽湡閫夎閫氱煡', '绗竴杞€夎灏嗕簬8鏈?0鏃ュ紑鏀撅紝璇峰悓瀛︿滑鎻愬墠鏌ョ湅鍩瑰吇鏂规锛屽悎鐞嗚鍒掑鍒嗐€?, '鍏憡', 0, '2026-07-12 10:30:00'
    UNION ALL SELECT '鎴戞牎瀛﹀瓙鍦ㄥ叏鍥藉ぇ瀛︾敓绋嬪簭璁捐绔炶禌涓幏浣崇哗', '鍦ㄥ垰鍒氱粨鏉熺殑鍏ㄥ浗澶у鐢熺▼搴忚璁＄珵璧涗腑锛屾垜鏍′笁鏀唬琛ㄩ槦鍒嗚幏閲戙€侀摱銆侀摐濂栵紝鍒涘巻鍙叉渶濂芥垚缁┿€?, '鏂伴椈', 0, '2026-07-08 15:20:00'
    UNION ALL SELECT '鏅烘収鏍″洯鏈嶅姟骞冲彴姝ｅ紡涓婄嚎璇曡繍琛?, '骞冲彴鏁村悎鏁欏姟銆佸宸ャ€佸姙鍏笌鍩虹鏁版嵁鍥涘ぇ鏉垮潡锛屼负鍏ㄦ牎甯堢敓鎻愪緵涓€绔欏紡鍦ㄧ嚎鏈嶅姟銆?, '鏂伴椈', 0, '2026-07-05 08:00:00'
    UNION ALL SELECT '鍥句功棣嗘殤鏈熷紑鏀炬椂闂磋皟鏁村叕鍛?, '7鏈?5鏃ヨ嚦8鏈?5鏃ユ湡闂达紝鍥句功棣嗗紑鏀炬椂闂磋皟鏁翠负姣忔棩9:00-17:00锛岃妭鍋囨棩闂銆?, '鍏憡', 0, '2026-07-13 16:45:00'
) t
WHERE NOT EXISTS (SELECT 1 FROM news);

-- ---------- 璁哄潧甯栧瓙涓庡洖澶?----------
INSERT INTO `forum_post` (`title`, `content`, `author_id`, `like_count`, `view_count`, `status`, `create_time`)
SELECT t.title, t.content, t.author_id, t.like_count, t.view_count, t.status, t.create_time FROM (
    SELECT '鏂扮敓姹傚姪锛氬鑸嶇綉缁滃浣曞紑閫氾紵' AS title, '椹笂瑕佹姤鍒颁簡锛岃闂鑸嶇殑鏍″洯缃戞€庝箞鍔炵悊锛熼渶瑕佹彁鍓嶅噯澶囦粈涔堟潗鏂欏悧锛? AS content, @u_s1 AS author_id, 12 AS like_count, 208 AS view_count, 1 AS status, '2026-07-11 20:15:00' AS create_time
    UNION ALL SELECT '鏆戞湡瀹炰範缁忛獙鍒嗕韩甯?, '鍒氱粨鏉熷湪涓€瀹朵簰鑱旂綉鍏徃鐨勫疄涔狅紝鏁寸悊浜嗕竴浜涙姇閫掔畝鍘嗗拰闈㈣瘯鐨勭粡楠岋紝娆㈣繋浜ゆ祦銆?, @u_s1, 45, 530, 1, '2026-07-09 14:30:00'
    UNION ALL SELECT '鍏充簬閫夎绯荤粺浣跨敤闂鐨勭瓟鐤戞眹鎬?, '鏁寸悊浜嗗悓瀛︿滑甯歌鐨勯€夎闂鍜岃В鍐冲姙娉曪紝閫夎鍓嶅缓璁厛鐪嬭繖涓€甯栥€?, @u_t1, 67, 890, 1, '2026-07-12 09:00:00'
    UNION ALL SELECT '浣庝环鍑哄叏鏂拌€冪爺璧勬枡锛堣繚瑙勭ず渚嬶級', '鍚勭鑰冪爺璧勬枡浣庝环杞锛屽姞寰俊璇﹁亰銆?, @u_s1, 0, 35, -1, '2026-07-13 22:40:00'
) t
WHERE NOT EXISTS (SELECT 1 FROM forum_post);

SET @p_net   := (SELECT post_id FROM forum_post WHERE title = '鏂扮敓姹傚姪锛氬鑸嶇綉缁滃浣曞紑閫氾紵' LIMIT 1);
SET @p_intern := (SELECT post_id FROM forum_post WHERE title = '鏆戞湡瀹炰範缁忛獙鍒嗕韩甯? LIMIT 1);
SET @p_course := (SELECT post_id FROM forum_post WHERE title = '鍏充簬閫夎绯荤粺浣跨敤闂鐨勭瓟鐤戞眹鎬? LIMIT 1);

INSERT INTO `forum_comment` (`post_id`, `author_id`, `content`, `status`, `create_time`)
SELECT t.post_id, t.author_id, t.content, 1, t.create_time FROM (
    SELECT @p_net AS post_id, @u_t1 AS author_id, '鎶ュ埌褰撳ぉ鍦ㄥ鑸嶆ゼ涓嬫湁缃戠粶杩愯惀鍟嗙殑鍔炵悊鐐癸紝甯﹁韩浠借瘉鍗冲彲銆? AS content, '2026-07-11 21:00:00' AS create_time
    UNION ALL SELECT @p_net, @u_s1, '涔熷彲浠ュ湪浼佷笟寰俊閲屾悳绱?鏍″洯缃戣嚜鍔╁紑閫?锛岀嚎涓婂姙鐞嗘洿蹇€?, '2026-07-11 21:35:00'
    UNION ALL SELECT @p_intern, @u_s1, '鎰熻阿鍒嗕韩锛佽闂畝鍘嗘ā鏉挎柟渚垮彂涓€浠藉悧锛?, '2026-07-09 15:10:00'
    UNION ALL SELECT @p_intern, @u_t1, '鍐欏緱寰堝疄鐢紝宸叉帹鑽愮粰鎴戝甫鐨勬瘯涓氳璁″皬缁勩€?, '2026-07-09 18:22:00'
    UNION ALL SELECT @p_course, @u_s1, '璇烽棶璺ㄤ笓涓氶€夎闇€瑕佸厛鎻愪氦鐢宠鍚楋紵', '2026-07-12 10:05:00'
    UNION ALL SELECT @p_course, @u_t1, '闇€瑕佺殑锛屽湪鏁欏姟绯荤粺鎻愪氦璺ㄤ笓涓氶€夎鐢宠锛屽闄㈠鏍搁€氳繃鍚庡嵆鍙€夎銆?, '2026-07-12 10:40:00'
    UNION ALL SELECT @p_course, @u_s1, '鏄庣櫧浜嗭紝璋㈣阿鑰佸笀锛?, '2026-07-12 11:02:00'
    UNION ALL SELECT @p_net, @u_s1, '琛ュ厖锛氭柊鐢熷鑸嶄粖骞村凡鍏ㄩ儴瑕嗙洊 WiFi锛屽紑閫氳处鍙峰悗鐩存帴杩炴帴鍗冲彲銆?, '2026-07-12 08:50:00'
) t
WHERE NOT EXISTS (SELECT 1 FROM forum_comment);

