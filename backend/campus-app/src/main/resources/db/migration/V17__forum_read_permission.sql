-- ============================================
-- 鎴愬憳 D锛氳鍧涙潈闄愮嫭绔?鈥?瀛︾敓鍙兘鐪嬭鍧涳紝涓嶈兘鐪嬫暟鎹姤琛?
-- ============================================

-- 1. 鏂板 forum:read 鏉冮檺
INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('forum:read', '鏌ョ湅鏂伴椈涓庤鍧?)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 2. 鎵€鏈夎鑹查兘鏈?forum:read
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r CROSS JOIN `permission` p
WHERE p.permission_code = 'forum:read';

-- 3. 绉婚櫎瀛︾敓鐨?base:read 鏉冮檺
DELETE rp FROM `role_permission` rp
JOIN `role` r ON r.role_id = rp.role_id
JOIN `permission` p ON p.permission_id = rp.permission_id
WHERE r.role_code = 'STUDENT' AND p.permission_code = 'base:read';

