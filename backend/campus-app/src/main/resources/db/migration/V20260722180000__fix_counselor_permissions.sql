-- ============================================================
-- 修复辅导员/教师权限V2：辅导员拥有全部协同办公功能
-- ============================================================

-- 1. 移除 TEACHER 的 fee:overview:read
DELETE rp FROM role_permission rp
JOIN role r ON rp.role_id = r.role_id
JOIN permission p ON rp.permission_id = p.permission_id
WHERE r.role_code = 'TEACHER' AND p.permission_code = 'fee:overview:read';

-- 2. 授予 COUNSELOR 协同办公全部权限
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.role_id, p.permission_id FROM role r, permission p
WHERE r.role_code = 'COUNSELOR'
AND p.permission_code IN (
    'office:read',           -- 协同办公入口
    'fee:self:read',         -- C1 缴费查询
    'fee:self:pay',          -- C1 缴费支付
    'fee:overview:read',     -- C1 查看学生缴费概览
    'asset:read',            -- C2 资产查看
    'asset:apply',           -- C2 资产申请
    'work-plan:self',        -- C3 个人工作计划
    'work-plan:manage',      -- C3 管理他人计划
    'document:self',         -- C4 公文查看/发起
    'document:approve',      -- C4 公文审批
    'meeting:self',          -- C5 个人会议
    'meeting:manage',        -- C5 会议管理
    'notification:self:read' -- 通知读取
);

-- 3. 移除 TEACHER 的 fee:manage（教师不应管理缴费）
DELETE rp FROM role_permission rp
JOIN role r ON rp.role_id = r.role_id
JOIN permission p ON rp.permission_id = p.permission_id
WHERE r.role_code = 'TEACHER' AND p.permission_code IN ('fee:manage', 'fee:overview:read');

-- ==================== 验收 ====================
SELECT '教师 fees' AS label, COUNT(*) FROM role r JOIN role_permission rp ON r.role_id=rp.role_id JOIN permission p ON rp.permission_id=p.permission_id WHERE r.role_code='TEACHER' AND p.permission_code LIKE 'fee%'
UNION ALL SELECT '辅导员 fees', COUNT(*) FROM role r JOIN role_permission rp ON r.role_id=rp.role_id JOIN permission p ON rp.permission_id=p.permission_id WHERE r.role_code='COUNSELOR' AND p.permission_code LIKE 'fee%'
UNION ALL SELECT '辅导员 office总数', COUNT(*) FROM role r JOIN role_permission rp ON r.role_id=rp.role_id JOIN permission p ON rp.permission_id=p.permission_id WHERE r.role_code='COUNSELOR' AND p.permission_code LIKE '%:%'
UNION ALL SELECT '=== 完成 ===', 0;
