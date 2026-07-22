-- ============================================================
-- 学生事务端演示数据V2：实验室、评教(1-5分)、竞赛
-- ============================================================

-- ==================== 1. 实验室 ====================
INSERT INTO lab (lab_name, lab_no, location, capacity, manager_id, description, status, created_at, updated_at)
VALUES
('软件创新实验室', 'LAB001', '实验楼D-101', 30, 3, 'AI与数据科学实验平台', 1, NOW(), NOW()),
('电子设计实验室', 'LAB002', '实验楼D-102', 25, 3, '嵌入式开发实验平台', 1, NOW(), NOW());

INSERT INTO lab_open_slot (lab_id, open_date, start_period, end_period, created_by, created_at, updated_at)
SELECT lab_id, CURDATE(), 1, 4, manager_id, NOW(), NOW() FROM lab
UNION ALL SELECT lab_id, CURDATE(), 5, 8, manager_id, NOW(), NOW() FROM lab
UNION ALL SELECT lab_id, CURDATE(), 9, 12, manager_id, NOW(), NOW() FROM lab;

-- ==================== 2. 评教(教师800001, 评分1-5) ====================
INSERT INTO evaluation (student_id, schedule_id, teacher_id, course_id, semester, score_teaching, score_content, score_method, comment, create_time)
SELECT s.student_id, sc.schedule_id, sc.teacher_id, sc.course_id, '2025-2026-1',
       3 + (s.student_id % 3), 3 + ((s.student_id+1) % 3), 4 - (s.student_id % 3),
       CASE WHEN s.student_id % 2 = 0 THEN '讲解清晰，内容充实' ELSE '课堂活跃，互动良好' END,
       NOW()
FROM student s
JOIN course_selection cs ON cs.student_id = s.student_id AND cs.semester = '2025-2026-1' AND cs.status = 1
JOIN schedule sc ON sc.course_id = cs.course_id AND sc.semester = '2025-2026-1' AND sc.teacher_id = 3
WHERE s.student_id BETWEEN 1 AND 11
LIMIT 10;

INSERT INTO evaluation (student_id, schedule_id, teacher_id, course_id, semester, score_teaching, score_content, score_method, comment, create_time)
SELECT s.student_id, sc.schedule_id, sc.teacher_id, sc.course_id, '2025-2026-1',
       4 + (s.student_id % 2), 3 + ((s.student_id+2) % 3), 4 - ((s.student_id+1) % 3),
       CASE WHEN s.student_id % 3 = 0 THEN '认真负责，受益匪浅' ELSE '教学方法得当' END,
       NOW()
FROM student s
JOIN course_selection cs ON cs.student_id = s.student_id AND cs.semester = '2025-2026-1' AND cs.status = 1
JOIN schedule sc ON sc.course_id = cs.course_id AND sc.semester = '2025-2026-1' AND sc.teacher_id = 19
WHERE s.student_id BETWEEN 1 AND 11
LIMIT 8;

-- ==================== 3. 竞赛 ====================
INSERT INTO competition (competition_no, title, description, requirements, publisher_id, deadline, min_members, max_members, max_team_count, status, create_time, updated_at, published_at)
VALUES
('COMP001', '软件创新大赛', '软件设计与开发竞赛', '每队2-5人，需提交项目计划书', 3, '2026-09-30', 2, 5, 20, 1, NOW(), NOW(), NOW()),
('COMP002', '智能算法挑战赛', '数据科学与AI编程竞赛', '每队1-3人，Java/Python', 3, '2026-10-15', 1, 3, 30, 1, NOW(), NOW(), NOW());

INSERT INTO competition_team (registration_no, competition_id, team_name, leader_id, material_description, status, apply_time, updated_at)
SELECT 'REG001', c.competition_id, '代码先锋队', 1, '校园服务平台', 2, NOW(), NOW() FROM competition c WHERE c.competition_no='COMP001'
UNION ALL SELECT 'REG002', c.competition_id, '算法突击队', 2, '推荐系统实现', 1, NOW(), NOW() FROM competition c WHERE c.competition_no='COMP001'
UNION ALL SELECT 'REG003', c.competition_id, '数据探路者', 3, '图像识别研究', 2, NOW(), NOW() FROM competition c WHERE c.competition_no='COMP002';

INSERT INTO competition_member (team_id, student_id, role, invitation_status, invited_by, invited_at)
SELECT 1, 1, '队长', 1, NULL, NOW()
UNION ALL SELECT 1, 2, '队员', 1, 1, NOW()
UNION ALL SELECT 1, 3, '队员', 1, 1, NOW()
UNION ALL SELECT 2, 2, '队长', 1, NULL, NOW()
UNION ALL SELECT 2, 4, '队员', 1, 2, NOW()
UNION ALL SELECT 3, 3, '队长', 1, NULL, NOW()
UNION ALL SELECT 3, 5, '队员', 1, 3, NOW();

SELECT '=== 验收 ===' AS '';
SELECT '实验室' AS l, COUNT(*) FROM lab
UNION ALL SELECT '开放时段', COUNT(*) FROM lab_open_slot
UNION ALL SELECT '评教(AVG)', ROUND(AVG(score_teaching),1) FROM evaluation
UNION ALL SELECT '竞赛', COUNT(*) FROM competition
UNION ALL SELECT '队伍', COUNT(*) FROM competition_team
UNION ALL SELECT '完成', 0;
