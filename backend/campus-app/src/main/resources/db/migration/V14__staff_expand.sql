-- ============================================
-- 鎴愬憳 D锛氭暀鑱屽伐鏁版嵁鎵╁厖锛堢湡瀹炲ぇ瀛﹁妯★級
-- 14 涓櫌绯?脳 ~8-10 浜?鈮?125 浜猴紝鍚皯閲忓仠鐢?
-- ============================================

SET @pwd = '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy';
SET @staff_role = (SELECT role_id FROM role WHERE role_code = 'STAFF' LIMIT 1);

--
-- 璁＄畻鏈轰笌浜哄伐鏅鸿兘瀛﹂櫌锛坉ept_id=1锛屽師鏈?浜猴紝琛ュ厖3浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200010', @pwd, 2, '娌堥粯鐒?, 1, '13901001010', 'shenmr@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 1, 1),
('200011', @pwd, 2, '钂嬬煡杩?, 1, '13901001011', 'jiangzy@campus.edu.cn', '鍓暀鎺?, '鏁欑爺瀹や富浠?, 1, 1),
('200012', @pwd, 2, '闊╅洦妗?, 2, '13901001012', 'hanyt@campus.edu.cn', '璁插笀', NULL, 1, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200010','200011','200012');

--
-- 缁忔祹绠＄悊瀛﹂櫌锛坉ept_id=2锛屽師鏈?浜猴紝琛ュ厖5浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200020', @pwd, 2, '瀛熶护浠?, 2, '13902002020', 'mengly@campus.edu.cn', '鏁欐巿', '闄㈤暱', 2, 1),
('200021', @pwd, 2, '鍞愰€搁', 1, '13902002021', 'tangyf@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 2, 1),
('200022', @pwd, 2, '浠绘檽妤?, 2, '13902002022', 'renxn@campus.edu.cn', '璁插笀', NULL, 2, 1),
('200023', @pwd, 3, '涓佸缓骞?, 1, '13902002023', 'dingjp@campus.edu.cn', NULL, '鏁欏姟绉樹功', 2, 1),
('200024', @pwd, 2, '鍚曟€濇簮', 1, '13902002024', 'lvsy@campus.edu.cn', '鍔╂暀', NULL, 2, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200020','200021','200022','200023','200024');

--
-- 澶栧浗璇闄紙dept_id=3锛屽師鏈?浜猴紝琛ュ厖7浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200030', @pwd, 2, '椤惧娓?, 2, '13903003030', 'guwq@campus.edu.cn', '鏁欐巿', '闄㈤暱', 3, 1),
('200031', @pwd, 2, '娓╁崥鏂?, 1, '13903003031', 'wenbw@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 3, 1),
('200032', @pwd, 2, '鍙剁煡绉?, 2, '13903003032', 'yezq@campus.edu.cn', '璁插笀', NULL, 3, 1),
('200033', @pwd, 2, '濮氶敠绋?, 1, '13903003033', 'yaojc@campus.edu.cn', '璁插笀', NULL, 3, 1),
('200034', @pwd, 3, '甯搁泤鐞?, 2, '13903003034', 'changyq@campus.edu.cn', NULL, '琛屾斂鍔╃悊', 3, 1),
('200035', @pwd, 3, '鍌呴暱搴?, 1, '13903003035', 'fucg@campus.edu.cn', NULL, '瀹為獙瀹ょ鐞嗗憳', 3, 1),
('200036', @pwd, 2, '涔旇嫢鍏?, 2, '13903003036', 'qiaorl@campus.edu.cn', '鍔╂暀', NULL, 3, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200030','200031','200032','200033','200034','200035','200036');

--
-- 鏈烘宸ョ▼瀛﹂櫌锛坉ept_id=4锛屽師鏈?浜猴紝琛ュ厖7浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200040', @pwd, 2, '璋櫙琛?, 1, '13904004040', 'tanjx@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 4, 1),
('200041', @pwd, 2, '宕旀槑鍝?, 1, '13904004041', 'cuimz@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 4, 1),
('200042', @pwd, 2, '涓囩鎬?, 2, '13904004042', 'wanqy@campus.edu.cn', '鍓暀鎺?, NULL, 4, 1),
('200043', @pwd, 2, '鐭虫尟鍗?, 1, '13904004043', 'shizh@campus.edu.cn', '璁插笀', NULL, 4, 1),
('200044', @pwd, 3, '榫欐檽鐞?, 2, '13904004044', 'longxl@campus.edu.cn', NULL, '鏁欏姟绉樹功', 4, 1),
('200045', @pwd, 3, '鐔婂織杩?, 1, '13904004045', 'xiongzy@campus.edu.cn', NULL, '瀹為獙鍛?, 4, 1),
('200046', @pwd, 2, '鍚戠編鐞?, 2, '13904004046', 'xiangmq@campus.edu.cn', '鍔╂暀', NULL, 4, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200040','200041','200042','200043','200044','200045','200046');

--
-- 鏁板涓庣粺璁″闄紙dept_id=5锛屽師鏈?浜猴紝琛ュ厖6浜猴級
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200050', @pwd, 2, '榫氭槑杩?, 1, '13905005050', 'gongmy@campus.edu.cn', '鏁欐巿', '闄㈤暱', 5, 1),
('200051', @pwd, 2, '寤栭洦鑿?, 2, '13905005051', 'liaoyf@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 5, 1),
('200052', @pwd, 2, '鑼冪珛璇?, 1, '13905005052', 'fanlc@campus.edu.cn', '璁插笀', NULL, 5, 1),
('200053', @pwd, 2, '鐧介湶鏅?, 2, '13905005053', 'bailx@campus.edu.cn', '璁插笀', NULL, 5, 1),
('200054', @pwd, 3, '鐗涚瀹?, 1, '13905005054', 'niuqs@campus.edu.cn', NULL, '琛屾斂鍔╃悊', 5, 1),
('200055', @pwd, 2, '楠嗗ぉ瀹?, 1, '13905005055', 'luoty@campus.edu.cn', '鍔╂暀', NULL, 5, 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200050','200051','200052','200053','200054','200055');

--
-- 鑹烘湳璁捐瀛﹂櫌锛坉ept_id=6锛屽師鏈?浜猴紝琛ュ厖7浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200060', @pwd, 2, '澶忔煶渚?, 2, '13906006060', 'xialy@campus.edu.cn', '鏁欐巿', '闄㈤暱', 6, 1),
('200061', @pwd, 2, '璐圭帀鎴?, 1, '13906006061', 'feiyc@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 6, 1),
('200062', @pwd, 2, '瑜氭槑闇?, 2, '13906006062', 'chumx@campus.edu.cn', '鍓暀鎺?, NULL, 6, 1),
('200063', @pwd, 2, '灏ゅ瓙娑?, 1, '13906006063', 'youzh@campus.edu.cn', '璁插笀', NULL, 6, 1),
('200064', @pwd, 3, '閾舵檽鐕?, 2, '13906006064', 'yinxy@campus.edu.cn', NULL, '鏁欏姟绉樹功', 6, 1),
('200065', @pwd, 3, '鏌ユ案搴?, 1, '13906006065', 'chayk@campus.edu.cn', NULL, '璁惧绠＄悊鍛?, 6, 1),
('200066', @pwd, 2, '绫虫€濈惇', 2, '13906006066', 'misq@campus.edu.cn', '鍔╂暀', NULL, 6, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200060','200061','200062','200063','200064','200065','200066');

--
-- 娉曞闄紙dept_id=16锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200160', @pwd, 2, '绠€闈掓澗', 1, '13916016016', 'jianqs@campus.edu.cn', '鏁欐巿', '闄㈤暱', 16, 1),
('200161', @pwd, 2, '宀虫€濆ù', 2, '13916016017', 'yuesx@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 16, 1),
('200162', @pwd, 2, '鑽ｅ崥闊?, 1, '13916016018', 'rongbt@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 16, 1),
('200163', @pwd, 2, '姹犳槧鏈?, 2, '13916016019', 'chiyy@campus.edu.cn', '鍓暀鎺?, NULL, 16, 1),
('200164', @pwd, 2, '宸壙蹇?, 1, '13916016020', 'wucz@campus.edu.cn', '璁插笀', NULL, 16, 1),
('200165', @pwd, 2, '濞勬檽鏅?, 2, '13916016021', 'louxq@campus.edu.cn', '璁插笀', NULL, 16, 1),
('200166', @pwd, 3, '鐬垮缓鍗?, 1, '13916016022', 'qujh@campus.edu.cn', NULL, '鏁欏姟绉樹功', 16, 1),
('200167', @pwd, 3, '閯㈢鏈?, 2, '13916016023', 'yanqy@campus.edu.cn', NULL, '璧勬枡绠＄悊鍛?, 16, 1),
('200168', @pwd, 2, '鍐兼枃鍗?, 1, '13916016024', 'xianwb@campus.edu.cn', '鍔╂暀', NULL, 16, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200160','200161','200162','200163','200164','200165','200166','200167','200168');

--
-- 鍦熸湪宸ョ▼瀛﹂櫌锛坉ept_id=17锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200170', @pwd, 2, '娲敠鍗?, 1, '13917017017', 'hongjh@campus.edu.cn', '鏁欐巿', '闄㈤暱', 17, 1),
('200171', @pwd, 2, '瀵囧織杩?, 1, '13917017018', 'kouzy@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 17, 1),
('200172', @pwd, 2, '涓涙収蹇?, 2, '13917017019', 'conghx@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 17, 1),
('200173', @pwd, 2, '鐒︽枃榫?, 1, '13917017020', 'jiaowl@campus.edu.cn', '鍓暀鎺?, NULL, 17, 1),
('200174', @pwd, 2, '鐢勫瓙钀?, 2, '13917017021', 'zhenzx@campus.edu.cn', '璁插笀', NULL, 17, 1),
('200175', @pwd, 2, '閮庢€濊繙', 1, '13917017022', 'langsy@campus.edu.cn', '璁插笀', NULL, 17, 1),
('200176', @pwd, 3, '妤氫簯椋?, 1, '13917017023', 'chuyf@campus.edu.cn', NULL, '瀹為獙鍛?, 17, 1),
('200177', @pwd, 3, '榛庤嫢鍏?, 2, '13917017024', 'lirl@campus.edu.cn', NULL, '鏁欏姟绉樹功', 17, 1),
('200178', @pwd, 2, '钃濅竴楦?, 1, '13917017025', 'lanym@campus.edu.cn', '鍔╂暀', NULL, 17, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200170','200171','200172','200173','200174','200175','200176','200177','200178');

--
-- 鐢靛瓙淇℃伅瀛﹂櫌锛坉ept_id=18锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200180', @pwd, 2, '姝︿繆褰?, 1, '13918018018', 'wujy@campus.edu.cn', '鏁欐巿', '闄㈤暱', 18, 1),
('200181', @pwd, 2, '妤肩帀鍏?, 2, '13918018019', 'louyl@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 18, 1),
('200182', @pwd, 2, '娆ч敠绋?, 1, '13918018020', 'oujc@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 18, 1),
('200183', @pwd, 2, '鐢勯泤鐞?, 2, '13918018021', 'zhenyq@campus.edu.cn', '鍓暀鎺?, NULL, 18, 1),
('200184', @pwd, 2, '钂嬫€濇簮', 1, '13918018022', 'jiangsy@campus.edu.cn', '璁插笀', '鏁欑爺瀹や富浠?, 18, 1),
('200185', @pwd, 2, '璐濇檽妤?, 2, '13918018023', 'beixn@campus.edu.cn', '璁插笀', NULL, 18, 1),
('200186', @pwd, 3, '宕斿缓骞?, 1, '13918018024', 'cuijp@campus.edu.cn', NULL, '瀹為獙瀹や富浠?, 18, 1),
('200187', @pwd, 3, '涓囬洦妗?, 2, '13918018025', 'wanyt@campus.edu.cn', NULL, '琛屾斂鍔╃悊', 18, 1),
('200188', @pwd, 2, '鑰垮崥鏂?, 1, '13918018026', 'gengbw@campus.edu.cn', '鍔╂暀', NULL, 18, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200180','200181','200182','200183','200184','200185','200186','200187','200188');

--
-- 鍖栧鍖栧伐瀛﹂櫌锛坉ept_id=19锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200190', @pwd, 2, '閽辩帀鎴?, 1, '13919019019', 'qianyc@campus.edu.cn', '鏁欐巿', '闄㈤暱', 19, 1),
('200191', @pwd, 2, '钖涜嫢鍏?, 2, '13919019020', 'xuerl@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 19, 1),
('200192', @pwd, 2, '浜庣珛璇?, 1, '13919019021', 'yulc@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 19, 1),
('200193', @pwd, 2, '涓滄柟鏄庨湠', 2, '13919019022', 'dongfmx@campus.edu.cn', '鍓暀鎺?, NULL, 19, 1),
('200194', @pwd, 2, '閭辨€濊繙', 1, '13919019023', 'qiusy@campus.edu.cn', '璁插笀', NULL, 19, 1),
('200195', @pwd, 3, '浣樺缓鍗?, 1, '13919019024', 'shejh@campus.edu.cn', NULL, '瀹為獙鍛?, 19, 1),
('200196', @pwd, 3, '鍏扮編鐞?, 2, '13919019025', 'lanmq@campus.edu.cn', NULL, '鏁欏姟绉樹功', 19, 1),
('200197', @pwd, 2, '鍏ㄥ瓙杞?, 1, '13919019026', 'quanzx@campus.edu.cn', '鍔╂暀', NULL, 19, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200190','200191','200192','200193','200194','200195','200196','200197');

--
-- 鐢熷懡绉戝瀛﹂櫌锛坉ept_id=20锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝鏃犲仠鐢級
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200200', @pwd, 2, '鎴愮鎬?, 2, '13920020020', 'chengqy@campus.edu.cn', '鏁欐巿', '闄㈤暱', 20, 1),
('200201', @pwd, 2, '钁涙壙蹇?, 1, '13920020021', 'gecz@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 20, 1),
('200202', @pwd, 2, '鏇惧瓙娑?, 2, '13920020022', 'zengzh@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 20, 1),
('200203', @pwd, 2, '閭垫槑鍝?, 1, '13920020023', 'shaomz@campus.edu.cn', '鍓暀鎺?, NULL, 20, 1),
('200204', @pwd, 2, '鏍炬檽鐞?, 2, '13920020024', 'luanxl@campus.edu.cn', '璁插笀', NULL, 20, 1),
('200205', @pwd, 2, '鍒ぉ瀹?, 1, '13920020025', 'biety@campus.edu.cn', '璁插笀', NULL, 20, 1),
('200206', @pwd, 3, '鐕曚繆褰?, 1, '13920020026', 'yanjy@campus.edu.cn', NULL, '瀹為獙鍛?, 20, 1),
('200207', @pwd, 3, '璧瀹?, 2, '13920020027', 'heqs@campus.edu.cn', NULL, '鏁欏姟绉樹功', 20, 1)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200200','200201','200202','200203','200204','200205','200206','200207');

--
-- 鏁欒偛瀛﹂櫌锛坉ept_id=21锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200210', @pwd, 2, '鏁栨€濊繙', 1, '13921021021', 'aosy@campus.edu.cn', '鏁欐巿', '闄㈤暱', 21, 1),
('200211', @pwd, 2, '瀹夋煶渚?, 2, '13921021022', 'anly@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 21, 1),
('200212', @pwd, 2, '鍐峰崥闊?, 1, '13921021023', 'lengbt@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 21, 1),
('200213', @pwd, 2, '姹犵帀鍏?, 2, '13921021024', 'chiyl@campus.edu.cn', '鍓暀鎺?, NULL, 21, 1),
('200214', @pwd, 2, '涔愭檽妤?, 2, '13921021025', 'lexn@campus.edu.cn', '璁插笀', NULL, 21, 1),
('200215', @pwd, 2, '宸撮敠绋?, 1, '13921021026', 'bajc@campus.edu.cn', '璁插笀', NULL, 21, 1),
('200216', @pwd, 3, '瀹楀缓鍗?, 1, '13921021027', 'zongjh@campus.edu.cn', NULL, '琛屾斂鍔╃悊', 21, 1),
('200217', @pwd, 2, '娓告€濈惇', 2, '13921021028', 'yousq@campus.edu.cn', '鍔╂暀', NULL, 21, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200210','200211','200212','200213','200214','200215','200216','200217');

--
-- 鏂伴椈浼犳挱瀛﹂櫌锛坉ept_id=22锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200220', @pwd, 2, '寰愰洦妗?, 2, '13922022022', 'xuyt@campus.edu.cn', '鏁欐巿', '闄㈤暱', 22, 1),
('200221', @pwd, 2, '鍥介敠鍗?, 1, '13922022023', 'guojh@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 22, 1),
('200222', @pwd, 2, '閭㈡槑闇?, 2, '13922022024', 'xingmx@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 22, 1),
('200223', @pwd, 2, '鍝堝織杩?, 1, '13922022025', 'hazy@campus.edu.cn', '鍓暀鎺?, NULL, 22, 1),
('200224', @pwd, 2, '鏇剧珛璇?, 1, '13922022026', 'zenglc@campus.edu.cn', '璁插笀', NULL, 22, 1),
('200225', @pwd, 3, '閯傝嫢鍏?, 2, '13922022027', 'erl@campus.edu.cn', NULL, '鏁欏姟绉樹功', 22, 1),
('200226', @pwd, 3, '妤肩帀鎴?, 1, '13922022028', 'louyc@campus.edu.cn', NULL, '璁惧绠＄悊鍛?, 22, 1),
('200227', @pwd, 2, '鏄濇檽鏅?, 2, '13922022029', 'zanxq@campus.edu.cn', '鍔╂暀', NULL, 22, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200220','200221','200222','200223','200224','200225','200226','200227');

--
-- 鍖诲闄紙dept_id=23锛屾棤鍘熸湁锛岃ˉ鍏?浜猴紝+1鍋滅敤锛?
--
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200230', @pwd, 2, '閽熸櫙琛?, 1, '13923023023', 'zhongjx@campus.edu.cn', '鏁欐巿', '闄㈤暱', 23, 1),
('200231', @pwd, 2, '宸存収蹇?, 2, '13923023024', 'bahx@campus.edu.cn', '鏁欐巿', '鍓櫌闀?, 23, 1),
('200232', @pwd, 2, '姹熸槑鍝?, 1, '13923023025', 'jiangmz@campus.edu.cn', '鍓暀鎺?, '绯讳富浠?, 23, 1),
('200233', @pwd, 2, '鍗滅帀鍏?, 2, '13923023026', 'buyl@campus.edu.cn', '鍓暀鎺?, '鏁欑爺瀹や富浠?, 23, 1),
('200234', @pwd, 2, '閭㈡枃榫?, 1, '13923023027', 'xingwl@campus.edu.cn', '璁插笀', NULL, 23, 1),
('200235', @pwd, 2, '鍏荤鎬?, 2, '13923023028', 'yangqy@campus.edu.cn', '璁插笀', NULL, 23, 1),
('200236', @pwd, 3, '鎷涗繆褰?, 1, '13923023029', 'zhaojy@campus.edu.cn', NULL, '瀹為獙瀹や富浠?, 23, 1),
('200237', @pwd, 3, '鑽嗘檽鐞?, 2, '13923023030', 'jingxl@campus.edu.cn', NULL, '鏁欏姟绉樹功', 23, 1),
('200238', @pwd, 2, '涓滈儹澶╁畤', 1, '13923023031', 'donggty@campus.edu.cn', '鍔╂暀', NULL, 23, 0)
ON DUPLICATE KEY UPDATE real_name = VALUES(real_name), phone = VALUES(phone), email = VALUES(email), title = VALUES(title), position = VALUES(position), dept_id = VALUES(dept_id), status = VALUES(status);
INSERT INTO user_role (user_id, role_id) SELECT user_id, @staff_role FROM `user` WHERE username IN ('200230','200231','200232','200233','200234','200235','200236','200237','200238');

