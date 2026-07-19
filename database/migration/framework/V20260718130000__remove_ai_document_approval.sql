USE school_spring;

-- 删除成员 C 的 AI 公文摘要与审批助手菜单及授权关系。
DELETE FROM `menu`
WHERE `path` = '/home/ai-approval'
   OR `permission_code` = 'ai-approval:use';

DELETE rp
FROM `role_permission` rp
JOIN `permission` p ON p.`permission_id` = rp.`permission_id`
WHERE p.`permission_code` = 'ai-approval:use';

DELETE FROM `permission`
WHERE `permission_code` = 'ai-approval:use';
