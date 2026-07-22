package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V50__base_seed_data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        // ---------- Departments ----------
        exec(connection, """
            INSERT INTO `department` (`dept_name`, `dept_code`, `description`) VALUES
                ('计算机与人工智能学院', 'CS', '设有软件工程、人工智能等专业，建有省级重点实验室。'),
                ('经济管理学院', 'EM', '涵盖会计学、工商管理等专业，注重产教融合。'),
                ('外国语学院', 'FL', '开设英语、日语等专业，培养复合型外语人才。'),
                ('机械工程学院', 'ME', '以智能制造为特色，设有机械设计、车辆工程等专业。'),
                ('数学与统计学院', 'MS', '基础学科学院，设有数学与应用数学、统计学专业。'),
                ('艺术设计学院', 'AD', '设有视觉传达设计、环境设计等专业。')
            ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`), `description` = VALUES(`description`)
        """);

        exec(connection, "SET @d_cs := (SELECT dept_id FROM department WHERE dept_code = 'CS')");
        exec(connection, "SET @d_em := (SELECT dept_id FROM department WHERE dept_code = 'EM')");
        exec(connection, "SET @d_fl := (SELECT dept_id FROM department WHERE dept_code = 'FL')");
        exec(connection, "SET @d_me := (SELECT dept_id FROM department WHERE dept_code = 'ME')");
        exec(connection, "SET @d_ms := (SELECT dept_id FROM department WHERE dept_code = 'MS')");
        exec(connection, "SET @d_ad := (SELECT dept_id FROM department WHERE dept_code = 'AD')");

        // ---------- Majors ----------
        exec(connection, """
            INSERT INTO `major` (`dept_id`, `major_name`, `major_code`, `cultivation_plan`) VALUES
                (@d_cs, '软件工程', 'CS01', '培养掌握软件开发全流程能力的高级工程人才。'),
                (@d_cs, '人工智能', 'CS02', '培养具备机器学习与智能系统研发能力的人才。'),
                (@d_em, '会计学', 'EM01', '培养熟悉财务会计与审计实务的应用型人才。'),
                (@d_em, '工商管理', 'EM02', '培养具备现代企业管理能力的复合型人才。'),
                (@d_fl, '英语', 'FL01', '培养具有扎实英语功底的翻译与教学人才。'),
                (@d_fl, '日语', 'FL02', '培养面向国际交流的日语应用人才。'),
                (@d_me, '机械设计制造及其自动化', 'ME01', '培养面向智能制造的机械工程人才。'),
                (@d_me, '车辆工程', 'ME02', '培养新能源汽车方向的工程技术人才。'),
                (@d_ms, '数学与应用数学', 'MS01', '培养具备扎实数学基础的研究与应用人才。'),
                (@d_ms, '统计学', 'MS02', '培养掌握数据分析方法的统计人才。'),
                (@d_ad, '视觉传达设计', 'AD01', '培养品牌与数字媒体方向的设计人才。'),
                (@d_ad, '环境设计', 'AD02', '培养空间与景观方向的设计人才。')
            ON DUPLICATE KEY UPDATE `major_name` = VALUES(`major_name`), `dept_id` = VALUES(`dept_id`)
        """);

        exec(connection, "SET @m_cs01 := (SELECT major_id FROM major WHERE major_code = 'CS01')");
        exec(connection, "SET @m_cs02 := (SELECT major_id FROM major WHERE major_code = 'CS02')");
        exec(connection, "SET @m_em01 := (SELECT major_id FROM major WHERE major_code = 'EM01')");
        exec(connection, "SET @m_em02 := (SELECT major_id FROM major WHERE major_code = 'EM02')");
        exec(connection, "SET @m_fl01 := (SELECT major_id FROM major WHERE major_code = 'FL01')");
        exec(connection, "SET @m_fl02 := (SELECT major_id FROM major WHERE major_code = 'FL02')");
        exec(connection, "SET @m_me01 := (SELECT major_id FROM major WHERE major_code = 'ME01')");
        exec(connection, "SET @m_me02 := (SELECT major_id FROM major WHERE major_code = 'ME02')");
        exec(connection, "SET @m_ms01 := (SELECT major_id FROM major WHERE major_code = 'MS01')");
        exec(connection, "SET @m_ms02 := (SELECT major_id FROM major WHERE major_code = 'MS02')");
        exec(connection, "SET @m_ad01 := (SELECT major_id FROM major WHERE major_code = 'AD01')");
        exec(connection, "SET @m_ad02 := (SELECT major_id FROM major WHERE major_code = 'AD02')");

        // ---------- Grades ----------
        exec(connection, """
            INSERT INTO `grade` (`grade_name`)
            SELECT t.grade_name FROM (
                SELECT '2024级' AS grade_name UNION ALL SELECT '2025级' UNION ALL SELECT '2026级'
            ) t
            WHERE NOT EXISTS (SELECT 1 FROM grade g WHERE g.grade_name = t.grade_name)
        """);

        exec(connection, "SET @g2024 := (SELECT grade_id FROM grade WHERE grade_name = '2024级' LIMIT 1)");
        exec(connection, "SET @g2025 := (SELECT grade_id FROM grade WHERE grade_name = '2025级' LIMIT 1)");
        exec(connection, "SET @g2026 := (SELECT grade_id FROM grade WHERE grade_name = '2026级' LIMIT 1)");

        // ---------- Staff profiles (password: 123321) ----------
        exec(connection, "UPDATE `user` SET `real_name` = '刘一鸣', `gender` = 1, `title` = '讲师',   `position` = NULL,     `dept_id` = @d_cs, `phone` = '13800001001', `email` = 'liuym@campus.edu'  WHERE `username` = '700001'");
        exec(connection, "UPDATE `user` SET `real_name` = '宋建国', `gender` = 1, `title` = NULL,     `position` = '教务干事', `dept_id` = @d_cs, `phone` = '13800001002', `email` = 'songjg@campus.edu' WHERE `username` = '800001'");
        exec(connection, "UPDATE `user` SET `real_name` = '系统管理员' WHERE `username` = 'admin'");

        exec(connection, """
            INSERT INTO `user` (`username`, `password`, `user_type`, `real_name`, `gender`, `phone`, `email`, `title`, `position`, `dept_id`, `status`) VALUES
                ('700002', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '陈静',   2, '13800001003', 'chenj@campus.edu',   '教授',   '院长',     @d_cs, 1),
                ('700003', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '王海涛', 1, '13800001004', 'wanght@campus.edu',  '副教授', NULL,       @d_cs, 1),
                ('700004', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '李文丽', 2, '13800001005', 'liwl@campus.edu',    '教授',   '系主任',   @d_em, 1),
                ('700005', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '张国强', 1, '13800001006', 'zhanggq@campus.edu', '讲师',   NULL,       @d_em, 1),
                ('700006', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '赵雪梅', 2, '13800001007', 'zhaoxm@campus.edu',  '副教授', NULL,       @d_fl, 1),
                ('700007', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '孙志刚', 1, '13800001008', 'sunzg@campus.edu',   '教授',   '副院长',   @d_me, 1),
                ('700008', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '周敏',   2, '13800001009', 'zhoum@campus.edu',   '讲师',   NULL,       @d_ms, 1),
                ('700009', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 2, '吴桂芳', 2, '13800001010', 'wugf@campus.edu',    '副教授', NULL,       @d_ad, 1),
                ('800002', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, '郑立新', 1, '13800001011', 'zhenglx@campus.edu', NULL,     '辅导员',   @d_cs, 1),
                ('800003', '$2a$10$O9AYH1qiGk9m8wdTB3GKQ.bshEv1b5ofrGfNh0Rzw7YQD9IklnLgy', 3, '冯春燕', 2, '13800001012', 'fengcy@campus.edu',  NULL,     '辅导员',   @d_em, 1)
            ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`), `dept_id` = VALUES(`dept_id`), `title` = VALUES(`title`), `position` = VALUES(`position`)
        """);

        exec(connection, """
            INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
            SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON (
                (u.user_type = 2 AND r.role_code = 'TEACHER') OR
                (u.user_type = 3 AND r.role_code = 'STAFF')
            )
            WHERE u.username IN ('700002','700003','700004','700005','700006','700007','700008','700009','800002','800003')
        """);

        // ---------- Students (60 records across 3 grades / 10 majors / 12 provinces) ----------
        exec(connection, """
            INSERT INTO `student` (`student_no`, `student_name`, `gender`, `student_birth`, `student_age`, `student_address`, `grade_id`, `dept_id`, `major_id`, `class_name`, `origin_place`, `enroll_year`, `status`) VALUES
                (2024100001, '张伟',   1, '2006-03-12', 20, '济南市历下区', @g2024, @d_cs, @m_cs01, '软件2401', '山东', 2024, 1),
                (2024100002, '李娜',   2, '2006-07-08', 20, '郑州市金水区', @g2024, @d_cs, @m_cs01, '软件2401', '河南', 2024, 1),
                (2024100003, '王强',   1, '2005-11-23', 21, '南京市鼓楼区', @g2024, @d_cs, @m_cs01, '软件2401', '江苏', 2024, 1),
                (2024100004, '刘洋',   1, '2006-01-15', 20, '杭州市西湖区', @g2024, @d_cs, @m_cs01, '软件2402', '浙江', 2024, 1),
                (2024100005, '陈雪',   2, '2006-05-30', 20, '广州市天河区', @g2024, @d_cs, @m_cs01, '软件2402', '广东', 2024, 1),
                (2024100006, '杨光',   1, '2006-09-02', 20, '成都市武侯区', @g2024, @d_cs, @m_cs01, '软件2402', '四川', 2024, 1),
                (2024100007, '赵敏',   2, '2006-04-18', 20, '武汉市洪山区', @g2024, @d_cs, @m_cs02, '智能2401', '湖北', 2024, 1),
                (2024100008, '黄鹏',   1, '2005-12-09', 21, '合肥市蜀山区', @g2024, @d_cs, @m_cs02, '智能2401', '安徽', 2024, 1),
                (2024100009, '周婷',   2, '2006-06-21', 20, '长沙市岳麓区', @g2024, @d_cs, @m_cs02, '智能2401', '湖南', 2024, 1),
                (2024100010, '吴昊',   1, '2006-02-14', 20, '石家庄市桥西区', @g2024, @d_cs, @m_cs02, '智能2401', '河北', 2024, 1),
                (2024100011, '徐芳',   2, '2006-08-25', 20, '西安市雁塔区', @g2024, @d_em, @m_em01, '会计2401', '陕西', 2024, 1),
                (2024100012, '孙磊',   1, '2006-10-11', 20, '福州市鼓楼区', @g2024, @d_em, @m_em01, '会计2401', '福建', 2024, 1),
                (2024100013, '马丽',   2, '2006-03-05', 20, '青岛市市南区', @g2024, @d_em, @m_em01, '会计2401', '山东', 2024, 1),
                (2024100014, '朱军',   1, '2005-12-28', 21, '洛阳市涧西区', @g2024, @d_em, @m_em02, '工管2401', '河南', 2024, 1),
                (2024100015, '胡蝶',   2, '2006-07-17', 20, '苏州市姑苏区', @g2024, @d_em, @m_em02, '工管2401', '江苏', 2024, 1),
                (2024100016, '郭涛',   1, '2006-01-09', 20, '宁波市海曙区', @g2024, @d_fl, @m_fl01, '英语2401', '浙江', 2024, 1),
                (2024100017, '林霞',   2, '2006-05-22', 20, '深圳市南山区', @g2024, @d_fl, @m_fl01, '英语2401', '广东', 2024, 1),
                (2024100018, '何平',   1, '2006-09-14', 20, '绵阳市涪城区', @g2024, @d_fl, @m_fl02, '日语2401', '四川', 2024, 1),
                (2024100019, '高翔',   1, '2006-04-01', 20, '襄阳市樊城区', @g2024, @d_me, @m_me01, '机械2401', '湖北', 2024, 1),
                (2024100020, '罗静',   2, '2006-11-19', 20, '芜湖市镜湖区', @g2024, @d_me, @m_me01, '机械2401', '安徽', 2024, 1),
                (2024100021, '郑凯',   1, '2006-06-07', 20, '株洲市天元区', @g2024, @d_me, @m_me01, '机械2401', '湖南', 2024, 1),
                (2024100022, '梁爽',   2, '2006-02-26', 20, '唐山市路北区', @g2024, @d_me, @m_me02, '车辆2401', '河北', 2024, 1),
                (2024100023, '宋佳',   2, '2006-08-13', 20, '咸阳市秦都区', @g2024, @d_ms, @m_ms01, '数学2401', '陕西', 2024, 1),
                (2024100024, '唐磊',   1, '2006-10-04', 20, '厦门市思明区', @g2024, @d_ad, @m_ad01, '视传2401', '福建', 2024, 1),
                (2025100001, '许晴',   2, '2007-03-16', 19, '烟台市芝罘区', @g2025, @d_cs, @m_cs01, '软件2501', '山东', 2025, 1),
                (2025100002, '邓超',   1, '2007-07-29', 19, '开封市龙亭区', @g2025, @d_cs, @m_cs01, '软件2501', '河南', 2025, 1),
                (2025100003, '冯媛',   2, '2007-01-08', 19, '无锡市梁溪区', @g2025, @d_cs, @m_cs01, '软件2501', '江苏', 2025, 1),
                (2025100004, '曹阳',   1, '2007-05-20', 19, '温州市鹿城区', @g2025, @d_cs, @m_cs01, '软件2502', '浙江', 2025, 1),
                (2025100005, '彭飞',   1, '2007-09-11', 19, '佛山市禅城区', @g2025, @d_cs, @m_cs01, '软件2502', '广东', 2025, 1),
                (2025100006, '董雨',   2, '2007-04-03', 19, '德阳市旌阳区', @g2025, @d_cs, @m_cs02, '智能2501', '四川', 2025, 1),
                (2025100007, '袁野',   1, '2007-12-15', 19, '宜昌市西陵区', @g2025, @d_cs, @m_cs02, '智能2501', '湖北', 2025, 1),
                (2025100008, '蒋欣',   2, '2007-06-26', 19, '安庆市迎江区', @g2025, @d_cs, @m_cs02, '智能2501', '安徽', 2025, 1),
                (2025100009, '韩雪',   2, '2007-02-17', 19, '衡阳市雁峰区', @g2025, @d_cs, @m_cs02, '智能2501', '湖南', 2025, 1),
                (2025100010, '沈涛',   1, '2007-08-08', 19, '保定市竞秀区', @g2025, @d_em, @m_em01, '会计2501', '河北', 2025, 1),
                (2025100011, '姚明轩', 1, '2007-10-30', 19, '宝鸡市渭滨区', @g2025, @d_em, @m_em01, '会计2501', '陕西', 2025, 1),
                (2025100012, '谢丹',   2, '2007-03-21', 19, '泉州市丰泽区', @g2025, @d_em, @m_em01, '会计2501', '福建', 2025, 1),
                (2025100013, '钟鸣',   1, '2007-11-12', 19, '潍坊市奎文区', @g2025, @d_em, @m_em02, '工管2501', '山东', 2025, 1),
                (2025100014, '崔琳',   2, '2007-05-04', 19, '新乡市红旗区', @g2025, @d_em, @m_em02, '工管2501', '河南', 2025, 1),
                (2025100015, '潘越',   1, '2007-09-25', 19, '徐州市云龙区', @g2025, @d_fl, @m_fl01, '英语2501', '江苏', 2025, 1),
                (2025100016, '陆瑶',   2, '2007-01-27', 19, '嘉兴市南湖区', @g2025, @d_fl, @m_fl01, '英语2501', '浙江', 2025, 1),
                (2025100017, '蔡明',   1, '2007-07-06', 19, '东莞市南城区', @g2025, @d_fl, @m_fl02, '日语2501', '广东', 2025, 1),
                (2025100018, '丁香',   2, '2007-04-14', 19, '乐山市市中区', @g2025, @d_me, @m_me01, '机械2501', '四川', 2025, 1),
                (2025100019, '任杰',   1, '2007-12-01', 19, '黄石市黄石港区', @g2025, @d_me, @m_me01, '机械2501', '湖北', 2025, 1),
                (2025100020, '方圆',   1, '2007-06-18', 19, '蚌埠市龙子湖区', @g2025, @d_me, @m_me02, '车辆2501', '安徽', 2025, 1),
                (2026100001, '石磊',   1, '2008-02-09', 18, '临沂市兰山区', @g2026, @d_cs, @m_cs01, '软件2601', '山东', 2026, 1),
                (2026100002, '贾静雯', 2, '2008-08-19', 18, '南阳市卧龙区', @g2026, @d_cs, @m_cs01, '软件2601', '河南', 2026, 1),
                (2026100003, '孟浩',   1, '2008-10-22', 18, '常州市天宁区', @g2026, @d_cs, @m_cs01, '软件2601', '江苏', 2026, 1),
                (2026100004, '秦岚',   2, '2008-03-31', 18, '金华市婺城区', @g2026, @d_cs, @m_cs01, '软件2601', '浙江', 2026, 1),
                (2026100005, '江涛',   1, '2008-11-05', 18, '珠海市香洲区', @g2026, @d_cs, @m_cs02, '智能2601', '广东', 2026, 1),
                (2026100006, '尹梦',   2, '2008-05-13', 18, '南充市顺庆区', @g2026, @d_cs, @m_cs02, '智能2601', '四川', 2026, 1),
                (2026100007, '薛峰',   1, '2008-09-27', 18, '荆州市沙市区', @g2026, @d_cs, @m_cs02, '智能2601', '湖北', 2026, 1),
                (2026100008, '闫妮',   2, '2008-01-24', 18, '阜阳市颍州区', @g2026, @d_em, @m_em01, '会计2601', '安徽', 2026, 1),
                (2026100009, '龚宇',   1, '2008-07-16', 18, '岳阳市岳阳楼区', @g2026, @d_em, @m_em01, '会计2601', '湖南', 2026, 1),
                (2026100010, '常远',   1, '2008-04-08', 18, '邯郸市丛台区', @g2026, @d_fl, @m_fl01, '英语2601', '河北', 2026, 1),
                (2026100011, '倪妮',   2, '2008-12-11', 18, '渭南市临渭区', @g2026, @d_fl, @m_fl01, '英语2601', '陕西', 2026, 1),
                (2026100012, '严峻',   1, '2008-06-02', 18, '漳州市芗城区', @g2026, @d_me, @m_me01, '机械2601', '福建', 2026, 1),
                (2026100013, '牛莉',   2, '2008-02-28', 18, '淄博市张店区', @g2026, @d_me, @m_me01, '机械2601', '山东', 2026, 1),
                (2026100014, '侯亮',   1, '2008-08-06', 18, '商丘市梁园区', @g2026, @d_ms, @m_ms01, '数学2601', '河南', 2026, 1),
                (2026100015, '邵兵',   1, '2008-10-17', 18, '扬州市广陵区', @g2026, @d_ad, @m_ad01, '视传2601', '江苏', 2026, 1),
                (2026100016, '万茜',   2, '2008-05-09', 18, '绍兴市越城区', @g2026, @d_cs, @m_cs01, '软件2601', '浙江', 2026, 1)
            ON DUPLICATE KEY UPDATE `student_name` = VALUES(`student_name`), `dept_id` = VALUES(`dept_id`), `major_id` = VALUES(`major_id`)
        """);

        // ---------- Enrollment plans (2024-2026) ----------
        exec(connection, """
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
            ON DUPLICATE KEY UPDATE `plan_count` = VALUES(`plan_count`), `actual_count` = VALUES(`actual_count`), `report_rate` = VALUES(`report_rate`)
        """);

        // ---------- News ----------
        exec(connection, "SET @u_admin := (SELECT user_id FROM `user` WHERE username = 'admin')");
        exec(connection, "SET @u_t1    := (SELECT user_id FROM `user` WHERE username = '700001')");
        exec(connection, "SET @u_s1    := (SELECT user_id FROM `user` WHERE username = '600001')");

        exec(connection, """
            INSERT INTO `news` (`title`, `content`, `news_type`, `publisher_id`, `is_pinned`, `create_time`)
            SELECT t.title, t.content, t.news_type, @u_admin, t.is_pinned, t.create_time FROM (
                SELECT '关于2026级新生报到安排的通知' AS title, '2026级新生请于8月28日-29日持录取通知书到各学院迎新点办理报到手续，宿舍分配结果可在迎新系统中查询。' AS content, '公告' AS news_type, 1 AS is_pinned, '2026-07-10 09:00:00' AS create_time
                UNION ALL SELECT '2026-2027学年第一学期选课通知', '第一轮选课将于8月20日开放，请同学们提前查看培养方案，合理规划学分。', '公告', 0, '2026-07-12 10:30:00'
                UNION ALL SELECT '我校学子在全国大学生程序设计竞赛中获佳绩', '在刚刚结束的全国大学生程序设计竞赛中，我校三支代表队分获金、银、铜奖，创历史最好成绩。', '新闻', 0, '2026-07-08 15:20:00'
                UNION ALL SELECT '智慧校园服务平台正式上线试运行', '平台整合教务、学工、办公与基础数据四大板块，为全校师生提供一站式在线服务。', '新闻', 0, '2026-07-05 08:00:00'
                UNION ALL SELECT '图书馆暑期开放时间调整公告', '7月15日至8月25日期间，图书馆开放时间调整为每日9:00-17:00，节假日闭馆。', '公告', 0, '2026-07-13 16:45:00'
            ) t
        """);

        // ---------- Forum posts ----------
        exec(connection, """
            INSERT INTO `forum_post` (`title`, `content`, `author_id`, `like_count`, `view_count`, `status`, `create_time`)
            SELECT t.title, t.content, t.author_id, t.like_count, t.view_count, t.status, t.create_time FROM (
                SELECT '新生求助：宿舍网络如何开通？' AS title, '马上要报到了，请问宿舍的校园网怎么办理？需要提前准备什么材料吗？' AS content, @u_s1 AS author_id, 12 AS like_count, 208 AS view_count, 1 AS status, '2026-07-11 20:15:00' AS create_time
                UNION ALL SELECT '暑期实习经验分享帖', '刚结束在一家互联网公司的实习，整理了一些投递简历和面试的经验，欢迎交流。', @u_s1, 45, 530, 1, '2026-07-09 14:30:00'
                UNION ALL SELECT '关于选课系统使用问题的答疑汇总', '整理了同学们常见的选课问题和解决办法，选课前建议先看这一帖。', @u_t1, 67, 890, 1, '2026-07-12 09:00:00'
                UNION ALL SELECT '低价出全新考研资料（违规示例）', '各种考研资料低价转让，加微信详聊。', @u_s1, 0, 35, -1, '2026-07-13 22:40:00'
            ) t
        """);

        exec(connection, "SET @p_net   := (SELECT post_id FROM forum_post WHERE title = '新生求助：宿舍网络如何开通？' LIMIT 1)");
        exec(connection, "SET @p_intern := (SELECT post_id FROM forum_post WHERE title = '暑期实习经验分享帖' LIMIT 1)");
        exec(connection, "SET @p_course := (SELECT post_id FROM forum_post WHERE title = '关于选课系统使用问题的答疑汇总' LIMIT 1)");

        // ---------- Forum comments ----------
        exec(connection, """
            INSERT INTO `forum_comment` (`post_id`, `author_id`, `content`, `status`, `create_time`)
            SELECT t.post_id, t.author_id, t.content, 1, t.create_time FROM (
                SELECT @p_net AS post_id, @u_t1 AS author_id, '报到当天在宿舍楼下有网络运营商的办理点，带身份证即可。' AS content, '2026-07-11 21:00:00' AS create_time
                UNION ALL SELECT @p_net, @u_s1, '也可以在企业微信里搜索"校园网自助开通"，线上办理更快。', '2026-07-11 21:35:00'
                UNION ALL SELECT @p_intern, @u_s1, '感谢分享！请问简历模板方便发一份吗？', '2026-07-09 15:10:00'
                UNION ALL SELECT @p_intern, @u_t1, '写得很实用，已推荐给我带的毕业设计小组。', '2026-07-09 18:22:00'
                UNION ALL SELECT @p_course, @u_s1, '请问跨专业选课需要先提交申请吗？', '2026-07-12 10:05:00'
                UNION ALL SELECT @p_course, @u_t1, '需要的，在教务系统提交跨专业选课申请，学院审核通过后即可选课。', '2026-07-12 10:40:00'
                UNION ALL SELECT @p_course, @u_s1, '明白了，谢谢老师！', '2026-07-12 11:02:00'
                UNION ALL SELECT @p_net, @u_s1, '补充：新生宿舍今年已全部覆盖 WiFi，开通账号后直接连接即可。', '2026-07-12 08:50:00'
            ) t
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
