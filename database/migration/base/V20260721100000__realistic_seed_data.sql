-- ============================================
-- 智慧校园 - 真实大学规模种子数据
-- 14 院系 / 34 专业 / ~145 教职工 / 10,200 学生
-- 年级：2022-2025（在读），2026 仅有招生计划（未报到）
-- 2022 级 ~87% 已毕业，部分延毕/休学
-- 热门/冷门专业分层，报到率真实递减
-- ============================================
SET NAMES utf8mb4;
USE school_spring;

-- ============================================================
-- PART 0: 清理旧种子数据
-- ============================================================
DELETE FROM student WHERE student_no >= 2022100000;
DELETE FROM enrollment;
DELETE FROM user_role WHERE user_id IN (SELECT u.user_id FROM `user` u WHERE u.username BETWEEN '200001' AND '200399');
DELETE FROM `user` WHERE username BETWEEN '200001' AND '200399';
DELETE FROM major WHERE major_code IN (
    'CS03','EM03','ME03','AD03','MS03',
    'LAW01','LAW02','CE01','CE02','EE01','EE02','EE03',
    'CH01','CH02','LS01','LS02','EDU01','EDU02','JR01','JR02','MED01','MED02'
);
DELETE FROM department WHERE dept_code IN ('LAW','CE','EE','CH','LS','EDU','JR','MED');

