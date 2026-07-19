-- ======================================
-- 角色账号互换迁移脚本
-- 700001: 教职工 → 辅导员
-- 800001: 辅导员 → 教职工
-- user_type: 1=学生, 2=辅导员, 3=教职工, 4=教务处
-- ======================================

-- 1. 更新 user 表的 user_type
UPDATE user SET user_type=3 WHERE username='700001';  -- 变为教职工
UPDATE user SET user_type=2 WHERE username='800001';  -- 变为辅导员

-- 2. 更新 user_role 关联：700001→COUNSELOR, 800001→STAFF
UPDATE user_role
SET role_id = (SELECT role_id FROM (SELECT role_id FROM role WHERE role_code='COUNSELOR') AS r)
WHERE user_id = (SELECT user_id FROM user WHERE username='700001');

UPDATE user_role
SET role_id = (SELECT role_id FROM (SELECT role_id FROM role WHERE role_code='STAFF') AS r)
WHERE user_id = (SELECT user_id FROM user WHERE username='800001');

-- 3. 验证
SELECT u.username, u.user_type,
  CASE u.user_type WHEN 1 THEN '学生' WHEN 2 THEN '辅导员' WHEN 3 THEN '教职工' WHEN 4 THEN '教务处' END AS type_label,
  r.role_name
FROM user u
JOIN user_role ur ON u.user_id=ur.user_id
JOIN role r ON ur.role_id=r.role_id
ORDER BY u.user_type;
