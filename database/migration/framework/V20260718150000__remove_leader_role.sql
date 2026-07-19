USE school_spring;

-- LEADER 不再是系统角色。先移除本地演示账号，再删除角色；关联权限由外键级联清理。
DELETE FROM `user`
WHERE username = 'leader';

DELETE FROM `role`
WHERE role_code = 'LEADER';