-- ============================================================
-- PART 1: 院系（14 个）
-- ============================================================
INSERT INTO `department` (`dept_name`, `dept_code`, `description`) VALUES
    ('计算机与人工智能学院', 'CS',  '软件工程、人工智能、数据科学与大数据技术。省级人工智能重点实验室。'),
    ('经济管理学院',         'EM',  '会计学、工商管理、金融学。省级经管实验教学示范中心。'),
    ('外国语学院',           'FL',  '英语、日语。同声传译实验室，承担全校大学外语教学。'),
    ('机械工程学院',         'ME',  '机械设计、车辆工程、智能制造工程。省级智能制造工程中心。'),
    ('数学与统计学院',       'MS',  '数学与应用数学、统计学、数据计算及应用。'),
    ('艺术设计学院',         'AD',  '视觉传达设计、环境设计、数字媒体艺术。艺术创作中心。'),
    ('法学院',               'LAW', '法学、知识产权。模拟法庭，法律援助中心。'),
    ('土木工程学院',         'CE',  '土木工程、工程管理。结构工程省级重点实验室。'),
    ('电子信息学院',         'EE',  '电子信息工程、通信工程、物联网工程。5G 通信实验室。'),
    ('化学化工学院',         'CH',  '化学工程与工艺、应用化学。省级化工工程技术中心。'),
    ('生命科学学院',         'LS',  '生物技术、食品科学与工程。生物工程研究中心。'),
    ('教育学院',             'EDU', '教育学、学前教育。附属实验幼儿园，微格教学中心。'),
    ('新闻传播学院',         'JR',  '新闻学、网络与新媒体。融媒体实验中心。'),
    ('医学院',               'MED', '临床医学（五年制）、护理学。直属附属医院。')
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`), `description` = VALUES(`description`);

SET @d_cs  = (SELECT dept_id FROM department WHERE dept_code = 'CS');
SET @d_em  = (SELECT dept_id FROM department WHERE dept_code = 'EM');
SET @d_fl  = (SELECT dept_id FROM department WHERE dept_code = 'FL');
SET @d_me  = (SELECT dept_id FROM department WHERE dept_code = 'ME');
SET @d_ms  = (SELECT dept_id FROM department WHERE dept_code = 'MS');
SET @d_ad  = (SELECT dept_id FROM department WHERE dept_code = 'AD');
SET @d_law = (SELECT dept_id FROM department WHERE dept_code = 'LAW');
SET @d_ce  = (SELECT dept_id FROM department WHERE dept_code = 'CE');
SET @d_ee  = (SELECT dept_id FROM department WHERE dept_code = 'EE');
SET @d_ch  = (SELECT dept_id FROM department WHERE dept_code = 'CH');
SET @d_ls  = (SELECT dept_id FROM department WHERE dept_code = 'LS');
SET @d_edu = (SELECT dept_id FROM department WHERE dept_code = 'EDU');
SET @d_jr  = (SELECT dept_id FROM department WHERE dept_code = 'JR');
SET @d_med = (SELECT dept_id FROM department WHERE dept_code = 'MED');

-- ============================================================
-- PART 2: 专业（34 个）
-- 热度：🔥热门=报考多报到率高 / ⭐中等 / ❄️冷门=需调剂
-- ============================================================
INSERT INTO `major` (`dept_id`, `major_name`, `major_code`, `cultivation_plan`) VALUES
    (@d_cs,  '软件工程',                'CS01', '全流程软件开发与工程管理。就业率连续五年 98%+。'),
    (@d_cs,  '人工智能',                'CS02', '机器学习、深度学习与智能系统研发。'),
    (@d_cs,  '数据科学与大数据技术',    'CS03', '大数据分析、数据挖掘与智能决策。'),
    (@d_em,  '会计学',      'EM01', '国际会计准则与审计实务，ACCA 方向班。'),
    (@d_em,  '工商管理',    'EM02', '现代企业管理与创新创业，MBA 衔接。'),
    (@d_em,  '金融学',      'EM03', '金融分析与风险管理，CFA 方向。'),
    (@d_fl,  '英语', 'FL01', '翻译、教学与跨文化商务沟通。专八通过率 75%+。'),
    (@d_fl,  '日语', 'FL02', '对日商务与文化交流，N1 通过率 85%。'),
    (@d_me,  '机械设计制造及其自动化', 'ME01', '智能制造装备设计与自动化控制。'),
    (@d_me,  '车辆工程',               'ME02', '新能源汽车与智能网联汽车技术。'),
    (@d_me,  '智能制造工程',           'ME03', '工业 4.0 智能装备与系统集成。'),
    (@d_ms,  '数学与应用数学', 'MS01', '数学建模与应用分析，师范方向。'),
    (@d_ms,  '统计学',         'MS02', '数据分析与统计建模，就业偏经济金融。'),
    (@d_ms,  '数据计算及应用', 'MS03', '科学计算与行业数据建模。'),
    (@d_ad,  '视觉传达设计',   'AD01', '品牌设计、UI/UX 与数字媒体创意。'),
    (@d_ad,  '环境设计',       'AD02', '室内设计、景观与公共空间规划。'),
    (@d_ad,  '数字媒体艺术',   'AD03', '数字内容创意、动画与交互媒体。'),
    (@d_law, '法学',     'LAW01', '法学理论与法律实务，法考通过率 40%。'),
    (@d_law, '知识产权', 'LAW02', '知识产权保护、管理与运营。'),
    (@d_ce,  '土木工程', 'CE01', '建筑/桥梁/隧道工程设计施工管理。'),
    (@d_ce,  '工程管理', 'CE02', '工程技术背景+现代项目管理（BIM/造价）。'),
    (@d_ee,  '电子信息工程', 'EE01', '电子信息系统设计开发与集成。'),
    (@d_ee,  '通信工程',     'EE02', '通信系统与 5G 网络技术。'),
    (@d_ee,  '物联网工程',   'EE03', '物联网架构、嵌入式开发与应用集成。'),
    (@d_ch,  '化学工程与工艺', 'CH01', '化工生产过程优化与技术开发。'),
    (@d_ch,  '应用化学',       'CH02', '化学分析检测与精细化学品开发。'),
    (@d_ls,  '生物技术',       'LS01', '生物技术研发与产业化应用。'),
    (@d_ls,  '食品科学与工程', 'LS02', '食品研发、质量安全与工厂设计。'),
    (@d_edu, '教育学',   'EDU01', '教育管理、课程开发与教学研究。'),
    (@d_edu, '学前教育', 'EDU02', '学前课程设计、儿童发展评估。'),
    (@d_jr,  '新闻学',       'JR01', '全媒体新闻采编、深度报道与媒体运营。'),
    (@d_jr,  '网络与新媒体', 'JR02', '新媒体策划、短视频与社交媒体运营。'),
    (@d_med, '临床医学', 'MED01', '五年制，临床诊疗能力与人文素养。'),
    (@d_med, '护理学',   'MED02', '护理管理与临床护理，三甲医院实习。')
ON DUPLICATE KEY UPDATE `major_name` = VALUES(`major_name`), `dept_id` = VALUES(`dept_id`);

-- 专业 ID 变量
SET @m_cs01  = (SELECT major_id FROM major WHERE major_code = 'CS01');
SET @m_cs02  = (SELECT major_id FROM major WHERE major_code = 'CS02');
SET @m_cs03  = (SELECT major_id FROM major WHERE major_code = 'CS03');
SET @m_em01  = (SELECT major_id FROM major WHERE major_code = 'EM01');
SET @m_em02  = (SELECT major_id FROM major WHERE major_code = 'EM02');
SET @m_em03  = (SELECT major_id FROM major WHERE major_code = 'EM03');
SET @m_fl01  = (SELECT major_id FROM major WHERE major_code = 'FL01');
SET @m_fl02  = (SELECT major_id FROM major WHERE major_code = 'FL02');
SET @m_me01  = (SELECT major_id FROM major WHERE major_code = 'ME01');
SET @m_me02  = (SELECT major_id FROM major WHERE major_code = 'ME02');
SET @m_me03  = (SELECT major_id FROM major WHERE major_code = 'ME03');
SET @m_ms01  = (SELECT major_id FROM major WHERE major_code = 'MS01');
SET @m_ms02  = (SELECT major_id FROM major WHERE major_code = 'MS02');
SET @m_ms03  = (SELECT major_id FROM major WHERE major_code = 'MS03');
SET @m_ad01  = (SELECT major_id FROM major WHERE major_code = 'AD01');
SET @m_ad02  = (SELECT major_id FROM major WHERE major_code = 'AD02');
SET @m_ad03  = (SELECT major_id FROM major WHERE major_code = 'AD03');
SET @m_law01 = (SELECT major_id FROM major WHERE major_code = 'LAW01');
SET @m_law02 = (SELECT major_id FROM major WHERE major_code = 'LAW02');
SET @m_ce01  = (SELECT major_id FROM major WHERE major_code = 'CE01');
SET @m_ce02  = (SELECT major_id FROM major WHERE major_code = 'CE02');
SET @m_ee01  = (SELECT major_id FROM major WHERE major_code = 'EE01');
SET @m_ee02  = (SELECT major_id FROM major WHERE major_code = 'EE02');
SET @m_ee03  = (SELECT major_id FROM major WHERE major_code = 'EE03');
SET @m_ch01  = (SELECT major_id FROM major WHERE major_code = 'CH01');
SET @m_ch02  = (SELECT major_id FROM major WHERE major_code = 'CH02');
SET @m_ls01  = (SELECT major_id FROM major WHERE major_code = 'LS01');
SET @m_ls02  = (SELECT major_id FROM major WHERE major_code = 'LS02');
SET @m_edu01 = (SELECT major_id FROM major WHERE major_code = 'EDU01');
SET @m_edu02 = (SELECT major_id FROM major WHERE major_code = 'EDU02');
SET @m_jr01  = (SELECT major_id FROM major WHERE major_code = 'JR01');
SET @m_jr02  = (SELECT major_id FROM major WHERE major_code = 'JR02');
SET @m_med01 = (SELECT major_id FROM major WHERE major_code = 'MED01');
SET @m_med02 = (SELECT major_id FROM major WHERE major_code = 'MED02');

-- ============================================================
-- PART 3: 年级
-- ============================================================
INSERT IGNORE INTO `grade` (`grade_name`) VALUES ('2022级'),('2023级'),('2024级'),('2025级'),('2026级');
SET @g2022 = (SELECT grade_id FROM grade WHERE grade_name = '2022级' LIMIT 1);
SET @g2023 = (SELECT grade_id FROM grade WHERE grade_name = '2023级' LIMIT 1);
SET @g2024 = (SELECT grade_id FROM grade WHERE grade_name = '2024级' LIMIT 1);
SET @g2025 = (SELECT grade_id FROM grade WHERE grade_name = '2025级' LIMIT 1);
SET @g2026 = (SELECT grade_id FROM grade WHERE grade_name = '2026级' LIMIT 1);

-- ============================================================
-- PART 4: 教职工（~145 人，14 院系 + 校级机关）
-- 职称：教授/副教授/讲师/助教/高级实验师/实验师/研究员/副研究员/助理研究员
-- 职务：院长/副院长/系主任/教研室主任/实验室主任/辅导员/教学秘书/行政助理
-- 状态：95% 在职(1)，5% 退休返聘/离职/停用(0)
-- 统一密码：123321 的 BCrypt
-- ============================================================
SET @pwd = '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy';

-- ---------- 校级机关（无院系归属，dept_id=NULL）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200001', @pwd, 3, '周志远', 1, '13901000001', 'zhouzy@campus.edu.cn', NULL,       '教务处处长',     NULL, 1),
('200002', @pwd, 3, '吴晓华', 2, '13901000002', 'wuxh@campus.edu.cn', NULL,       '教务处副处长',   NULL, 1),
('200003', @pwd, 3, '郑国栋', 1, '13901000003', 'zhenggd@campus.edu.cn', NULL,     '学工部部长',     NULL, 1),
('200004', @pwd, 3, '钱丽萍', 2, '13901000004', 'qianlp@campus.edu.cn', NULL,      '招生办主任',     NULL, 1),
('200005', @pwd, 3, '孙伟民', 1, '13901000005', 'sunwm@campus.edu.cn', NULL,       '就业指导中心主任', NULL, 1),
('200006', @pwd, 3, '李雪琴', 2, '13901000006', 'lixq@campus.edu.cn', NULL,        '人事处处长',     NULL, 1),
('200007', @pwd, 3, '赵永康', 1, '13901000007', 'zhaoyk@campus.edu.cn', NULL,      '财务处处长',     NULL, 1),
('200008', @pwd, 3, '陈慧敏', 2, '13901000008', 'chenhm@campus.edu.cn', NULL,      '信息中心主任',   NULL, 1),
('200009', @pwd, 3, '王守义', 1, '13901000009', 'wangsy@campus.edu.cn', NULL,      '后勤管理处处长', NULL, 1),
('200010', @pwd, 3, '林淑芬', 2, '13901000010', 'linsf@campus.edu.cn', NULL,       '图书馆馆长',     NULL, 0);

-- ---------- 计算机与人工智能学院（大院，12 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200101', @pwd, 2, '张正豪', 1, '13901010001', 'zhangzh@campus.edu.cn', '教授',     '院长',               @d_cs, 1),
('200102', @pwd, 2, '黄丽华', 2, '13901010002', 'huanglh@campus.edu.cn', '教授',    '副院长',             @d_cs, 1),
('200103', @pwd, 2, '陈志强', 1, '13901010003', 'chenzq@campus.edu.cn', '教授',    '系主任（软件工程）',  @d_cs, 1),
('200104', @pwd, 2, '刘明月', 2, '13901010004', 'liumy@campus.edu.cn',   '副教授',  '系主任（人工智能）',  @d_cs, 1),
('200105', @pwd, 2, '杨建国', 1, '13901010005', 'yangjg@campus.edu.cn',  '副教授',  '教研室主任',         @d_cs, 1),
('200106', @pwd, 2, '吴思远', 1, '13901010006', 'wusy@campus.edu.cn',   '讲师',    NULL,                 @d_cs, 1),
('200107', @pwd, 2, '周雪晴', 2, '13901010007', 'zhouxq@campus.edu.cn',  '讲师',    NULL,                 @d_cs, 1),
('200108', @pwd, 2, '郑浩然', 1, '13901010008', 'zhenghr@campus.edu.cn', '讲师',   NULL,                 @d_cs, 1),
('200109', @pwd, 2, '冯晓婷', 2, '13901010009', 'fengxt@campus.edu.cn',  '助教',   NULL,                 @d_cs, 1),
('200110', @pwd, 3, '蒋文斌', 1, '13901010010', 'jiangwb@campus.edu.cn', NULL,     '教学秘书',           @d_cs, 1),
('200111', @pwd, 3, '沈秋月', 2, '13901010011', 'shenqy@campus.edu.cn',  NULL,     '辅导员',             @d_cs, 1),
('200112', @pwd, 2, '马德龙', 1, '13901010012', 'madl@campus.edu.cn',    '副教授',  NULL,                 @d_cs, 0);

-- ---------- 电子信息学院（大院，12 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200121', @pwd, 2, '何志明', 1, '13901020001', 'hezm@campus.edu.cn',   '教授',    '院长',               @d_ee, 1),
('200122', @pwd, 2, '许丽君', 2, '13901020002', 'xulj@campus.edu.cn',   '教授',    '副院长',             @d_ee, 1),
('200123', @pwd, 2, '罗文辉', 1, '13901020003', 'luowh@campus.edu.cn',  '教授',    '系主任（电信）',     @d_ee, 1),
('200124', @pwd, 2, '谢玉兰', 2, '13901020004', 'xieyl@campus.edu.cn',  '副教授',  '系主任（通信）',     @d_ee, 1),
('200125', @pwd, 2, '唐建平', 1, '13901020005', 'tangjp@campus.edu.cn', '副教授',  '教研室主任',         @d_ee, 1),
('200126', @pwd, 2, '曹慧敏', 2, '13901020006', 'caohm@campus.edu.cn',  '讲师',    NULL,                 @d_ee, 1),
('200127', @pwd, 2, '邓志远', 1, '13901020007', 'dengzy@campus.edu.cn', '讲师',    NULL,                 @d_ee, 1),
('200128', @pwd, 2, '彭晓燕', 2, '13901020008', 'pengxy@campus.edu.cn', '讲师',    NULL,                 @d_ee, 1),
('200129', @pwd, 2, '肖国栋', 1, '13901020009', 'xiaogd@campus.edu.cn', '助教',    NULL,                 @d_ee, 1),
('200130', @pwd, 3, '田永康', 1, '13901020010', 'tianyk@campus.edu.cn', NULL,      '实验室主任',         @d_ee, 1),
('200131', @pwd, 3, '袁丽华', 2, '13901020011', 'yuanlh@campus.edu.cn', NULL,      '辅导员',             @d_ee, 1),
('200132', @pwd, 2, '范志强', 1, '13901020012', 'fanzq@campus.edu.cn',  '讲师',    NULL,                 @d_ee, 0);

-- ---------- 经济管理学院（大院，12 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200141', @pwd, 2, '高铭远', 1, '13901030001', 'gaomy@campus.edu.cn',   '教授',   '院长',              @d_em, 1),
('200142', @pwd, 2, '蔡雅文', 2, '13901030002', 'caiyw@campus.edu.cn',   '教授',   '副院长',            @d_em, 1),
('200143', @pwd, 2, '潘志伟', 1, '13901030003', 'panzw@campus.edu.cn',   '教授',   '系主任（会计学）',  @d_em, 1),
('200144', @pwd, 2, '董丽娜', 2, '13901030004', 'dongln@campus.edu.cn',  '副教授', '系主任（金融学）',  @d_em, 1),
('200145', @pwd, 2, '苏建平', 1, '13901030005', 'sujp@campus.edu.cn',   '副教授', '教研室主任',        @d_em, 1),
('200146', @pwd, 2, '叶思源', 1, '13901030006', 'yesy@campus.edu.cn',   '讲师',   NULL,                @d_em, 1),
('200147', @pwd, 2, '卢晓琳', 2, '13901030007', 'luxl@campus.edu.cn',   '讲师',   NULL,                @d_em, 1),
('200148', @pwd, 2, '任志强', 1, '13901030008', 'renzq@campus.edu.cn',  '讲师',   NULL,                @d_em, 1),
('200149', @pwd, 3, '姜雪梅', 2, '13901030009', 'jiangxm@campus.edu.cn', NULL,    '教学秘书',          @d_em, 1),
('200150', @pwd, 3, '廖永康', 1, '13901030010', 'liaoyk@campus.edu.cn', NULL,     '辅导员',            @d_em, 1),
('200151', @pwd, 2, '邱淑贞', 2, '13901030011', 'qiusz@campus.edu.cn',  '助教',   NULL,                @d_em, 1),
('200152', @pwd, 2, '韦建国', 1, '13901030012', 'weijg@campus.edu.cn',  '副教授', NULL,                 @d_em, 0);

-- ---------- 机械工程学院（大院，11 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200161', @pwd, 2, '雷振华', 1, '13901040001', 'leizh@campus.edu.cn',   '教授',   '院长',              @d_me, 1),
('200162', @pwd, 2, '贺丽萍', 2, '13901040002', 'help@campus.edu.cn',    '教授',   '副院长',            @d_me, 1),
('200163', @pwd, 2, '严志强', 1, '13901040003', 'yanzq@campus.edu.cn',   '教授',   '系主任（机械）',    @d_me, 1),
('200164', @pwd, 2, '覃晓燕', 2, '13901040004', 'qinxy@campus.edu.cn',   '副教授', '系主任（车辆）',    @d_me, 1),
('200165', @pwd, 2, '邹建平', 1, '13901040005', 'zoujp@campus.edu.cn',   '副教授', NULL,                @d_me, 1),
('200166', @pwd, 2, '石思远', 1, '13901040006', 'shisy@campus.edu.cn',   '讲师',   NULL,                @d_me, 1),
('200167', @pwd, 2, '崔雨桐', 2, '13901040007', 'cuiyt@campus.edu.cn',   '讲师',   NULL,                @d_me, 1),
('200168', @pwd, 2, '贾浩然', 1, '13901040008', 'jiahr@campus.edu.cn',   '讲师',   NULL,                @d_me, 1),
('200169', @pwd, 3, '邱永康', 1, '13901040009', 'qiuyk@campus.edu.cn',   NULL,     '实验员',            @d_me, 1),
('200170', @pwd, 3, '韦晓月', 2, '13901040010', 'weixy@campus.edu.cn',   NULL,     '辅导员',            @d_me, 1),
('200171', @pwd, 2, '鲁志明', 1, '13901040011', 'luzm@campus.edu.cn',    '讲师',   NULL,                @d_me, 0);

-- ---------- 医学院（中院，11 人，临床+护理）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200181', @pwd, 2, '龚正清', 1, '13901050001', 'gongzq@campus.edu.cn',  '教授',     '院长',              @d_med, 1),
('200182', @pwd, 2, '邱雅琴', 2, '13901050002', 'qiuyq@campus.edu.cn',   '教授',     '副院长',            @d_med, 1),
('200183', @pwd, 2, '龙建平', 1, '13901050003', 'longjp@campus.edu.cn',  '教授',     '系主任（临床）',    @d_med, 1),
('200184', @pwd, 2, '万慧心', 2, '13901050004', 'wanhx@campus.edu.cn',   '副教授',   '系主任（护理）',    @d_med, 1),
('200185', @pwd, 2, '段文龙', 1, '13901050005', 'duanwl@campus.edu.cn',  '副教授',   '教研室主任',        @d_med, 1),
('200186', @pwd, 2, '车晓琳', 2, '13901050006', 'chexl@campus.edu.cn',   '讲师',     NULL,                @d_med, 1),
('200187', @pwd, 2, '冉志远', 1, '13901050007', 'ranzy@campus.edu.cn',   '讲师',     NULL,                @d_med, 1),
('200188', @pwd, 2, '白露晞', 2, '13901050008', 'bailx@campus.edu.cn',   '讲师',     NULL,                @d_med, 1),
('200189', @pwd, 3, '匡永康', 1, '13901050009', 'kuangyk@campus.edu.cn', NULL,       '实验室主任',        @d_med, 1),
('200190', @pwd, 3, '揭晓月', 2, '13901050010', 'jiexy@campus.edu.cn',   NULL,       '教学秘书',          @d_med, 1),
('200191', @pwd, 2, '邢锦华', 1, '13901050011', 'xingjh@campus.edu.cn',  '助教',     NULL,                @d_med, 0);

-- ---------- 土木工程学院（中院，10 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200201', @pwd, 2, '侯景行', 1, '13901060001', 'houjx@campus.edu.cn',   '教授',   '院长',            @d_ce, 1),
('200202', @pwd, 2, '游慧心', 2, '13901060002', 'youhx@campus.edu.cn',   '教授',   '副院长',          @d_ce, 1),
('200203', @pwd, 2, '练志远', 1, '13901060003', 'lianzy@campus.edu.cn',  '副教授', '系主任（土木）',  @d_ce, 1),
('200204', @pwd, 2, '辜玉兰', 2, '13901060004', 'guyl@campus.edu.cn',    '副教授', '系主任（工管）',  @d_ce, 1),
('200205', @pwd, 2, '植建平', 1, '13901060005', 'zhijp@campus.edu.cn',   '副教授', NULL,              @d_ce, 1),
('200206', @pwd, 2, '涂思源', 1, '13901060006', 'tusy@campus.edu.cn',    '讲师',   NULL,              @d_ce, 1),
('200207', @pwd, 2, '蓝晓楠', 2, '13901060007', 'lanxn@campus.edu.cn',   '讲师',   NULL,              @d_ce, 1),
('200208', @pwd, 3, '谷永康', 1, '13901060008', 'guyk@campus.edu.cn',    NULL,     '实验员',          @d_ce, 1),
('200209', @pwd, 3, '荆秋月', 2, '13901060009', 'jingqy@campus.edu.cn',  NULL,     '辅导员',          @d_ce, 1),
('200210', @pwd, 2, '冼立诚', 1, '13901060010', 'xianlc@campus.edu.cn',  '助教',   NULL,              @d_ce, 0);

-- ---------- 法学院（中院，9 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200221', @pwd, 2, '简青松', 1, '13901070001', 'jianqs@campus.edu.cn',  '教授',   '院长',           @d_law, 1),
('200222', @pwd, 2, '岳思娴', 2, '13901070002', 'yuesx@campus.edu.cn',   '教授',   '副院长',         @d_law, 1),
('200223', @pwd, 2, '荣博韬', 1, '13901070003', 'rongbt@campus.edu.cn',  '副教授', '系主任（法学）', @d_law, 1),
('200224', @pwd, 2, '池映月', 2, '13901070004', 'chiyy@campus.edu.cn',   '副教授', '系主任（知产）', @d_law, 1),
('200225', @pwd, 2, '巫承志', 1, '13901070005', 'wucz@campus.edu.cn',    '讲师',   NULL,             @d_law, 1),
('200226', @pwd, 2, '娄晓晴', 2, '13901070006', 'louxq@campus.edu.cn',   '讲师',   NULL,             @d_law, 1),
('200227', @pwd, 3, '瞿建华', 1, '13901070007', 'qujh@campus.edu.cn',    NULL,     '教学秘书',       @d_law, 1),
('200228', @pwd, 3, '鄢秋月', 2, '13901070008', 'yanqy@campus.edu.cn',   NULL,     '辅导员',         @d_law, 1),
('200229', @pwd, 2, '冷文博', 1, '13901070009', 'lengwb@campus.edu.cn',  '助教',   NULL,             @d_law, 0);

-- ---------- 化学化工学院（中小院，9 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200241', @pwd, 2, '钱玉成', 1, '13901080001', 'qianyc@campus.edu.cn',  '教授',   '院长',           @d_ch, 1),
('200242', @pwd, 2, '薛若兰', 2, '13901080002', 'xuerl@campus.edu.cn',   '教授',   '副院长',         @d_ch, 1),
('200243', @pwd, 2, '于立诚', 1, '13901080003', 'yulc@campus.edu.cn',    '副教授', '系主任（化工）', @d_ch, 1),
('200244', @pwd, 2, '东方明霞', 2, '13901080004', 'dongfmx@campus.edu.cn','副教授', '系主任（应化）', @d_ch, 1),
('200245', @pwd, 2, '邱思远', 1, '13901080005', 'qiusy@campus.edu.cn',   '讲师',   NULL,             @d_ch, 1),
('200246', @pwd, 2, '兰美琪', 2, '13901080006', 'lanmq@campus.edu.cn',   '讲师',   NULL,             @d_ch, 1),
('200247', @pwd, 3, '佘建华', 1, '13901080007', 'shejh@campus.edu.cn',   NULL,     '实验员',         @d_ch, 1),
('200248', @pwd, 3, '全子轩', 2, '13901080008', 'quanzx@campus.edu.cn',  NULL,     '教学秘书',       @d_ch, 1),
('200249', @pwd, 2, '聂博文', 1, '13901080009', 'niebw@campus.edu.cn',   '助教',   NULL,             @d_ch, 0);

-- ---------- 生命科学学院（中小院，8 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200261', @pwd, 2, '成秋怡', 2, '13901090001', 'chengqy@campus.edu.cn', '教授',   '院长',            @d_ls, 1),
('200262', @pwd, 2, '葛承志', 1, '13901090002', 'gecz@campus.edu.cn',    '教授',   '副院长',          @d_ls, 1),
('200263', @pwd, 2, '曾子涵', 2, '13901090003', 'zengzh@campus.edu.cn',  '副教授', '系主任（生技）',  @d_ls, 1),
('200264', @pwd, 2, '邵明哲', 1, '13901090004', 'shaomz@campus.edu.cn',  '副教授', '系主任（食品）',  @d_ls, 1),
('200265', @pwd, 2, '栾晓琳', 2, '13901090005', 'luanxl@campus.edu.cn',  '讲师',   NULL,              @d_ls, 1),
('200266', @pwd, 2, '别天宇', 1, '13901090006', 'biety@campus.edu.cn',   '讲师',   NULL,              @d_ls, 1),
('200267', @pwd, 3, '燕俊彦', 1, '13901090007', 'yanjy@campus.edu.cn',   NULL,     '实验员',          @d_ls, 1),
('200268', @pwd, 3, '赫秋实', 2, '13901090008', 'heqs@campus.edu.cn',    NULL,     '教学秘书',        @d_ls, 1);

-- ---------- 教育学院（中小院，8 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200281', @pwd, 2, '敖思远', 1, '13901100001', 'aosy@campus.edu.cn',   '教授',   '院长',            @d_edu, 1),
('200282', @pwd, 2, '安柳依', 2, '13901100002', 'anly@campus.edu.cn',   '教授',   '副院长',          @d_edu, 1),
('200283', @pwd, 2, '冷博韬', 1, '13901100003', 'lengbt@campus.edu.cn', '副教授', '系主任（教育）',  @d_edu, 1),
('200284', @pwd, 2, '池玉兰', 2, '13901100004', 'chiyl@campus.edu.cn',  '副教授', '系主任（学前）',  @d_edu, 1),
('200285', @pwd, 2, '乐晓楠', 2, '13901100005', 'lexn@campus.edu.cn',   '讲师',   NULL,              @d_edu, 1),
('200286', @pwd, 2, '巴锦程', 1, '13901100006', 'bajc@campus.edu.cn',   '讲师',   NULL,              @d_edu, 1),
('200287', @pwd, 3, '宗建华', 1, '13901100007', 'zongjh@campus.edu.cn',  NULL,    '行政助理',        @d_edu, 1),
('200288', @pwd, 2, '游思琪', 2, '13901100008', 'yousq@campus.edu.cn',  '助教',   NULL,              @d_edu, 0);

-- ---------- 新闻传播学院（中小院，8 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200301', @pwd, 2, '徐雨桐', 2, '13901110001', 'xuyt@campus.edu.cn',   '教授',   '院长',            @d_jr, 1),
('200302', @pwd, 2, '国锦华', 1, '13901110002', 'guojh@campus.edu.cn',  '教授',   '副院长',          @d_jr, 1),
('200303', @pwd, 2, '邢明霞', 2, '13901110003', 'xingmx@campus.edu.cn', '副教授', '系主任（新闻）',  @d_jr, 1),
('200304', @pwd, 2, '哈志远', 1, '13901110004', 'hazy@campus.edu.cn',   '副教授', '系主任（网新）',  @d_jr, 1),
('200305', @pwd, 2, '曾立诚', 1, '13901110005', 'zenglc@campus.edu.cn',  '讲师',  NULL,              @d_jr, 1),
('200306', @pwd, 3, '鄂若兰', 2, '13901110006', 'erl@campus.edu.cn',    NULL,     '教学秘书',        @d_jr, 1),
('200307', @pwd, 3, '楼玉成', 1, '13901110007', 'louyc@campus.edu.cn',  NULL,     '设备管理员',      @d_jr, 1),
('200308', @pwd, 2, '昝晓晴', 2, '13901110008', 'zanxq@campus.edu.cn',  '助教',   NULL,              @d_jr, 0);

-- ---------- 外国语学院（中小院，9 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200321', @pwd, 2, '顾婉清', 2, '13901120001', 'guwq@campus.edu.cn',   '教授',   '院长',           @d_fl, 1),
('200322', @pwd, 2, '温博文', 1, '13901120002', 'wenbw@campus.edu.cn',  '教授',   '副院长',         @d_fl, 1),
('200323', @pwd, 2, '叶知秋', 2, '13901120003', 'yezq@campus.edu.cn',   '副教授', '系主任（英语）', @d_fl, 1),
('200324', @pwd, 2, '姚锦程', 1, '13901120004', 'yaojc@campus.edu.cn',  '副教授', '系主任（日语）', @d_fl, 1),
('200325', @pwd, 2, '乔若兰', 2, '13901120005', 'qiaorl@campus.edu.cn', '讲师',   NULL,             @d_fl, 1),
('200326', @pwd, 2, '傅长庚', 1, '13901120006', 'fucg@campus.edu.cn',   '讲师',   NULL,             @d_fl, 1),
('200327', @pwd, 3, '常雅琴', 2, '13901120007', 'changyq@campus.edu.cn', NULL,    '行政助理',       @d_fl, 1),
('200328', @pwd, 3, '龙晓峰', 1, '13901120008', 'longxf@campus.edu.cn', NULL,     '语音室管理员',   @d_fl, 1),
('200329', @pwd, 2, '米思琪', 2, '13901120009', 'misq@campus.edu.cn',   '助教',   NULL,             @d_fl, 0);

-- ---------- 数学与统计学院（小院，8 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200341', @pwd, 2, '龚明远', 1, '13901130001', 'gongmy@campus.edu.cn',  '教授',   '院长',            @d_ms, 1),
('200342', @pwd, 2, '廖雨菲', 2, '13901130002', 'liaoyf@campus.edu.cn',  '副教授', '副院长',          @d_ms, 1),
('200343', @pwd, 2, '范立诚', 1, '13901130003', 'fanlc@campus.edu.cn',   '副教授', '系主任（数学）',  @d_ms, 1),
('200344', @pwd, 2, '白露晞', 2, '13901130004', 'bailx@campus.edu.cn',   '讲师',   '系主任（统计）',  @d_ms, 1),
('200345', @pwd, 2, '骆天宇', 1, '13901130005', 'luoty@campus.edu.cn',   '讲师',   NULL,              @d_ms, 1),
('200346', @pwd, 2, '牛秋实', 2, '13901130006', 'niuqs@campus.edu.cn',   '讲师',   NULL,              @d_ms, 1),
('200347', @pwd, 3, '查永康', 1, '13901130007', 'chayk@campus.edu.cn',   NULL,     '行政助理',        @d_ms, 1),
('200348', @pwd, 2, '银晓燕', 2, '13901130008', 'yinxy@campus.edu.cn',   '助教',   NULL,              @d_ms, 0);

-- ---------- 艺术设计学院（小院，8 人）----------
INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
('200361', @pwd, 2, '夏柳依', 2, '13901140001', 'xialy@campus.edu.cn',   '教授',   '院长',            @d_ad, 1),
('200362', @pwd, 2, '费玉成', 1, '13901140002', 'feiyc@campus.edu.cn',   '教授',   '副院长',          @d_ad, 1),
('200363', @pwd, 2, '褚明霞', 2, '13901140003', 'chumx@campus.edu.cn',   '副教授', '系主任（视传）',  @d_ad, 1),
('200364', @pwd, 2, '尤子涵', 1, '13901140004', 'youzh@campus.edu.cn',   '副教授', '系主任（环设）',  @d_ad, 1),
('200365', @pwd, 2, '耿博文', 1, '13901140005', 'gengbw@campus.edu.cn',  '讲师',   NULL,              @d_ad, 1),
('200366', @pwd, 2, '米晓楠', 2, '13901140006', 'mixn@campus.edu.cn',    '讲师',   NULL,              @d_ad, 1),
('200367', @pwd, 3, '崔建平', 1, '13901140007', 'cuijp@campus.edu.cn',   NULL,     '设备管理员',      @d_ad, 1),
('200368', @pwd, 2, '万雨桐', 2, '13901140008', 'wanyt@campus.edu.cn',   '助教',   NULL,              @d_ad, 0);

-- 分配教职工角色（user_type=2→TEACHER, user_type=3→STAFF）
INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON (
    (u.user_type = 2 AND r.role_code = 'TEACHER') OR
    (u.user_type = 3 AND r.role_code = 'STAFF')
) WHERE u.username BETWEEN '200001' AND '200368';

SELECT '教职工总数: ' AS label, COUNT(*) AS cnt FROM `user` WHERE username BETWEEN '200001' AND '200368';

-- ============================================================
-- PART 5: 学生生成存储过程
-- 使用大规模真实姓名库 + 11 位手机号 + 真实地址
-- 注意：MySQL 存储过程内 CASE WHEN 嵌套 RAND() 可能触发解析器问题，
-- 这里全部改用 IF/ELSEIF + 临时变量避免复杂嵌套
-- ============================================================
DROP PROCEDURE IF EXISTS gen_students;
DELIMITER //

CREATE PROCEDURE gen_students(
    IN base_no BIGINT,
    IN p_grade_id BIGINT,
    IN p_enroll_year INT,
    IN p_dept_id BIGINT,
    IN p_major_id BIGINT,
    IN p_class_prefix VARCHAR(16),
    IN p_cnt INT,
    IN p_provinces TEXT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_r1 DOUBLE;
    DECLARE v_r2 DOUBLE;
    DECLARE v_r3 DOUBLE;
    DECLARE v_gender TINYINT;
    DECLARE v_surname VARCHAR(4);
    DECLARE v_given_name VARCHAR(16);
    DECLARE v_birth DATE;
    DECLARE v_age INT;
    DECLARE v_phone VARCHAR(11);
    DECLARE v_addr VARCHAR(64);
    DECLARE v_province VARCHAR(16);
    DECLARE v_class_name VARCHAR(24);
    DECLARE v_status TINYINT;
    DECLARE v_total_provinces INT;
    DECLARE v_birth_year INT;
    DECLARE v_birth_month INT;
    DECLARE v_birth_day INT;
    DECLARE v_name_type INT;

    SET v_total_provinces = CHAR_LENGTH(p_provinces) - CHAR_LENGTH(REPLACE(p_provinces, ',', '')) + 1;

    WHILE i < p_cnt DO
        -- 预生成随机数，避免 RAND() 嵌套
        SET v_r1 = RAND();
        SET v_r2 = RAND();
        SET v_r3 = RAND();

        -- 性别：~52% 男
        SET v_gender = IF(v_r1 < 0.52, 1, 2);

        -- 姓氏（80 个常见姓）
        SET v_surname = ELT(1+FLOOR(v_r2*80),
            '王','李','张','刘','陈','杨','黄','赵','吴','周',
            '徐','孙','马','朱','胡','郭','何','高','林','罗',
            '郑','梁','谢','宋','唐','韩','曹','许','邓','冯',
            '萧','程','蔡','彭','潘','袁','于','董','叶','蒋',
            '杜','苏','魏','吕','田','丁','沈','姜','范','江',
            '傅','钟','卢','汪','戴','崔','任','陆','廖','姚',
            '方','金','邱','夏','谭','韦','贾','邹','石','熊',
            '孟','秦','阎','薛','侯','雷','白','龙','段','郝');

        -- 名字类型：0=双字 1=单字 2=预定义双字名
        SET v_name_type = FLOOR(v_r3*3);

        IF v_gender = 1 THEN
            -- 男名
            IF v_name_type = 0 THEN
                SET v_given_name = CONCAT(
                    ELT(1+FLOOR(RAND()*30), '伟','强','磊','涛','浩','宇','轩','铭','博','文',
                        '杰','鹏','飞','洋','军','勇','斌','峰','辉','毅',
                        '恒','睿','哲','宸','彦','楷','泽','廷','霖','龙'),
                    ELT(1+FLOOR(RAND()*30), '伟','强','磊','涛','浩','宇','轩','铭','博','文',
                        '杰','鹏','飞','洋','军','勇','斌','峰','辉','毅',
                        '恒','睿','哲','宸','彦','楷','泽','廷','霖','龙'));
            ELSEIF v_name_type = 1 THEN
                SET v_given_name = ELT(1+FLOOR(RAND()*30), '伟','强','磊','涛','浩','宇','轩','铭','博','文',
                    '杰','鹏','飞','洋','军','勇','斌','峰','辉','毅',
                    '恒','睿','哲','宸','彦','楷','泽','廷','霖','龙');
            ELSE
                SET v_given_name = ELT(1+FLOOR(RAND()*19), '一鸣','天宇','浩然','子涵','思远','志远','明哲',
                    '俊杰','博文','晓峰','建国','志强','建华','国栋','永康','文斌','锦程','立诚','景行');
            END IF;
        ELSE
            -- 女名
            IF v_name_type = 0 THEN
                SET v_given_name = CONCAT(
                    ELT(1+FLOOR(RAND()*30), '婷','雪','琳','敏','静','丽','娟','芳','霞','艳',
                        '娜','媛','莉','萍','红','玲','慧','琴','云','莹',
                        '怡','欣','雯','瑶','萱','颖','妍','丹','洁','秀'),
                    ELT(1+FLOOR(RAND()*30), '婷','雪','琳','敏','静','丽','娟','芳','霞','艳',
                        '娜','媛','莉','萍','红','玲','慧','琴','云','莹',
                        '怡','欣','雯','瑶','萱','颖','妍','丹','洁','秀'));
            ELSEIF v_name_type = 1 THEN
                SET v_given_name = ELT(1+FLOOR(RAND()*30), '婷','雪','琳','敏','静','丽','娟','芳','霞','艳',
                    '娜','媛','莉','萍','红','玲','慧','琴','云','莹',
                    '怡','欣','雯','瑶','萱','颖','妍','丹','洁','秀');
            ELSE
                SET v_given_name = ELT(1+FLOOR(RAND()*18), '雨涵','思琪','婉清','若兰','晓月','秋怡','雅琴',
                    '慧心','柳依','明霞','玉兰','露晞','映月','美琪','思源','晓琳','雨桐','诗雨');
            END IF;
        END IF;

        -- 生日：入学年-18±1
        SET v_birth_year = p_enroll_year - 18 + FLOOR(RAND()*3) - 1;
        SET v_birth_month = 1 + FLOOR(RAND()*12);
        SET v_birth_day = 1 + FLOOR(RAND()*28);
        SET v_birth = STR_TO_DATE(CONCAT(v_birth_year,'-',LPAD(v_birth_month,2,'0'),'-',LPAD(v_birth_day,2,'0')), '%Y-%m-%d');
        SET v_age = 2026 - v_birth_year;

        -- 11 位手机号（27 个真实号段）
        SET v_phone = CONCAT(
            ELT(1+FLOOR(RAND()*27), '130','131','132','135','136','137','138','139',
                '150','151','152','158','159','166','176','177','178',
                '182','183','185','186','187','188','189','191','198','199'),
            LPAD(FLOOR(RAND()*100000000), 8, '0'));

        -- 生源地（轮询分配）
        SET v_province = SUBSTRING_INDEX(SUBSTRING_INDEX(p_provinces, ',', 1 + (i % v_total_provinces)), ',', -1);
        SET v_addr = CONCAT(v_province,
            ELT(1+FLOOR(RAND()*8), '市第一区','市第二区','市新城区','市高新区','市开发区','市老城区','县','县级市'));

        -- 班级名（每班约 40 人）
        SET v_class_name = CONCAT(p_class_prefix, LPAD(1 + FLOOR(i / 40), 2, '0'));

        -- 学籍状态
        IF p_enroll_year = 2022 THEN
            SET v_r1 = RAND();
            IF v_r1 < 0.87 THEN SET v_status = 3;
            ELSEIF v_r1 < 0.92 THEN SET v_status = 1;
            ELSEIF v_r1 < 0.95 THEN SET v_status = 2;
            ELSE SET v_status = 0;
            END IF;
        ELSEIF p_enroll_year IN (2023, 2024, 2025) THEN
            SET v_r1 = RAND();
            IF v_r1 < 0.96 THEN SET v_status = 1;
            ELSEIF v_r1 < 0.98 THEN SET v_status = 2;
            ELSE SET v_status = 0;
            END IF;
        ELSE
            SET v_status = 1;
        END IF;

        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, phone, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i, CONCAT(v_surname, v_given_name), v_gender, v_birth, v_age,
            v_addr, v_phone, p_grade_id, p_dept_id, p_major_id, v_class_name,
            v_province, p_enroll_year, v_status
        );

        SET i = i + 1;
    END WHILE;
END//

DELIMITER ;

-- ============================================================
-- PART 6: 生成学生（按专业×年级，总计 10,200 人）
-- 院系规模：CS≈1540 EE≈1270 EM≈1250 ME≈1080 MED≈650 CE≈625
--           LAW≈496 CH≈498 LS≈442 EDU≈420 JR≈412 FL≈424 MS≈486 AD≈484
-- ============================================================

-- ============ 计算机与人工智能学院（大院 ~1540 人）============
-- 🔥CS01 软件工程（最热门）
CALL gen_students(2022100000, @g2022, 2022, @d_cs, @m_cs01, '软件22', 148, '山东,河南,江苏,浙江,广东,四川,湖北,安徽,湖南,河北');
CALL gen_students(2023100000, @g2023, 2023, @d_cs, @m_cs01, '软件23', 152, '山东,河南,江苏,浙江,广东,四川,湖北,安徽,湖南,河北,陕西');
CALL gen_students(2024100000, @g2024, 2024, @d_cs, @m_cs01, '软件24', 156, '山东,河南,江苏,浙江,广东,四川,湖北,安徽,湖南,河北,陕西,福建');
CALL gen_students(2025100000, @g2025, 2025, @d_cs, @m_cs01, '软件25', 160, '山东,河南,江苏,浙江,广东,四川,湖北,安徽,湖南,河北,陕西,福建,江西');
-- 🔥CS02 人工智能
CALL gen_students(2022100200, @g2022, 2022, @d_cs, @m_cs02, '智能22', 122, '北京,上海,广东,浙江,江苏,湖北,四川,陕西,山东,天津');
CALL gen_students(2023100200, @g2023, 2023, @d_cs, @m_cs02, '智能23', 128, '北京,上海,广东,浙江,江苏,湖北,四川,陕西,山东,天津,重庆,湖南');
CALL gen_students(2024100200, @g2024, 2024, @d_cs, @m_cs02, '智能24', 132, '北京,上海,广东,浙江,江苏,湖北,四川,陕西,山东,天津,重庆,湖南,辽宁');
CALL gen_students(2025100200, @g2025, 2025, @d_cs, @m_cs02, '智能25', 136, '北京,上海,广东,浙江,江苏,湖北,四川,陕西,山东,天津,重庆,湖南,辽宁,福建');
-- 🔥CS03 大数据
CALL gen_students(2022100400, @g2022, 2022, @d_cs, @m_cs03, '数据22', 98, '贵州,云南,广西,甘肃,山西,内蒙古,江西,吉林,黑龙江,新疆');
CALL gen_students(2023100400, @g2023, 2023, @d_cs, @m_cs03, '数据23', 104, '贵州,云南,广西,甘肃,山西,内蒙古,江西,吉林,黑龙江,新疆,海南');
CALL gen_students(2024100400, @g2024, 2024, @d_cs, @m_cs03, '数据24', 110, '贵州,云南,广西,甘肃,山西,内蒙古,江西,吉林,黑龙江,新疆,海南,宁夏');
CALL gen_students(2025100400, @g2025, 2025, @d_cs, @m_cs03, '数据25', 115, '贵州,云南,广西,甘肃,山西,内蒙古,江西,吉林,黑龙江,新疆,海南,宁夏,青海');

-- ============ 电子信息学院（大院 ~1270 人）============
-- 🔥EE01 电子信息工程
CALL gen_students(2022100600, @g2022, 2022, @d_ee, @m_ee01, '电信22', 118, '四川,重庆,湖北,湖南,河南,山东,安徽,江西,广东,广西');
CALL gen_students(2023100600, @g2023, 2023, @d_ee, @m_ee01, '电信23', 123, '四川,重庆,湖北,湖南,河南,山东,安徽,江西,广东,广西,贵州');
CALL gen_students(2024100600, @g2024, 2024, @d_ee, @m_ee01, '电信24', 128, '四川,重庆,湖北,湖南,河南,山东,安徽,江西,广东,广西,贵州,云南');
CALL gen_students(2025100600, @g2025, 2025, @d_ee, @m_ee01, '电信25', 132, '四川,重庆,湖北,湖南,河南,山东,安徽,江西,广东,广西,贵州,云南,甘肃');
-- 🔥EE02 通信工程
CALL gen_students(2022100800, @g2022, 2022, @d_ee, @m_ee02, '通信22', 100, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,河北,山西');
CALL gen_students(2023100800, @g2023, 2023, @d_ee, @m_ee02, '通信23', 105, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,河北,山西,陕西');
CALL gen_students(2024100800, @g2024, 2024, @d_ee, @m_ee02, '通信24', 108, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,河北,山西,陕西,辽宁');
CALL gen_students(2025100800, @g2025, 2025, @d_ee, @m_ee02, '通信25', 110, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,河北,山西,陕西,辽宁,吉林');
-- 🔥EE03 物联网工程
CALL gen_students(2022101000, @g2022, 2022, @d_ee, @m_ee03, '物联22', 86, '北京,天津,上海,重庆,江苏,浙江,广东,四川,湖北,山东');
CALL gen_students(2023101000, @g2023, 2023, @d_ee, @m_ee03, '物联23', 90, '北京,天津,上海,重庆,江苏,浙江,广东,四川,湖北,山东,辽宁');
CALL gen_students(2024101000, @g2024, 2024, @d_ee, @m_ee03, '物联24', 95, '北京,天津,上海,重庆,江苏,浙江,广东,四川,湖北,山东,辽宁,陕西');
CALL gen_students(2025101000, @g2025, 2025, @d_ee, @m_ee03, '物联25', 100, '北京,天津,上海,重庆,江苏,浙江,广东,四川,湖北,山东,辽宁,陕西,福建');

-- ============ 经济管理学院（大院 ~1250 人）============
-- 🔥EM01 会计学
CALL gen_students(2022101200, @g2022, 2022, @d_em, @m_em01, '会计22', 112, '浙江,福建,广东,江苏,山东,四川,湖北,湖南,河南,安徽');
CALL gen_students(2023101200, @g2023, 2023, @d_em, @m_em01, '会计23', 118, '浙江,福建,广东,江苏,山东,四川,湖北,湖南,河南,安徽,江西');
CALL gen_students(2024101200, @g2024, 2024, @d_em, @m_em01, '会计24', 122, '浙江,福建,广东,江苏,山东,四川,湖北,湖南,河南,安徽,江西,上海');
CALL gen_students(2025101200, @g2025, 2025, @d_em, @m_em01, '会计25', 128, '浙江,福建,广东,江苏,山东,四川,湖北,湖南,河南,安徽,江西,上海,北京');
-- 🔥EM03 金融学
CALL gen_students(2022101400, @g2022, 2022, @d_em, @m_em03, '金融22', 98, '上海,北京,深圳,广州,杭州,南京,成都,武汉,天津,重庆');
CALL gen_students(2023101400, @g2023, 2023, @d_em, @m_em03, '金融23', 104, '上海,北京,深圳,广州,杭州,南京,成都,武汉,天津,重庆,青岛,苏州');
CALL gen_students(2024101400, @g2024, 2024, @d_em, @m_em03, '金融24', 110, '上海,北京,深圳,广州,杭州,南京,成都,武汉,天津,重庆,青岛,苏州,宁波');
CALL gen_students(2025101400, @g2025, 2025, @d_em, @m_em03, '金融25', 115, '上海,北京,深圳,广州,杭州,南京,成都,武汉,天津,重庆,青岛,苏州,宁波,西安');
-- ⭐EM02 工商管理
CALL gen_students(2022101600, @g2022, 2022, @d_em, @m_em02, '工管22', 85, '山东,河南,河北,山西,陕西,甘肃,宁夏,内蒙古,辽宁,吉林');
CALL gen_students(2023101600, @g2023, 2023, @d_em, @m_em02, '工管23', 90, '山东,河南,河北,山西,陕西,甘肃,宁夏,内蒙古,辽宁,吉林,黑龙江');
CALL gen_students(2024101600, @g2024, 2024, @d_em, @m_em02, '工管24', 94, '山东,河南,河北,山西,陕西,甘肃,宁夏,内蒙古,辽宁,吉林,黑龙江,新疆');
CALL gen_students(2025101600, @g2025, 2025, @d_em, @m_em02, '工管25', 96, '山东,河南,河北,山西,陕西,甘肃,宁夏,内蒙古,辽宁,吉林,黑龙江,新疆,青海');

-- ============ 机械工程学院（大院 ~1080 人）============
-- ⭐ME01 机械设计制造及其自动化
CALL gen_students(2022101800, @g2022, 2022, @d_me, @m_me01, '机械22', 114, '辽宁,吉林,黑龙江,山东,河南,河北,山西,江苏,安徽,湖北');
CALL gen_students(2023101800, @g2023, 2023, @d_me, @m_me01, '机械23', 118, '辽宁,吉林,黑龙江,山东,河南,河北,山西,江苏,安徽,湖北,湖南');
CALL gen_students(2024101800, @g2024, 2024, @d_me, @m_me01, '机械24', 122, '辽宁,吉林,黑龙江,山东,河南,河北,山西,江苏,安徽,湖北,湖南,四川');
CALL gen_students(2025101800, @g2025, 2025, @d_me, @m_me01, '机械25', 124, '辽宁,吉林,黑龙江,山东,河南,河北,山西,江苏,安徽,湖北,湖南,四川,重庆');
-- ⭐ME02 车辆工程
CALL gen_students(2022102000, @g2022, 2022, @d_me, @m_me02, '车辆22', 76, '吉林,湖北,广东,上海,重庆,北京,天津,广西,安徽,江西');
CALL gen_students(2023102000, @g2023, 2023, @d_me, @m_me02, '车辆23', 80, '吉林,湖北,广东,上海,重庆,北京,天津,广西,安徽,江西,陕西');
CALL gen_students(2024102000, @g2024, 2024, @d_me, @m_me02, '车辆24', 85, '吉林,湖北,广东,上海,重庆,北京,天津,广西,安徽,江西,陕西,湖南');
CALL gen_students(2025102000, @g2025, 2025, @d_me, @m_me02, '车辆25', 88, '吉林,湖北,广东,上海,重庆,北京,天津,广西,安徽,江西,陕西,湖南,浙江');
-- ⭐ME03 智能制造工程
CALL gen_students(2022102200, @g2022, 2022, @d_me, @m_me03, '智造22', 66, '江苏,浙江,广东,山东,湖北,河南,四川,福建,安徽,湖南');
CALL gen_students(2023102200, @g2023, 2023, @d_me, @m_me03, '智造23', 72, '江苏,浙江,广东,山东,湖北,河南,四川,福建,安徽,湖南,江西');
CALL gen_students(2024102200, @g2024, 2024, @d_me, @m_me03, '智造24', 78, '江苏,浙江,广东,山东,湖北,河南,四川,福建,安徽,湖南,江西,重庆');
CALL gen_students(2025102200, @g2025, 2025, @d_me, @m_me03, '智造25', 82, '江苏,浙江,广东,山东,湖北,河南,四川,福建,安徽,湖南,江西,重庆,上海');

-- ============ 医学院（中院 ~650 人）============
-- 🔥MED01 临床医学（五年制，无法 2027 年前毕业）
CALL gen_students(2022102400, @g2022, 2022, @d_med, @m_med01, '临床22', 88, '河南,山东,四川,广东,江苏,湖北,湖南,安徽,河北,浙江');
CALL gen_students(2023102400, @g2023, 2023, @d_med, @m_med01, '临床23', 92, '河南,山东,四川,广东,江苏,湖北,湖南,安徽,河北,浙江,福建');
CALL gen_students(2024102400, @g2024, 2024, @d_med, @m_med01, '临床24', 98, '河南,山东,四川,广东,江苏,湖北,湖南,安徽,河北,浙江,福建,江西');
CALL gen_students(2025102400, @g2025, 2025, @d_med, @m_med01, '临床25', 102, '河南,山东,四川,广东,江苏,湖北,湖南,安徽,河北,浙江,福建,江西,陕西');
-- ❄️MED02 护理学
CALL gen_students(2022102600, @g2022, 2022, @d_med, @m_med02, '护理22', 66, '河南,山东,四川,湖北,湖南,安徽,陕西,甘肃,贵州,云南');
CALL gen_students(2023102600, @g2023, 2023, @d_med, @m_med02, '护理23', 70, '河南,山东,四川,湖北,湖南,安徽,陕西,甘肃,贵州,云南,山西');
CALL gen_students(2024102600, @g2024, 2024, @d_med, @m_med02, '护理24', 75, '河南,山东,四川,湖北,湖南,安徽,陕西,甘肃,贵州,云南,山西,广西');
CALL gen_students(2025102600, @g2025, 2025, @d_med, @m_med02, '护理25', 78, '河南,山东,四川,湖北,湖南,安徽,陕西,甘肃,贵州,云南,山西,广西,河北');

-- ============ 土木工程学院（中院 ~625 人）============
-- ⭐CE01 土木工程
CALL gen_students(2022102800, @g2022, 2022, @d_ce, @m_ce01, '土木22', 90, '四川,重庆,贵州,云南,湖北,湖南,河南,山东,陕西,甘肃');
CALL gen_students(2023102800, @g2023, 2023, @d_ce, @m_ce01, '土木23', 95, '四川,重庆,贵州,云南,湖北,湖南,河南,山东,陕西,甘肃,安徽');
CALL gen_students(2024102800, @g2024, 2024, @d_ce, @m_ce01, '土木24', 100, '四川,重庆,贵州,云南,湖北,湖南,河南,山东,陕西,甘肃,安徽,江西');
CALL gen_students(2025102800, @g2025, 2025, @d_ce, @m_ce01, '土木25', 102, '四川,重庆,贵州,云南,湖北,湖南,河南,山东,陕西,甘肃,安徽,江西,广东');
-- ❄️CE02 工程管理
CALL gen_students(2022103000, @g2022, 2022, @d_ce, @m_ce02, '工管22', 58, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,四川,重庆');
CALL gen_students(2023103000, @g2023, 2023, @d_ce, @m_ce02, '工管23', 62, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,四川,重庆,安徽');
CALL gen_students(2024103000, @g2024, 2024, @d_ce, @m_ce02, '工管24', 66, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,四川,重庆,安徽,陕西');
CALL gen_students(2025103000, @g2025, 2025, @d_ce, @m_ce02, '工管25', 68, '江苏,浙江,福建,广东,湖北,湖南,河南,山东,四川,重庆,安徽,陕西,河北');

-- ============ 法学院（中院 ~496 人）============
-- ⭐LAW01 法学
CALL gen_students(2022103200, @g2022, 2022, @d_law, @m_law01, '法学22', 76, '北京,上海,广东,浙江,江苏,山东,河南,湖北,四川,重庆');
CALL gen_students(2023103200, @g2023, 2023, @d_law, @m_law01, '法学23', 80, '北京,上海,广东,浙江,江苏,山东,河南,湖北,四川,重庆,福建');
CALL gen_students(2024103200, @g2024, 2024, @d_law, @m_law01, '法学24', 85, '北京,上海,广东,浙江,江苏,山东,河南,湖北,四川,重庆,福建,湖南');
CALL gen_students(2025103200, @g2025, 2025, @d_law, @m_law01, '法学25', 88, '北京,上海,广东,浙江,江苏,山东,河南,湖北,四川,重庆,福建,湖南,安徽');
-- ❄️LAW02 知识产权
CALL gen_students(2022103400, @g2022, 2022, @d_law, @m_law02, '知产22', 40, '北京,上海,深圳,广州,杭州,南京,成都,武汉,西安,天津');
CALL gen_students(2023103400, @g2023, 2023, @d_law, @m_law02, '知产23', 42, '北京,上海,深圳,广州,杭州,南京,成都,武汉,西安,天津,青岛');
CALL gen_students(2024103400, @g2024, 2024, @d_law, @m_law02, '知产24', 47, '北京,上海,深圳,广州,杭州,南京,成都,武汉,西安,天津,青岛,厦门');
CALL gen_students(2025103400, @g2025, 2025, @d_law, @m_law02, '知产25', 48, '北京,上海,深圳,广州,杭州,南京,成都,武汉,西安,天津,青岛,厦门,苏州');

-- ============ 化学化工学院（中小院 ~498 人）============
-- ❄️CH01 化学工程与工艺
CALL gen_students(2022103600, @g2022, 2022, @d_ch, @m_ch01, '化工22', 66, '山东,江苏,浙江,河南,湖北,湖南,四川,广东,河北,辽宁');
CALL gen_students(2023103600, @g2023, 2023, @d_ch, @m_ch01, '化工23', 70, '山东,江苏,浙江,河南,湖北,湖南,四川,广东,河北,辽宁,安徽');
CALL gen_students(2024103600, @g2024, 2024, @d_ch, @m_ch01, '化工24', 75, '山东,江苏,浙江,河南,湖北,湖南,四川,广东,河北,辽宁,安徽,山西');
CALL gen_students(2025103600, @g2025, 2025, @d_ch, @m_ch01, '化工25', 78, '山东,江苏,浙江,河南,湖北,湖南,四川,广东,河北,辽宁,安徽,山西,甘肃');
-- ❄️CH02 应用化学
CALL gen_students(2022103800, @g2022, 2022, @d_ch, @m_ch02, '应化22', 52, '湖北,湖南,四川,重庆,贵州,云南,江西,广西,福建,广东');
CALL gen_students(2023103800, @g2023, 2023, @d_ch, @m_ch02, '应化23', 54, '湖北,湖南,四川,重庆,贵州,云南,江西,广西,福建,广东,安徽');
CALL gen_students(2024103800, @g2024, 2024, @d_ch, @m_ch02, '应化24', 58, '湖北,湖南,四川,重庆,贵州,云南,江西,广西,福建,广东,安徽,河南');
CALL gen_students(2025103800, @g2025, 2025, @d_ch, @m_ch02, '应化25', 60, '湖北,湖南,四川,重庆,贵州,云南,江西,广西,福建,广东,安徽,河南,山东');

-- ============ 生命科学学院（中小院 ~442 人）============
-- ❄️LS01 生物技术
CALL gen_students(2022104000, @g2022, 2022, @d_ls, @m_ls01, '生技22', 58, '湖北,湖南,四川,重庆,云南,贵州,广西,广东,福建,江西');
CALL gen_students(2023104000, @g2023, 2023, @d_ls, @m_ls01, '生技23', 62, '湖北,湖南,四川,重庆,云南,贵州,广西,广东,福建,江西,安徽');
CALL gen_students(2024104000, @g2024, 2024, @d_ls, @m_ls01, '生技24', 66, '湖北,湖南,四川,重庆,云南,贵州,广西,广东,福建,江西,安徽,浙江');
CALL gen_students(2025104000, @g2025, 2025, @d_ls, @m_ls01, '生技25', 68, '湖北,湖南,四川,重庆,云南,贵州,广西,广东,福建,江西,安徽,浙江,山东');
-- ❄️LS02 食品科学与工程
CALL gen_students(2022104200, @g2022, 2022, @d_ls, @m_ls02, '食品22', 46, '河南,山东,四川,湖北,湖南,广东,广西,福建,江西,安徽');
CALL gen_students(2023104200, @g2023, 2023, @d_ls, @m_ls02, '食品23', 48, '河南,山东,四川,湖北,湖南,广东,广西,福建,江西,安徽,浙江');
CALL gen_students(2024104200, @g2024, 2024, @d_ls, @m_ls02, '食品24', 52, '河南,山东,四川,湖北,湖南,广东,广西,福建,江西,安徽,浙江,江苏');
CALL gen_students(2025104200, @g2025, 2025, @d_ls, @m_ls02, '食品25', 54, '河南,山东,四川,湖北,湖南,广东,广西,福建,江西,安徽,浙江,江苏,河北');

-- ============ 教育学院（中小院 ~420 人）============
-- ❄️EDU01 教育学
CALL gen_students(2022104400, @g2022, 2022, @d_edu, @m_edu01, '教育22', 52, '河南,山东,四川,湖北,湖南,陕西,山西,甘肃,云南,贵州');
CALL gen_students(2023104400, @g2023, 2023, @d_edu, @m_edu01, '教育23', 56, '河南,山东,四川,湖北,湖南,陕西,山西,甘肃,云南,贵州,安徽');
CALL gen_students(2024104400, @g2024, 2024, @d_edu, @m_edu01, '教育24', 60, '河南,山东,四川,湖北,湖南,陕西,山西,甘肃,云南,贵州,安徽,江西');
CALL gen_students(2025104400, @g2025, 2025, @d_edu, @m_edu01, '教育25', 62, '河南,山东,四川,湖北,湖南,陕西,山西,甘肃,云南,贵州,安徽,江西,广西');
-- ❄️EDU02 学前教育
CALL gen_students(2022104600, @g2022, 2022, @d_edu, @m_edu02, '学前22', 45, '河南,山东,四川,湖北,湖南,安徽,江西,广西,贵州,云南');
CALL gen_students(2023104600, @g2023, 2023, @d_edu, @m_edu02, '学前23', 48, '河南,山东,四川,湖北,湖南,安徽,江西,广西,贵州,云南,甘肃');
CALL gen_students(2024104600, @g2024, 2024, @d_edu, @m_edu02, '学前24', 52, '河南,山东,四川,湖北,湖南,安徽,江西,广西,贵州,云南,甘肃,陕西');
CALL gen_students(2025104600, @g2025, 2025, @d_edu, @m_edu02, '学前25', 54, '河南,山东,四川,湖北,湖南,安徽,江西,广西,贵州,云南,甘肃,陕西,山西');

-- ============ 新闻传播学院（中小院 ~412 人）============
-- ❄️JR01 新闻学
CALL gen_students(2022104800, @g2022, 2022, @d_jr, @m_jr01, '新闻22', 52, '北京,上海,广东,浙江,江苏,湖北,湖南,四川,重庆,陕西');
CALL gen_students(2023104800, @g2023, 2023, @d_jr, @m_jr01, '新闻23', 56, '北京,上海,广东,浙江,江苏,湖北,湖南,四川,重庆,陕西,山东');
CALL gen_students(2024104800, @g2024, 2024, @d_jr, @m_jr01, '新闻24', 60, '北京,上海,广东,浙江,江苏,湖北,湖南,四川,重庆,陕西,山东,河南');
CALL gen_students(2025104800, @g2025, 2025, @d_jr, @m_jr01, '新闻25', 62, '北京,上海,广东,浙江,江苏,湖北,湖南,四川,重庆,陕西,山东,河南,福建');
-- ❄️JR02 网络与新媒体
CALL gen_students(2022105000, @g2022, 2022, @d_jr, @m_jr02, '网新22', 42, '广东,浙江,江苏,四川,湖北,湖南,福建,山东,河南,陕西');
CALL gen_students(2023105000, @g2023, 2023, @d_jr, @m_jr02, '网新23', 47, '广东,浙江,江苏,四川,湖北,湖南,福建,山东,河南,陕西,重庆');
CALL gen_students(2024105000, @g2024, 2024, @d_jr, @m_jr02, '网新24', 50, '广东,浙江,江苏,四川,湖北,湖南,福建,山东,河南,陕西,重庆,北京');
CALL gen_students(2025105000, @g2025, 2025, @d_jr, @m_jr02, '网新25', 53, '广东,浙江,江苏,四川,湖北,湖南,福建,山东,河南,陕西,重庆,北京,上海');

-- ============ 外国语学院（中小院 ~424 人）============
-- ❄️FL01 英语
CALL gen_students(2022105200, @g2022, 2022, @d_fl, @m_fl01, '英语22', 62, '山东,河南,河北,山西,陕西,甘肃,辽宁,吉林,黑龙江,内蒙古');
CALL gen_students(2023105200, @g2023, 2023, @d_fl, @m_fl01, '英语23', 66, '山东,河南,河北,山西,陕西,甘肃,辽宁,吉林,黑龙江,内蒙古,湖北');
CALL gen_students(2024105200, @g2024, 2024, @d_fl, @m_fl01, '英语24', 70, '山东,河南,河北,山西,陕西,甘肃,辽宁,吉林,黑龙江,内蒙古,湖北,湖南');
CALL gen_students(2025105200, @g2025, 2025, @d_fl, @m_fl01, '英语25', 72, '山东,河南,河北,山西,陕西,甘肃,辽宁,吉林,黑龙江,内蒙古,湖北,湖南,安徽');
-- ❄️FL02 日语
CALL gen_students(2022105400, @g2022, 2022, @d_fl, @m_fl02, '日语22', 38, '辽宁,吉林,黑龙江,山东,浙江,江苏,福建,广东,上海,北京');
CALL gen_students(2023105400, @g2023, 2023, @d_fl, @m_fl02, '日语23', 40, '辽宁,吉林,黑龙江,山东,浙江,江苏,福建,广东,上海,北京,天津');
CALL gen_students(2024105400, @g2024, 2024, @d_fl, @m_fl02, '日语24', 43, '辽宁,吉林,黑龙江,山东,浙江,江苏,福建,广东,上海,北京,天津,重庆');
CALL gen_students(2025105400, @g2025, 2025, @d_fl, @m_fl02, '日语25', 44, '辽宁,吉林,黑龙江,山东,浙江,江苏,福建,广东,上海,北京,天津,重庆,湖北');

-- ============ 数学与统计学院（小院 ~486 人）============
-- ❄️MS01 数学与应用数学
CALL gen_students(2022105600, @g2022, 2022, @d_ms, @m_ms01, '数学22', 46, '河南,山东,四川,湖北,湖南,陕西,安徽,江西,广西,贵州');
CALL gen_students(2023105600, @g2023, 2023, @d_ms, @m_ms01, '数学23', 48, '河南,山东,四川,湖北,湖南,陕西,安徽,江西,广西,贵州,山西');
CALL gen_students(2024105600, @g2024, 2024, @d_ms, @m_ms01, '数学24', 52, '河南,山东,四川,湖北,湖南,陕西,安徽,江西,广西,贵州,山西,甘肃');
CALL gen_students(2025105600, @g2025, 2025, @d_ms, @m_ms01, '数学25', 54, '河南,山东,四川,湖北,湖南,陕西,安徽,江西,广西,贵州,山西,甘肃,云南');
-- ❄️MS02 统计学
CALL gen_students(2022105800, @g2022, 2022, @d_ms, @m_ms02, '统计22', 38, '广东,浙江,江苏,福建,山东,湖北,湖南,四川,河南,安徽');
CALL gen_students(2023105800, @g2023, 2023, @d_ms, @m_ms02, '统计23', 40, '广东,浙江,江苏,福建,山东,湖北,湖南,四川,河南,安徽,江西');
CALL gen_students(2024105800, @g2024, 2024, @d_ms, @m_ms02, '统计24', 43, '广东,浙江,江苏,福建,山东,湖北,湖南,四川,河南,安徽,江西,河北');
CALL gen_students(2025105800, @g2025, 2025, @d_ms, @m_ms02, '统计25', 44, '广东,浙江,江苏,福建,山东,湖北,湖南,四川,河南,安徽,江西,河北,重庆');
-- ❄️MS03 数据计算及应用
CALL gen_students(2022106000, @g2022, 2022, @d_ms, @m_ms03, '数计22', 30, '贵州,云南,广西,甘肃,青海,宁夏,新疆,内蒙古,山西,江西');
CALL gen_students(2023106000, @g2023, 2023, @d_ms, @m_ms03, '数计23', 33, '贵州,云南,广西,甘肃,青海,宁夏,新疆,内蒙古,山西,江西,海南');
CALL gen_students(2024106000, @g2024, 2024, @d_ms, @m_ms03, '数计24', 36, '贵州,云南,广西,甘肃,青海,宁夏,新疆,内蒙古,山西,江西,海南,西藏');
CALL gen_students(2025106000, @g2025, 2025, @d_ms, @m_ms03, '数计25', 38, '贵州,云南,广西,甘肃,青海,宁夏,新疆,内蒙古,山西,江西,海南,西藏,黑龙江');

-- ============ 艺术设计学院（小院 ~484 人）============
-- ❄️AD01 视觉传达设计
CALL gen_students(2022106200, @g2022, 2022, @d_ad, @m_ad01, '视传22', 48, '山东,河南,湖北,湖南,四川,广东,浙江,江苏,福建,江西');
CALL gen_students(2023106200, @g2023, 2023, @d_ad, @m_ad01, '视传23', 50, '山东,河南,湖北,湖南,四川,广东,浙江,江苏,福建,江西,安徽');
CALL gen_students(2024106200, @g2024, 2024, @d_ad, @m_ad01, '视传24', 53, '山东,河南,湖北,湖南,四川,广东,浙江,江苏,福建,江西,安徽,河北');
CALL gen_students(2025106200, @g2025, 2025, @d_ad, @m_ad01, '视传25', 54, '山东,河南,湖北,湖南,四川,广东,浙江,江苏,福建,江西,安徽,河北,陕西');
-- ❄️AD02 环境设计
CALL gen_students(2022106400, @g2022, 2022, @d_ad, @m_ad02, '环设22', 36, '湖北,湖南,四川,重庆,广东,广西,江西,福建,安徽,浙江');
CALL gen_students(2023106400, @g2023, 2023, @d_ad, @m_ad02, '环设23', 38, '湖北,湖南,四川,重庆,广东,广西,江西,福建,安徽,浙江,江苏');
CALL gen_students(2024106400, @g2024, 2024, @d_ad, @m_ad02, '环设24', 40, '湖北,湖南,四川,重庆,广东,广西,江西,福建,安徽,浙江,江苏,山东');
CALL gen_students(2025106400, @g2025, 2025, @d_ad, @m_ad02, '环设25', 41, '湖北,湖南,四川,重庆,广东,广西,江西,福建,安徽,浙江,江苏,山东,河南');
-- ❄️AD03 数字媒体艺术
CALL gen_students(2022106600, @g2022, 2022, @d_ad, @m_ad03, '数媒22', 30, '广东,浙江,江苏,上海,北京,四川,湖北,湖南,福建,重庆');
CALL gen_students(2023106600, @g2023, 2023, @d_ad, @m_ad03, '数媒23', 33, '广东,浙江,江苏,上海,北京,四川,湖北,湖南,福建,重庆,陕西');
CALL gen_students(2024106600, @g2024, 2024, @d_ad, @m_ad03, '数媒24', 36, '广东,浙江,江苏,上海,北京,四川,湖北,湖南,福建,重庆,陕西,山东');
CALL gen_students(2025106600, @g2025, 2025, @d_ad, @m_ad03, '数媒25', 38, '广东,浙江,江苏,上海,北京,四川,湖北,湖南,福建,重庆,陕西,山东,河南');

DROP PROCEDURE IF EXISTS gen_students;

-- ============================================================
-- PART 7A: 2022 级临床医学五年制特殊处理
-- 临床医学五年制，2022 级要到 2027 年才毕业
-- 将之前随机分配的"已毕业"状态改回"在读"
-- ============================================================
UPDATE student SET status = 1
WHERE enroll_year = 2022
  AND major_id = @m_med01
  AND status = 3
  AND RAND() < 0.85;

-- ============================================================
-- PART 7B: 招生计划（基于实际学生数反推，2026 仅为计划）
-- 热门专业报到率高、冷门低。报名人数计划数 > 往年实际录取数
-- ============================================================
-- 2022-2025：根据实际学生数生成招生计划
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, yrs.yr,
    ROUND(s.cnt * (
        CASE WHEN m.major_code IN ('CS01','CS02') THEN 1.06
             WHEN m.major_code IN ('CS03','EE01','EE02','EE03','EM01','EM03','MED01') THEN 1.08
             WHEN m.major_code IN ('ME01','ME02','ME03','EM02','CE01','LAW01') THEN 1.12
             ELSE 1.18 END
    )) AS plan_count,
    s.cnt AS actual_count,
    0 AS report_rate
FROM major m
CROSS JOIN (SELECT 2022 AS yr UNION ALL SELECT 2023 UNION ALL SELECT 2024 UNION ALL SELECT 2025) yrs
JOIN (
    SELECT major_id, enroll_year, COUNT(*) AS cnt
    FROM student GROUP BY major_id, enroll_year
) s ON s.major_id = m.major_id AND s.enroll_year = yrs.yr;

-- 更新报到率 = actual_count / plan_count * 100
UPDATE enrollment SET report_rate = ROUND(actual_count / plan_count * 100, 2)
WHERE year BETWEEN 2022 AND 2025 AND plan_count > 0;

-- 2026 级招生计划（仅有计划数，无学生报到）
DELETE FROM enrollment WHERE year = 2026;
INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
SELECT m.major_id, 2026,
    CASE
        WHEN m.major_code IN ('CS01','CS02','CS03') THEN 180
        WHEN m.major_code = 'EE01' THEN 140
        WHEN m.major_code IN ('EE02','EE03') THEN 120
        WHEN m.major_code = 'EM01' THEN 140
        WHEN m.major_code = 'EM03' THEN 125
        WHEN m.major_code = 'EM02' THEN 105
        WHEN m.major_code = 'ME01' THEN 135
        WHEN m.major_code = 'ME02' THEN 95
        WHEN m.major_code = 'ME03' THEN 90
        WHEN m.major_code = 'MED01' THEN 110
        WHEN m.major_code = 'MED02' THEN 85
        WHEN m.major_code = 'CE01' THEN 110
        WHEN m.major_code = 'CE02' THEN 75
        WHEN m.major_code = 'LAW01' THEN 95
        WHEN m.major_code = 'LAW02' THEN 55
        WHEN m.major_code = 'CH01' THEN 85
        WHEN m.major_code = 'CH02' THEN 65
        WHEN m.major_code = 'LS01' THEN 75
        WHEN m.major_code = 'LS02' THEN 60
        WHEN m.major_code = 'EDU01' THEN 68
        WHEN m.major_code = 'EDU02' THEN 58
        WHEN m.major_code = 'JR01' THEN 68
        WHEN m.major_code = 'JR02' THEN 58
        WHEN m.major_code = 'FL01' THEN 78
        WHEN m.major_code = 'FL02' THEN 48
        WHEN m.major_code = 'MS01' THEN 58
        WHEN m.major_code = 'MS02' THEN 48
        WHEN m.major_code = 'MS03' THEN 42
        WHEN m.major_code = 'AD01' THEN 58
        WHEN m.major_code = 'AD02' THEN 45
        WHEN m.major_code = 'AD03' THEN 42
        ELSE 60
    END AS plan_count,
    0 AS actual_count,
    NULL AS report_rate
FROM major m;

-- ============================================================
-- PART 8: 验收查询
-- ============================================================
SELECT '=== 种子数据验收 ===' AS '';
SELECT '院系数:' AS label, COUNT(*) AS cnt FROM department;
SELECT '专业数:' AS label, COUNT(*) AS cnt FROM major;
SELECT '教职工数:' AS label, COUNT(*) AS cnt FROM `user` WHERE username BETWEEN '200001' AND '200368';
SELECT '学生总数:' AS label, COUNT(*) AS cnt FROM student;
SELECT '按年级:' AS label, g.grade_name, COUNT(*) AS cnt FROM student s JOIN grade g ON s.grade_id=g.grade_id GROUP BY g.grade_name ORDER BY g.grade_name;
SELECT '按学籍状态:' AS label, CASE status WHEN 1 THEN '在读' WHEN 2 THEN '休学' WHEN 3 THEN '毕业' WHEN 0 THEN '退学' END AS status_name, COUNT(*) AS cnt FROM student GROUP BY status ORDER BY status;
SELECT '按院系(TOP 5):' AS label, d.dept_name, COUNT(*) AS cnt FROM student s JOIN department d ON s.dept_id=d.dept_id GROUP BY d.dept_name ORDER BY cnt DESC LIMIT 5;
SELECT '招生计划(2026):' AS label, COUNT(*) AS cnt FROM enrollment WHERE year=2026;
SELECT '2026计划总数:' AS label, SUM(plan_count) AS total FROM enrollment WHERE year=2026;
SELECT '=== 完成 ===' AS '';
