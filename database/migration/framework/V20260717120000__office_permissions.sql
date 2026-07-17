USE school_spring;

INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
    ('fee:self:read', '查询本人账单与流水'), ('fee:self:pay', '支付本人账单'), ('fee:manage', '管理费用账单'),
    ('asset:read', '读取资产台账'), ('asset:apply', '提交资产申请'), ('asset:manage', '管理和审批资产'),
    ('work-plan:self', '维护本人工作计划'), ('work-plan:manage', '管理和点评工作计划'),
    ('document:self', '发起并查看本人公文'), ('document:approve', '审批流转至本人的公文'),
    ('meeting:self', '查看并反馈本人会议'), ('meeting:manage', '发布和管理会议'),
    ('notification:self:read', '读取本人通知'), ('ai-approval:use', '使用AI审批助手')
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1;

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.role_id, p.permission_id
FROM `role` r
CROSS JOIN `permission` p
WHERE r.role_code = 'ADMIN'
   OR (r.role_code = 'STUDENT' AND p.permission_code IN (
       'fee:self:read', 'fee:self:pay', 'notification:self:read'))
   OR (r.role_code = 'TEACHER' AND p.permission_code IN (
       'office:read', 'asset:read', 'asset:apply', 'work-plan:self',
       'document:self', 'document:approve', 'meeting:self', 'meeting:manage',
       'notification:self:read', 'ai-approval:use'))
   OR (r.role_code = 'STAFF' AND p.permission_code IN (
       'office:read', 'office:write', 'fee:self:read', 'fee:self:pay', 'fee:manage',
       'asset:read', 'asset:apply', 'asset:manage', 'work-plan:self', 'work-plan:manage',
       'document:self', 'document:approve', 'meeting:self', 'meeting:manage',
       'notification:self:read', 'ai-approval:use'));

INSERT INTO `menu` (`title`, `path`, `permission_code`, `sort_order`, `status`) VALUES
    ('学杂费交纳', '/home/fee-payment', 'fee:self:read', 31, 1)
ON DUPLICATE KEY UPDATE
    `title` = VALUES(`title`), `permission_code` = VALUES(`permission_code`),
    `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);
