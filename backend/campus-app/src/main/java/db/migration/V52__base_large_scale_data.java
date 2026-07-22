package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Connection;
import java.sql.Statement;

public class V52__base_large_scale_data extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        exec(connection, "DELETE FROM student WHERE student_no >= 2024100000");
        exec(connection, "DELETE FROM enrollment");
        exec(connection, "DELETE FROM major WHERE major_code IN ('LAW01','LAW02','CE01','CE02','EE01','EE02','EE03','CH01','CH02','LS01','LS02','EDU01','EDU02','JR01','JR02','MED01','MED02')");
        exec(connection, "DELETE FROM department WHERE dept_code IN ('LAW','CE','EE','CH','LS','EDU','JR','MED')");

        exec(connection, """
            INSERT INTO department (dept_name, dept_code, description) VALUES
                ('法学院', 'LAW', '设有法学、知识产权专业，拥有模拟法庭实训中心。'),
                ('土木工程学院', 'CE', '涵盖土木工程、工程管理专业，服务国家基础设施建设。'),
                ('电子信息学院', 'EE', '设有电子信息工程、通信工程、物联网工程专业。'),
                ('化学化工学院', 'CH', '以化学工程与工艺、应用化学为特色，拥有省级重点实验室。'),
                ('生命科学学院', 'LS', '开设生物技术、食品科学与工程专业。'),
                ('教育学院', 'EDU', '培养教育学、学前教育师资，建有附属实验幼儿园。'),
                ('新闻传播学院', 'JR', '设有新闻学、网络与新媒体专业，建有融媒体实验中心。'),
                ('医学院', 'MED', '开设临床医学、护理学专业，拥有直属附属医院。')
            ON DUPLICATE KEY UPDATE dept_name = VALUES(dept_name), description = VALUES(description)
        """);

        exec(connection, "SET @d_cs := (SELECT dept_id FROM department WHERE dept_code = 'CS')");
        exec(connection, "SET @d_em := (SELECT dept_id FROM department WHERE dept_code = 'EM')");
        exec(connection, "SET @d_fl := (SELECT dept_id FROM department WHERE dept_code = 'FL')");
        exec(connection, "SET @d_me := (SELECT dept_id FROM department WHERE dept_code = 'ME')");
        exec(connection, "SET @d_ms := (SELECT dept_id FROM department WHERE dept_code = 'MS')");
        exec(connection, "SET @d_ad := (SELECT dept_id FROM department WHERE dept_code = 'AD')");
        exec(connection, "SET @d_law := (SELECT dept_id FROM department WHERE dept_code = 'LAW')");
        exec(connection, "SET @d_ce := (SELECT dept_id FROM department WHERE dept_code = 'CE')");
        exec(connection, "SET @d_ee := (SELECT dept_id FROM department WHERE dept_code = 'EE')");
        exec(connection, "SET @d_ch := (SELECT dept_id FROM department WHERE dept_code = 'CH')");
        exec(connection, "SET @d_ls := (SELECT dept_id FROM department WHERE dept_code = 'LS')");
        exec(connection, "SET @d_edu := (SELECT dept_id FROM department WHERE dept_code = 'EDU')");
        exec(connection, "SET @d_jr := (SELECT dept_id FROM department WHERE dept_code = 'JR')");
        exec(connection, "SET @d_med := (SELECT dept_id FROM department WHERE dept_code = 'MED')");

        exec(connection, """
            INSERT INTO major (dept_id, major_name, major_code, cultivation_plan) VALUES
                (@d_law, '法学', 'LAW01', '培养系统掌握法学理论与实务技能的法律人才。'),
                (@d_law, '知识产权', 'LAW02', '培养知识产权保护与管理的复合型人才。'),
                (@d_ce, '土木工程', 'CE01', '培养从事建筑、桥梁、隧道等工程设计施工的高级工程技术人才。'),
                (@d_ce, '工程管理', 'CE02', '培养具备工程技术与经济管理能力的复合型人才。'),
                (@d_ee, '电子信息工程', 'EE01', '培养从事电子信息系统设计开发的高级工程人才。'),
                (@d_ee, '通信工程', 'EE02', '培养通信系统与网络技术的高级工程人才。'),
                (@d_ee, '物联网工程', 'EE03', '培养物联网系统设计与应用开发的高级工程人才。'),
                (@d_ch, '化学工程与工艺', 'CH01', '培养化工生产与技术开发的高级工程人才。'),
                (@d_ch, '应用化学', 'CH02', '培养具备化学分析与应用开发能力的复合型人才。'),
                (@d_ls, '生物技术', 'LS01', '培养生物技术研究与产品开发的高级专门人才。'),
                (@d_ls, '食品科学与工程', 'LS02', '培养食品研发与安全管理的高级工程人才。'),
                (@d_edu, '教育学', 'EDU01', '培养教育管理与教学研究的高级专门人才。'),
                (@d_edu, '学前教育', 'EDU02', '培养学前教育师资与教研人才。'),
                (@d_jr, '新闻学', 'JR01', '培养融媒体新闻采编与传播的高级人才。'),
                (@d_jr, '网络与新媒体', 'JR02', '培养新媒体内容创作与运营的高级人才。'),
                (@d_med, '临床医学', 'MED01', '五年制，培养具备临床诊疗能力的医学人才。'),
                (@d_med, '护理学', 'MED02', '培养护理管理与临床护理的高级人才。'),
                (@d_cs, '数据科学与大数据技术', 'CS03', '培养大数据分析与智能决策的高级工程人才。'),
                (@d_em, '金融学', 'EM03', '培养金融分析与风险管理的高级专门人才。'),
                (@d_me, '智能制造工程', 'ME03', '培养面向工业4.0的智能装备设计与制造人才。'),
                (@d_ad, '数字媒体艺术', 'AD03', '培养数字内容创意与制作的高级艺术人才。'),
                (@d_ms, '数据计算及应用', 'MS03', '培养面向行业应用的数据建模与分析人才。')
            ON DUPLICATE KEY UPDATE major_name = VALUES(major_name), dept_id = VALUES(dept_id)
        """);

        exec(connection, "SET @m_law01 := (SELECT major_id FROM major WHERE major_code = 'LAW01')");
        exec(connection, "SET @m_law02 := (SELECT major_id FROM major WHERE major_code = 'LAW02')");
        exec(connection, "SET @m_ce01 := (SELECT major_id FROM major WHERE major_code = 'CE01')");
        exec(connection, "SET @m_ce02 := (SELECT major_id FROM major WHERE major_code = 'CE02')");
        exec(connection, "SET @m_ee01 := (SELECT major_id FROM major WHERE major_code = 'EE01')");
        exec(connection, "SET @m_ee02 := (SELECT major_id FROM major WHERE major_code = 'EE02')");
        exec(connection, "SET @m_ee03 := (SELECT major_id FROM major WHERE major_code = 'EE03')");
        exec(connection, "SET @m_ch01 := (SELECT major_id FROM major WHERE major_code = 'CH01')");
        exec(connection, "SET @m_ch02 := (SELECT major_id FROM major WHERE major_code = 'CH02')");
        exec(connection, "SET @m_ls01 := (SELECT major_id FROM major WHERE major_code = 'LS01')");
        exec(connection, "SET @m_ls02 := (SELECT major_id FROM major WHERE major_code = 'LS02')");
        exec(connection, "SET @m_edu01 := (SELECT major_id FROM major WHERE major_code = 'EDU01')");
        exec(connection, "SET @m_edu02 := (SELECT major_id FROM major WHERE major_code = 'EDU02')");
        exec(connection, "SET @m_jr01 := (SELECT major_id FROM major WHERE major_code = 'JR01')");
        exec(connection, "SET @m_jr02 := (SELECT major_id FROM major WHERE major_code = 'JR02')");
        exec(connection, "SET @m_med01 := (SELECT major_id FROM major WHERE major_code = 'MED01')");
        exec(connection, "SET @m_med02 := (SELECT major_id FROM major WHERE major_code = 'MED02')");
        exec(connection, "SET @m_cs03 := (SELECT major_id FROM major WHERE major_code = 'CS03')");
        exec(connection, "SET @m_em03 := (SELECT major_id FROM major WHERE major_code = 'EM03')");
        exec(connection, "SET @m_me03 := (SELECT major_id FROM major WHERE major_code = 'ME03')");
        exec(connection, "SET @m_ad03 := (SELECT major_id FROM major WHERE major_code = 'AD03')");
        exec(connection, "SET @m_ms03 := (SELECT major_id FROM major WHERE major_code = 'MS03')");
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

        exec(connection, "SET @g2023 := (SELECT grade_id FROM grade WHERE grade_name = '2023级' LIMIT 1)");
        exec(connection, "SET @g2024 := (SELECT grade_id FROM grade WHERE grade_name = '2024级' LIMIT 1)");
        exec(connection, "SET @g2025 := (SELECT grade_id FROM grade WHERE grade_name = '2025级' LIMIT 1)");
        exec(connection, "SET @g2026 := (SELECT grade_id FROM grade WHERE grade_name = '2026级' LIMIT 1)");
        exec(connection, "INSERT IGNORE INTO grade (grade_name) VALUES ('2023级')");
        exec(connection, "SET @g2023 := (SELECT grade_id FROM grade WHERE grade_name = '2023级' LIMIT 1)");

        exec(connection, "DROP PROCEDURE IF EXISTS gen_stu");

        exec(connection, """
            CREATE PROCEDURE gen_stu(
                IN base_no BIGINT, IN gid BIGINT, IN ey INT,
                IN did BIGINT, IN mid BIGINT,
                IN cprefix VARCHAR(32), IN cnt INT,
                IN oris TEXT
            )
            BEGIN
                DECLARE i INT DEFAULT 1;
                DECLARE g CHAR(1);
                DECLARE sname VARCHAR(64);
                DECLARE o VARCHAR(32);
                DECLARE total_ori INT;
                SET total_ori = CHAR_LENGTH(oris) - CHAR_LENGTH(REPLACE(oris, ',', '')) + 1;
                WHILE i <= cnt DO
                    SET g = IF(i % 5 = 0, '2', '1');
                    SET sname = CONCAT(
                        ELT(1+FLOOR(RAND()*8),'张','王','李','赵','陈','刘','杨','黄'),
                        ELT(1+FLOOR(RAND()*6),'伟','娜','强','洋','雪','敏','鹏','婷','昊','芳','磊','丽','军','蝶'),
                        ELT(1+FLOOR(RAND()*4),'轩','涵','铭','瑶','辰','然','宇','桐','哲','琳','博','萱','皓','怡','睿','欣')
                    );
                    SET o = SUBSTRING_INDEX(SUBSTRING_INDEX(oris, ',', 1 + ((i-1) % total_ori)), ',', -1);
                    INSERT IGNORE INTO student (
                        student_no, student_name, gender, student_birth, student_age,
                        student_address, grade_id, dept_id, major_id, class_name,
                        origin_place, enroll_year, status
                    ) VALUES (
                        base_no + i, sname, IF(g='1',1,2),
                        DATE_ADD('2006-06-01', INTERVAL (i*17 % 365) DAY),
                        19 + ey - 2024,
                        CONCAT(o, '区'),
                        gid, did, mid,
                        CONCAT(cprefix, LPAD(1 + (i-1) DIV 40, 2, '0')),
                        o, ey,
                        CASE WHEN i <= cnt-1 THEN 1 WHEN RAND() < 0.3 THEN 0 ELSE 2 END
                    );
                    SET i = i + 1;
                END WHILE;
            END
        """);

        // CS
        exec(connection, "CALL gen_stu(2023100000,@g2023,2023,@d_cs,@m_cs01,'计算23',80,'济南,青岛,烟台,临沂,郑州,洛阳,新乡,安阳,石家庄,唐山,保定,邯郸')");
        exec(connection, "CALL gen_stu(2023100200,@g2023,2023,@d_cs,@m_cs02,'智能23',60,'北京,上海,广州,深圳,杭州,南京,武汉,成都,西安,重庆')");
        exec(connection, "CALL gen_stu(2023100300,@g2023,2023,@d_cs,@m_cs03,'数据23',60,'长沙,合肥,南昌,福州,南宁,昆明,贵阳,兰州,西宁,银川')");
        exec(connection, "CALL gen_stu(2024100000,@g2024,2024,@d_cs,@m_cs01,'软件24',100,'济南,青岛,郑州,洛阳,石家庄,唐山,太原,呼和浩特')");
        exec(connection, "CALL gen_stu(2024100200,@g2024,2024,@d_cs,@m_cs02,'智能24',80,'北京,上海,杭州,南京,武汉,成都,西安,广州,深圳,重庆,天津,苏州')");
        exec(connection, "CALL gen_stu(2024100300,@g2024,2024,@d_cs,@m_cs03,'数据24',70,'长沙,合肥,南昌,福州,南宁,昆明,贵阳,兰州,西宁,银川,乌鲁木齐,拉萨')");
        exec(connection, "CALL gen_stu(2025100000,@g2025,2025,@d_cs,@m_cs01,'软件25',110,'南京,苏州,无锡,常州,杭州,宁波,温州,金华,合肥,芜湖')");
        exec(connection, "CALL gen_stu(2025100200,@g2025,2025,@d_cs,@m_cs02,'智能25',90,'武汉,长沙,南昌,成都,重庆,昆明,贵阳,南宁,海口,三亚')");
        exec(connection, "CALL gen_stu(2025100300,@g2025,2025,@d_cs,@m_cs03,'数据25',80,'西安,兰州,西宁,银川,乌鲁木齐,太原,呼和浩特,哈尔滨,长春,沈阳,大连')");
        exec(connection, "CALL gen_stu(2026100000,@g2026,2026,@d_cs,@m_cs01,'软件26',120,'广州,深圳,东莞,佛山,中山,珠海,惠州,汕头,湛江,茂名')");
        exec(connection, "CALL gen_stu(2026100200,@g2026,2026,@d_cs,@m_cs02,'智能26',100,'上海,南京,杭州,苏州,无锡,宁波,合肥,武汉,长沙,成都')");
        exec(connection, "CALL gen_stu(2026100300,@g2026,2026,@d_cs,@m_cs03,'数据26',90,'北京,天津,石家庄,济南,青岛,郑州,西安,太原,沈阳,大连,长春,哈尔滨')");

        // EM
        exec(connection, "CALL gen_stu(2023100600,@g2023,2023,@d_em,@m_em01,'会计23',70,'广州,深圳,东莞,佛山,珠海,惠州,中山,江门,汕头,湛江,茂名')");
        exec(connection, "CALL gen_stu(2023100700,@g2023,2023,@d_em,@m_em02,'工管23',50,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州,衢州,丽水,舟山')");
        exec(connection, "CALL gen_stu(2023100800,@g2023,2023,@d_em,@m_em03,'金融23',40,'上海,北京,深圳,广州,杭州,南京,苏州,成都,武汉,天津,重庆,青岛')");
        exec(connection, "CALL gen_stu(2024100600,@g2024,2024,@d_em,@m_em01,'会计24',80,'合肥,芜湖,蚌埠,马鞍山,安庆,铜陵,阜阳,宿州,六安,亳州,滁州,宣城')");
        exec(connection, "CALL gen_stu(2024100700,@g2024,2024,@d_em,@m_em02,'工管24',55,'南昌,九江,赣州,吉安,宜春,抚州,上饶,景德镇,萍乡,新余,鹰潭')");
        exec(connection, "CALL gen_stu(2024100800,@g2024,2024,@d_em,@m_em03,'金融24',45,'福州,厦门,泉州,漳州,龙岩,三明,南平,莆田,宁德')");
        exec(connection, "CALL gen_stu(2025100600,@g2025,2025,@d_em,@m_em01,'会计25',90,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,许昌,平顶山,焦作')");
        exec(connection, "CALL gen_stu(2025100700,@g2025,2025,@d_em,@m_em02,'工管25',60,'武汉,黄石,宜昌,襄阳,荆州,十堰,孝感,黄冈,咸宁,恩施,随州')");
        exec(connection, "CALL gen_stu(2025100800,@g2025,2025,@d_em,@m_em03,'金融25',50,'长沙,株洲,湘潭,衡阳,岳阳,常德,郴州,怀化,永州,益阳')");
        exec(connection, "CALL gen_stu(2026100600,@g2026,2026,@d_em,@m_em01,'会计26',100,'成都,绵阳,德阳,宜宾,南充,泸州,达州,乐山,眉山,自贡')");
        exec(connection, "CALL gen_stu(2026100700,@g2026,2026,@d_em,@m_em02,'工管26',65,'贵阳,遵义,六盘水,安顺,毕节,铜仁,黔东南,黔南,黔西南')");
        exec(connection, "CALL gen_stu(2026100800,@g2026,2026,@d_em,@m_em03,'金融26',55,'昆明,曲靖,玉溪,大理,保山,红河,文山,普洱,西双版纳,德宏')");

        // EE
        exec(connection, "CALL gen_stu(2023101000,@g2023,2023,@d_ee,@m_ee01,'电信23',70,'成都,绵阳,德阳,宜宾,南充,泸州,内江,自贡,眉山,乐山,广元,遂宁')");
        exec(connection, "CALL gen_stu(2023101100,@g2023,2023,@d_ee,@m_ee02,'通信23',50,'西安,咸阳,宝鸡,渭南,汉中,延安,榆林,安康,商洛')");
        exec(connection, "CALL gen_stu(2023101200,@g2023,2023,@d_ee,@m_ee03,'物联23',40,'杭州,宁波,温州,嘉兴,湖州,绍兴,金华,台州')");
        exec(connection, "CALL gen_stu(2024101000,@g2024,2024,@d_ee,@m_ee01,'电信24',80,'武汉,黄石,十堰,宜昌,襄阳,鄂州,荆门,孝感,荆州,黄冈')");
        exec(connection, "CALL gen_stu(2024101100,@g2024,2024,@d_ee,@m_ee02,'通信24',55,'南京,苏州,无锡,常州,徐州,南通,扬州,镇江,泰州,盐城')");
        exec(connection, "CALL gen_stu(2024101200,@g2024,2024,@d_ee,@m_ee03,'物联24',45,'郑州,洛阳,新乡,安阳,开封,商丘,南阳,许昌,周口')");
        exec(connection, "CALL gen_stu(2025101000,@g2025,2025,@d_ee,@m_ee01,'电信25',90,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,滨州,东营')");
        exec(connection, "CALL gen_stu(2025101100,@g2025,2025,@d_ee,@m_ee02,'通信25',60,'合肥,芜湖,马鞍山,蚌埠,淮南,淮北,铜陵,安庆,阜阳')");
        exec(connection, "CALL gen_stu(2025101200,@g2025,2025,@d_ee,@m_ee03,'物联25',50,'长沙,株洲,湘潭,岳阳,衡阳,常德,郴州,永州,邵阳,娄底')");
        exec(connection, "CALL gen_stu(2026101000,@g2026,2026,@d_ee,@m_ee01,'电信26',100,'广州,深圳,东莞,佛山,珠海,惠州,中山,汕头,江门')");
        exec(connection, "CALL gen_stu(2026101100,@g2026,2026,@d_ee,@m_ee02,'通信26',70,'成都,重庆,昆明,贵阳,南宁,海口,拉萨')");
        exec(connection, "CALL gen_stu(2026101200,@g2026,2026,@d_ee,@m_ee03,'物联26',55,'上海,北京,天津,石家庄,太原,呼和浩特,沈阳,大连,哈尔滨,长春')");

        // ME
        exec(connection, "CALL gen_stu(2023101500,@g2023,2023,@d_me,@m_me01,'机械23',80,'沈阳,大连,鞍山,抚顺,本溪,丹东,锦州,营口,阜新,辽阳,盘锦')");
        exec(connection, "CALL gen_stu(2023101600,@g2023,2023,@d_me,@m_me02,'车辆23',50,'长春,吉林,四平,辽源,通化,白山,松原,白城,延边')");
        exec(connection, "CALL gen_stu(2023101700,@g2023,2023,@d_me,@m_me03,'智造23',30,'哈尔滨,齐齐哈尔,牡丹江,佳木斯,大庆,鸡西,鹤岗,双鸭山')");
        exec(connection, "CALL gen_stu(2024101500,@g2024,2024,@d_me,@m_me01,'机械24',90,'济南,青岛,淄博,枣庄,东营,烟台,潍坊,济宁,泰安,威海')");
        exec(connection, "CALL gen_stu(2024101600,@g2024,2024,@d_me,@m_me02,'车辆24',55,'郑州,洛阳,开封,平顶山,安阳,鹤壁,新乡,焦作,濮阳,许昌')");
        exec(connection, "CALL gen_stu(2024101700,@g2024,2024,@d_me,@m_me03,'智造24',35,'石家庄,唐山,秦皇岛,邯郸,邢台,保定,张家口,承德,沧州')");
        exec(connection, "CALL gen_stu(2025101500,@g2025,2025,@d_me,@m_me01,'机械25',100,'武汉,黄石,宜昌,襄阳,鄂州,荆门,孝感,荆州,黄冈,十堰')");
        exec(connection, "CALL gen_stu(2025101600,@g2025,2025,@d_me,@m_me02,'车辆25',60,'长沙,株洲,湘潭,衡阳,邵阳,岳阳,常德,益阳,郴州')");
        exec(connection, "CALL gen_stu(2025101700,@g2025,2025,@d_me,@m_me03,'智造25',40,'成都,自贡,攀枝花,泸州,德阳,绵阳,广元,遂宁,内江')");
        exec(connection, "CALL gen_stu(2026101500,@g2026,2026,@d_me,@m_me01,'机械26',110,'合肥,芜湖,蚌埠,马鞍山,淮北,铜陵,安庆,阜阳,宿州')");
        exec(connection, "CALL gen_stu(2026101600,@g2026,2026,@d_me,@m_me02,'车辆26',65,'南昌,景德镇,萍乡,九江,新余,鹰潭,赣州,吉安,宜春')");
        exec(connection, "CALL gen_stu(2026101700,@g2026,2026,@d_me,@m_me03,'智造26',45,'南京,无锡,徐州,常州,苏州,南通,连云港,淮安,盐城,扬州')");

        // CE
        exec(connection, "CALL gen_stu(2023102000,@g2023,2023,@d_ce,@m_ce01,'土木23',70,'重庆,万州,涪陵,渝中,江北,沙坪坝,九龙坡,南岸,北碚')");
        exec(connection, "CALL gen_stu(2023102100,@g2023,2023,@d_ce,@m_ce02,'工管23',40,'贵阳,六盘水,遵义,安顺,毕节,铜仁,兴义,凯里,都匀')");
        exec(connection, "CALL gen_stu(2024102000,@g2024,2024,@d_ce,@m_ce01,'土木24',75,'昆明,曲靖,玉溪,保山,昭通,丽江,普洱,临沧,楚雄')");
        exec(connection, "CALL gen_stu(2024102100,@g2024,2024,@d_ce,@m_ce02,'工管24',45,'南宁,柳州,桂林,梧州,北海,钦州,贵港,玉林,百色')");
        exec(connection, "CALL gen_stu(2025102000,@g2025,2025,@d_ce,@m_ce01,'土木25',80,'成都,绵阳,德阳,南充,达州,宜宾,泸州,乐山,自贡,内江')");
        exec(connection, "CALL gen_stu(2025102100,@g2025,2025,@d_ce,@m_ce02,'工管25',50,'西安,宝鸡,咸阳,渭南,延安,汉中,榆林,安康,商洛,铜川')");
        exec(connection, "CALL gen_stu(2026102000,@g2026,2026,@d_ce,@m_ce01,'土木26',90,'武汉,宜昌,襄阳,十堰,荆州,黄冈,孝感,恩施,咸宁,黄石')");
        exec(connection, "CALL gen_stu(2026102100,@g2026,2026,@d_ce,@m_ce02,'工管26',55,'长沙,株洲,衡阳,岳阳,常德,郴州,永州,邵阳,益阳,怀化')");

        // Law
        exec(connection, "CALL gen_stu(2023102500,@g2023,2023,@d_law,@m_law01,'法学23',60,'济南,青岛,烟台,潍坊,威海,日照,临沂,德州,聊城,菏泽')");
        exec(connection, "CALL gen_stu(2023102600,@g2023,2023,@d_law,@m_law02,'知产23',30,'北京,上海,广州,深圳,杭州,南京,武汉,成都,西安')");
        exec(connection, "CALL gen_stu(2024102500,@g2024,2024,@d_law,@m_law01,'法学24',65,'郑州,洛阳,开封,安阳,新乡,商丘,南阳,许昌,平顶山')");
        exec(connection, "CALL gen_stu(2024102600,@g2024,2024,@d_law,@m_law02,'知产24',35,'天津,重庆,石家庄,太原,沈阳,大连,长春,哈尔滨')");
        exec(connection, "CALL gen_stu(2025102500,@g2025,2025,@d_law,@m_law01,'法学25',70,'武汉,长沙,南昌,合肥,福州,南宁,海口,昆明,贵阳')");
        exec(connection, "CALL gen_stu(2025102600,@g2025,2025,@d_law,@m_law02,'知产25',40,'南京,苏州,无锡,杭州,宁波,温州,厦门,青岛,大连')");
        exec(connection, "CALL gen_stu(2026102500,@g2026,2026,@d_law,@m_law01,'法学26',75,'成都,重庆,西安,兰州,西宁,银川,乌鲁木齐,拉萨')");
        exec(connection, "CALL gen_stu(2026102600,@g2026,2026,@d_law,@m_law02,'知产26',45,'广州,深圳,东莞,佛山,珠海,中山,惠州,江门,汕头')");

        // CH
        exec(connection, "CALL gen_stu(2023103000,@g2023,2023,@d_ch,@m_ch01,'化工23',50,'南京,无锡,常州,苏州,南通,扬州,镇江,泰州,盐城')");
        exec(connection, "CALL gen_stu(2023103100,@g2023,2023,@d_ch,@m_ch02,'应化23',35,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州')");
        exec(connection, "CALL gen_stu(2024103000,@g2024,2024,@d_ch,@m_ch01,'化工24',55,'济南,青岛,淄博,东营,烟台,潍坊,济宁,泰安,威海')");
        exec(connection, "CALL gen_stu(2024103100,@g2024,2024,@d_ch,@m_ch02,'应化24',40,'合肥,芜湖,蚌埠,马鞍山,淮北,铜陵,安庆,滁州,阜阳')");
        exec(connection, "CALL gen_stu(2025103000,@g2025,2025,@d_ch,@m_ch01,'化工25',60,'武汉,黄石,宜昌,襄阳,孝感,荆州,黄冈,鄂州,十堰')");
        exec(connection, "CALL gen_stu(2025103100,@g2025,2025,@d_ch,@m_ch02,'应化25',45,'长沙,衡阳,株洲,湘潭,岳阳,常德,郴州,永州,邵阳')");
        exec(connection, "CALL gen_stu(2026103000,@g2026,2026,@d_ch,@m_ch01,'化工26',65,'成都,德阳,绵阳,南充,宜宾,泸州,乐山,自贡,眉山,内江')");
        exec(connection, "CALL gen_stu(2026103100,@g2026,2026,@d_ch,@m_ch02,'应化26',50,'西安,宝鸡,咸阳,延安,汉中,榆林,渭南,安康,商洛')");

        // Med
        exec(connection, "CALL gen_stu(2023103500,@g2023,2023,@d_med,@m_med01,'临床23',50,'成都,绵阳,德阳,南充,宜宾,泸州,自贡,眉山')");
        exec(connection, "CALL gen_stu(2023103600,@g2023,2023,@d_med,@m_med02,'护理23',40,'重庆,成都,西安,武汉,长沙,贵阳,昆明,南宁')");
        exec(connection, "CALL gen_stu(2024103500,@g2024,2024,@d_med,@m_med01,'临床24',55,'济南,青岛,烟台,潍坊,淄博,济宁,临沂,泰安,滨州')");
        exec(connection, "CALL gen_stu(2024103600,@g2024,2024,@d_med,@m_med02,'护理24',45,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,许昌,平顶山,焦作')");
        exec(connection, "CALL gen_stu(2025103500,@g2025,2025,@d_med,@m_med01,'临床25',60,'武汉,宜昌,襄阳,荆州,十堰,孝感,黄冈,咸宁,恩施')");
        exec(connection, "CALL gen_stu(2025103600,@g2025,2025,@d_med,@m_med02,'护理25',50,'长沙,株洲,衡阳,岳阳,常德,郴州,永州,邵阳,益阳,怀化')");
        exec(connection, "CALL gen_stu(2026103500,@g2026,2026,@d_med,@m_med01,'临床26',65,'广州,深圳,东莞,佛山,珠海,惠州,中山,江门,汕头,湛江')");
        exec(connection, "CALL gen_stu(2026103600,@g2026,2026,@d_med,@m_med02,'护理26',55,'南宁,柳州,桂林,玉林,北海,钦州,百色,河池')");

        // LS
        exec(connection, "CALL gen_stu(2023103800,@g2023,2023,@d_ls,@m_ls01,'生技23',45,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁,恩施')");
        exec(connection, "CALL gen_stu(2023103900,@g2023,2023,@d_ls,@m_ls02,'食品23',30,'长沙,株洲,衡阳,岳阳,常德,湘潭,郴州')");
        exec(connection, "CALL gen_stu(2024103800,@g2024,2024,@d_ls,@m_ls01,'生技24',50,'南京,苏州,无锡,常州,徐州,南通,扬州,镇江')");
        exec(connection, "CALL gen_stu(2024103900,@g2024,2024,@d_ls,@m_ls02,'食品24',35,'杭州,宁波,温州,嘉兴,绍兴,金华,台州')");
        exec(connection, "CALL gen_stu(2025103800,@g2025,2025,@d_ls,@m_ls01,'生技25',55,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,威海')");
        exec(connection, "CALL gen_stu(2025103900,@g2025,2025,@d_ls,@m_ls02,'食品25',40,'成都,绵阳,德阳,宜宾,南充,泸州,乐山,自贡,眉山')");
        exec(connection, "CALL gen_stu(2026103800,@g2026,2026,@d_ls,@m_ls01,'生技26',60,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州,六安,宣城')");
        exec(connection, "CALL gen_stu(2026103900,@g2026,2026,@d_ls,@m_ls02,'食品26',45,'南昌,九江,赣州,吉安,宜春,上饶,抚州,景德镇')");

        // FL
        exec(connection, "CALL gen_stu(2023104200,@g2023,2023,@d_fl,@m_fl01,'英语23',40,'上海,北京,广州,深圳,杭州,南京,成都,重庆,武汉')");
        exec(connection, "CALL gen_stu(2023104300,@g2023,2023,@d_fl,@m_fl02,'日语23',25,'大连,青岛,厦门,苏州,无锡,天津,西安,长沙')");
        exec(connection, "CALL gen_stu(2024104200,@g2024,2024,@d_fl,@m_fl01,'英语24',45,'济南,青岛,烟台,威海,日照,潍坊,淄博,泰安,济宁')");
        exec(connection, "CALL gen_stu(2024104300,@g2024,2024,@d_fl,@m_fl02,'日语24',28,'沈阳,长春,哈尔滨,大连,延边,丹东,抚顺,吉林')");
        exec(connection, "CALL gen_stu(2025104200,@g2025,2025,@d_fl,@m_fl01,'英语25',50,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州')");
        exec(connection, "CALL gen_stu(2025104300,@g2025,2025,@d_fl,@m_fl02,'日语25',30,'福州,厦门,泉州,漳州,龙岩,三明,南平,莆田,宁德')");
        exec(connection, "CALL gen_stu(2026104200,@g2026,2026,@d_fl,@m_fl01,'英语26',55,'广州,深圳,珠海,东莞,佛山,中山,惠州')");
        exec(connection, "CALL gen_stu(2026104300,@g2026,2026,@d_fl,@m_fl02,'日语26',35,'成都,重庆,昆明,贵阳,南宁,海口,拉萨')");

        // EDU
        exec(connection, "CALL gen_stu(2023104800,@g2023,2023,@d_edu,@m_edu01,'教育23',40,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,信阳')");
        exec(connection, "CALL gen_stu(2023104900,@g2023,2023,@d_edu,@m_edu02,'学前23',30,'石家庄,唐山,邯郸,保定,秦皇岛,沧州,廊坊')");
        exec(connection, "CALL gen_stu(2024104800,@g2024,2024,@d_edu,@m_edu01,'教育24',45,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,威海')");
        exec(connection, "CALL gen_stu(2024104900,@g2024,2024,@d_edu,@m_edu02,'学前24',35,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁,恩施')");
        exec(connection, "CALL gen_stu(2025104800,@g2025,2025,@d_edu,@m_edu01,'教育25',50,'长沙,株洲,衡阳,岳阳,常德,湘潭,郴州,永州')");
        exec(connection, "CALL gen_stu(2025104900,@g2025,2025,@d_edu,@m_edu02,'学前25',40,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州')");
        exec(connection, "CALL gen_stu(2026104800,@g2026,2026,@d_edu,@m_edu01,'教育26',55,'成都,绵阳,德阳,南充,宜宾,泸州,自贡,眉山')");
        exec(connection, "CALL gen_stu(2026104900,@g2026,2026,@d_edu,@m_edu02,'学前26',45,'昆明,曲靖,玉溪,大理,保山,红河,普洱')");

        // JR
        exec(connection, "CALL gen_stu(2023105200,@g2023,2023,@d_jr,@m_jr01,'新闻23',35,'北京,上海,广州,深圳,杭州,南京,武汉,成都')");
        exec(connection, "CALL gen_stu(2023105300,@g2023,2023,@d_jr,@m_jr02,'网新23',25,'长沙,西安,重庆,天津,郑州,济南,青岛')");
        exec(connection, "CALL gen_stu(2024105200,@g2024,2024,@d_jr,@m_jr01,'新闻24',40,'福州,厦门,泉州,漳州,龙岩,三明,南平,莆田')");
        exec(connection, "CALL gen_stu(2024105300,@g2024,2024,@d_jr,@m_jr02,'网新24',30,'沈阳,大连,长春,哈尔滨,吉林,牡丹江,大庆')");
        exec(connection, "CALL gen_stu(2025105200,@g2025,2025,@d_jr,@m_jr01,'新闻25',45,'成都,绵阳,德阳,宜宾,南充,泸州,乐山,自贡')");
        exec(connection, "CALL gen_stu(2025105300,@g2025,2025,@d_jr,@m_jr02,'网新25',35,'南昌,九江,赣州,吉安,上饶,景德镇,宜春')");
        exec(connection, "CALL gen_stu(2026105200,@g2026,2026,@d_jr,@m_jr01,'新闻26',50,'武汉,宜昌,襄阳,荆州,黄冈,孝感,黄石,十堰')");
        exec(connection, "CALL gen_stu(2026105300,@g2026,2026,@d_jr,@m_jr02,'网新26',40,'合肥,芜湖,马鞍山,蚌埠,安庆,阜阳,滁州')");

        // MS
        exec(connection, "CALL gen_stu(2023105600,@g2023,2023,@d_ms,@m_ms01,'数学23',35,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂')");
        exec(connection, "CALL gen_stu(2023105700,@g2023,2023,@d_ms,@m_ms02,'统计23',25,'南京,苏州,无锡,常州,南通,扬州,镇江')");
        exec(connection, "CALL gen_stu(2023105800,@g2023,2023,@d_ms,@m_ms03,'数计23',15,'杭州,宁波,温州,嘉兴,绍兴,金华')");
        exec(connection, "CALL gen_stu(2024105600,@g2024,2024,@d_ms,@m_ms01,'数学24',40,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁')");
        exec(connection, "CALL gen_stu(2024105700,@g2024,2024,@d_ms,@m_ms02,'统计24',30,'长沙,株洲,衡阳,岳阳,常德,湘潭')");
        exec(connection, "CALL gen_stu(2024105800,@g2024,2024,@d_ms,@m_ms03,'数计24',18,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳')");
        exec(connection, "CALL gen_stu(2025105600,@g2025,2025,@d_ms,@m_ms01,'数学25',45,'成都,绵阳,德阳,南充,宜宾,泸州,乐山')");
        exec(connection, "CALL gen_stu(2025105700,@g2025,2025,@d_ms,@m_ms02,'统计25',35,'郑州,洛阳,开封,新乡,安阳,商丘,南阳')");
        exec(connection, "CALL gen_stu(2025105800,@g2025,2025,@d_ms,@m_ms03,'数计25',20,'石家庄,唐山,邯郸,保定,秦皇岛,沧州')");
        exec(connection, "CALL gen_stu(2026105600,@g2026,2026,@d_ms,@m_ms01,'数学26',50,'广州,深圳,东莞,佛山,珠海,惠州,中山')");
        exec(connection, "CALL gen_stu(2026105700,@g2026,2026,@d_ms,@m_ms02,'统计26',40,'南昌,九江,赣州,吉安,宜春,上饶,景德镇')");
        exec(connection, "CALL gen_stu(2026105800,@g2026,2026,@d_ms,@m_ms03,'数计26',25,'福州,厦门,泉州,漳州,龙岩,三明')");

        // AD
        exec(connection, "CALL gen_stu(2023106000,@g2023,2023,@d_ad,@m_ad01,'视传23',30,'北京,上海,广州,深圳,杭州,南京,成都,重庆,西安')");
        exec(connection, "CALL gen_stu(2023106100,@g2023,2023,@d_ad,@m_ad02,'环设23',20,'武汉,长沙,郑州,济南,青岛,厦门,苏州,无锡')");
        exec(connection, "CALL gen_stu(2023106200,@g2023,2023,@d_ad,@m_ad03,'数媒23',15,'天津,沈阳,大连,长春,哈尔滨,合肥,南昌')");
        exec(connection, "CALL gen_stu(2024106000,@g2024,2024,@d_ad,@m_ad01,'视传24',35,'杭州,宁波,温州,嘉兴,湖州,绍兴,金华,台州,丽水')");
        exec(connection, "CALL gen_stu(2024106100,@g2024,2024,@d_ad,@m_ad02,'环设24',25,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州')");
        exec(connection, "CALL gen_stu(2024106200,@g2024,2024,@d_ad,@m_ad03,'数媒24',18,'成都,绵阳,德阳,宜宾,南充,泸州,自贡')");
        exec(connection, "CALL gen_stu(2025106000,@g2025,2025,@d_ad,@m_ad01,'视传25',40,'广州,深圳,东莞,佛山,珠海,中山,惠州,汕头,湛江')");
        exec(connection, "CALL gen_stu(2025106100,@g2025,2025,@d_ad,@m_ad02,'环设25',30,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁,黄石')");
        exec(connection, "CALL gen_stu(2025106200,@g2025,2025,@d_ad,@m_ad03,'数媒25',20,'长沙,株洲,衡阳,岳阳,常德,湘潭,郴州')");
        exec(connection, "CALL gen_stu(2026106000,@g2026,2026,@d_ad,@m_ad01,'视传26',45,'上海,北京,深圳,杭州,南京,成都,重庆,武汉,长沙')");
        exec(connection, "CALL gen_stu(2026106100,@g2026,2026,@d_ad,@m_ad02,'环设26',35,'西安,郑州,济南,青岛,合肥,南昌,福州,厦门')");
        exec(connection, "CALL gen_stu(2026106200,@g2026,2026,@d_ad,@m_ad03,'数媒26',25,'沈阳,大连,长春,哈尔滨,天津,石家庄,太原')");

        // enrollment
        exec(connection, """
            INSERT IGNORE INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
            SELECT m.major_id, y.year,
                   50 + FLOOR(RAND() * 130) AS plan_count,
                   0 AS actual_count,
                   NULL AS report_rate
            FROM major m
            CROSS JOIN (SELECT 2023 AS year UNION ALL SELECT 2024 UNION ALL SELECT 2025 UNION ALL SELECT 2026) y
        """);

        exec(connection, """
            UPDATE enrollment e
            JOIN (
                SELECT major_id, enroll_year AS year, COUNT(*) AS cnt
                FROM student WHERE status = 1
                GROUP BY major_id, enroll_year
            ) s ON s.major_id = e.major_id AND s.year = e.year
            SET e.actual_count = s.cnt,
                e.report_rate = ROUND(s.cnt / e.plan_count * 100, 2)
            WHERE e.plan_count > 0
        """);

        exec(connection, "UPDATE enrollment SET actual_count = plan_count * 0.8, report_rate = 80.00 WHERE actual_count = 0 AND year <= 2025 AND plan_count > 0");

        exec(connection, "DROP PROCEDURE IF EXISTS gen_stu");
        exec(connection, "SELECT '院系数: ', COUNT(*) FROM department");
        exec(connection, "SELECT '专业数: ', COUNT(*) FROM major");
        exec(connection, "SELECT '学生总数: ', COUNT(*) FROM student");
        exec(connection, "SELECT '招生计划数: ', COUNT(*) FROM enrollment");
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
