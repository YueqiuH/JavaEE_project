package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V8__base_supplement_data extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        // 补充学生至省属重点大学规模（~1.8 万）
        // 在已有7084 人基础上追加~11000 人

        // 设置部门变量
        exec(connection, "SET @d_cs := (SELECT dept_id FROM department WHERE dept_code = 'CS')");
        exec(connection, "SET @d_ee := (SELECT dept_id FROM department WHERE dept_code = 'EE')");
        exec(connection, "SET @d_em := (SELECT dept_id FROM department WHERE dept_code = 'EM')");
        exec(connection, "SET @d_me := (SELECT dept_id FROM department WHERE dept_code = 'ME')");
        exec(connection, "SET @d_ce := (SELECT dept_id FROM department WHERE dept_code = 'CE')");
        exec(connection, "SET @d_law := (SELECT dept_id FROM department WHERE dept_code = 'LAW')");
        exec(connection, "SET @d_ch := (SELECT dept_id FROM department WHERE dept_code = 'CH')");
        exec(connection, "SET @d_med := (SELECT dept_id FROM department WHERE dept_code = 'MED')");
        exec(connection, "SET @d_ls := (SELECT dept_id FROM department WHERE dept_code = 'LS')");
        exec(connection, "SET @d_fl := (SELECT dept_id FROM department WHERE dept_code = 'FL')");
        exec(connection, "SET @d_edu := (SELECT dept_id FROM department WHERE dept_code = 'EDU')");
        exec(connection, "SET @d_jr := (SELECT dept_id FROM department WHERE dept_code = 'JR')");
        exec(connection, "SET @d_ms := (SELECT dept_id FROM department WHERE dept_code = 'MS')");
        exec(connection, "SET @d_ad := (SELECT dept_id FROM department WHERE dept_code = 'AD')");

        exec(connection, "SET @g2023 := (SELECT grade_id FROM grade WHERE grade_name = '2023级' LIMIT 1)");
        exec(connection, "SET @g2024 := (SELECT grade_id FROM grade WHERE grade_name = '2024级' LIMIT 1)");
        exec(connection, "SET @g2025 := (SELECT grade_id FROM grade WHERE grade_name = '2025级' LIMIT 1)");
        exec(connection, "SET @g2026 := (SELECT grade_id FROM grade WHERE grade_name = '2026级' LIMIT 1)");

        // 创建存储过程（JDBC 不需要 DELIMITER）
        exec(connection, "DROP PROCEDURE IF EXISTS add_stu");
        exec(connection, """
            CREATE PROCEDURE add_stu(
                IN base_no BIGINT, IN gid BIGINT, IN ey INT,
                IN did BIGINT, IN mid BIGINT,
                IN cprefix VARCHAR(32), IN cnt INT,
                IN oris TEXT
            )
            BEGIN
                DECLARE i INT DEFAULT 1;
                DECLARE total_ori INT;
                SET total_ori = CHAR_LENGTH(oris) - CHAR_LENGTH(REPLACE(oris, ',', '')) + 1;
                WHILE i <= cnt DO
                    INSERT IGNORE INTO student (
                        student_no, student_name, gender, student_birth, student_age,
                        student_address, grade_id, dept_id, major_id, class_name,
                        origin_place, enroll_year, status
                    ) VALUES (
                        base_no + i,
                        CONCAT(ELT(1+FLOOR(RAND()*8),'张','王','李','赵','陈','刘','黄','周','吴','郑','孙','马'),
                               ELT(1+FLOOR(RAND()*6),'子涵','雨涵','浩然','思琪','天宇','梦瑶','志远','若曦','博文','晓萱'),
                               ELT(1+FLOOR(RAND()*4),'','','','杰','强','彬','然','杰','杰','宇','华','磊','飞','波','涛','凯')),
                        IF(i%4=0,2,1),
                        DATE_ADD('2006-06-01', INTERVAL (i*23 % 365) DAY),
                        19 + ey - 2024,
                        CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(oris, ',', 1 + ((i-1) % total_ori)), ',', -1), '市'),
                        gid, did, mid,
                        CONCAT(cprefix, LPAD(3 + (i-1) DIV 45, 2, '0')),
                        SUBSTRING_INDEX(SUBSTRING_INDEX(oris, ',', 1 + ((i-1) % total_ori)), ',', -1),
                        ey,
                        CASE WHEN i <= cnt-2 THEN 1 WHEN RAND() < 0.3 THEN 0 ELSE 2 END
                    );
                    SET i = i + 1;
                END WHILE;
            END
        """);

        // ========== 大院翻2-3倍：CS/EE/EM/ME 加大量 ==========

        // CS (+3200: 每级+800)
        call(connection, "2023200000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件23A',200,'济南,青岛,烟台,潍坊,临沂,济宁,淄博,泰安,郑州,洛阳,新乡,安阳,开封,南阳,许昌'");
        call(connection, "2023201000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能23A',150,'北京,上海,深圳,广州,杭州,南京,武汉,成都,西安,重庆,天津,苏州'");
        call(connection, "2023202000,@g2023,2023,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据23A',100,'长沙,合肥,南昌,福州,南宁,昆明,贵阳,兰州,太原'");
        call(connection, "2024200000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件24A',220,'济南,青岛,郑州,西安,武汉,成都,南京,合肥,长沙,石家庄'");
        call(connection, "2024201000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能24A',160,'北京,上海,杭州,深圳,广州,南京,苏州,无锡,宁波,厦门'");
        call(connection, "2024202000,@g2024,2024,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据24A',110,'成都,重庆,武汉,长沙,西安,南昌,福州,南宁,昆明,贵阳'");
        call(connection, "2025200000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件25A',240,'南京,杭州,苏州,无锡,合肥,武汉,成都,西安,济南,青岛'");
        call(connection, "2025201000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能25A',170,'北京,上海,广州,深圳,天津,重庆,武汉,成都,杭州,南京'");
        call(connection, "2025202000,@g2025,2025,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据25A',120,'西安,兰州,西宁,银川,乌鲁木齐,太原,呼和浩特,沈阳,大连,长春,哈尔滨'");
        call(connection, "2026200000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件26A',260,'广州,深圳,东莞,佛山,珠海,惠州,中山,江门,湛江,汕头'");
        call(connection, "2026201000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能26A',180,'上海,杭州,南京,苏州,无锡,宁波,合肥,武汉,长沙,成都,重庆'");
        call(connection, "2026202000,@g2026,2026,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据26A',130,'北京,天津,石家庄,济南,青岛,郑州,西安,沈阳,大连,哈尔滨,长春'");

        // EM (+1800: 每级+450)
        call(connection, "2023203000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计23A',130,'广州,深圳,东莞,佛山,珠海,中山,惠州,江门,肇庆,汕头,揭阳'");
        call(connection, "2023204000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管23A',90,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州,衢州,丽水,舟山'");
        call(connection, "2023205000,@g2023,2023,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融23A',80,'上海,北京,深圳,广州,杭州,南京,苏州,成都,武汉,天津,重庆'");
        call(connection, "2024203000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计24A',140,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,许昌,平顶山,焦作,周口'");
        call(connection, "2024204000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管24A',95,'合肥,芜湖,蚌埠,马鞍山,铜陵,安庆,阜阳,宿州,滁州,宣城'");
        call(connection, "2024205000,@g2024,2024,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融24A',85,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州,盐城,徐州'");
        call(connection, "2025203000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计25A',150,'成都,绵阳,德阳,宜宾,南充,泸州,达州,乐山,眉山,自贡,内江'");
        call(connection, "2025204000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管25A',100,'南昌,九江,赣州,吉安,宜春,抚州,上饶,景德镇,萍乡'");
        call(connection, "2025205000,@g2025,2025,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融25A',90,'福州,厦门,泉州,漳州,龙岩,三明,南平,宁德,莆田'");
        call(connection, "2026203000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计26A',160,'武汉,黄石,宜昌,襄阳,荆州,十堰,孝感,黄冈,咸宁,恩施,随州'");
        call(connection, "2026204000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管26A',105,'长沙,株洲,衡阳,岳阳,常德,郴州,永州,邵阳,益阳,怀化,娄底'");
        call(connection, "2026205000,@g2026,2026,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融26A',95,'昆明,曲靖,玉溪,大理,保山,红河,文山,普洱,西双版纳'");

        // EE (+1500: 每级+375)
        call(connection, "2023206000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信23A',120,'成都,绵阳,德阳,宜宾,南充,泸州,内江,自贡,眉山,广元,遂宁,达州'");
        call(connection, "2023207000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信23A',80,'西安,咸阳,宝鸡,渭南,汉中,延安,榆林,安康,商洛,铜川'");
        call(connection, "2023208000,@g2023,2023,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联23A',70,'深圳,广州,杭州,南京,武汉,成都,北京,上海,苏州,无锡'");
        call(connection, "2024206000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信24A',130,'武汉,宜昌,襄阳,荆州,黄冈,孝感,黄石,十堰,咸宁,恩施,荆门'");
        call(connection, "2024207000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信24A',90,'南京,苏州,无锡,常州,徐州,南通,扬州,镇江,盐城,泰州'");
        call(connection, "2024208000,@g2024,2024,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联24A',75,'合肥,芜湖,蚌埠,马鞍山,安庆,铜陵,阜阳,滁州,六安,宿州'");
        call(connection, "2025206000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信25A',140,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,滨州,东营,威海'");
        call(connection, "2025207000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信25A',95,'长沙,株洲,湘潭,岳阳,衡阳,常德,郴州,永州,邵阳,娄底,怀化'");
        call(connection, "2025208000,@g2025,2025,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联25A',80,'郑州,洛阳,新乡,安阳,开封,商丘,南阳,许昌,周口,平顶山'");
        call(connection, "2026206000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信26A',150,'广州,深圳,东莞,佛山,珠海,中山,惠州,汕头,江门,湛江,茂名'");
        call(connection, "2026207000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信26A',100,'成都,重庆,西安,兰州,西宁,银川,乌鲁木齐,拉萨'");
        call(connection, "2026208000,@g2026,2026,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联26A',85,'北京,天津,石家庄,太原,呼和浩特,沈阳,大连,长春,哈尔滨'");

        // ME (+1400: 每级+350)
        call(connection, "2023209000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械23A',130,'沈阳,大连,鞍山,抚顺,本溪,丹东,锦州,营口,阜新,辽阳,盘锦'");
        call(connection, "2023210000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆23A',80,'长春,吉林,四平,辽源,通化,白山,松原,白城,延边'");
        call(connection, "2023211000,@g2023,2023,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造23A',60,'哈尔滨,齐齐哈尔,牡丹江,佳木斯,大庆,鸡西,鹤岗,伊春,黑河'");
        call(connection, "2024209000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械24A',140,'济南,青岛,淄博,枣庄,东营,烟台,潍坊,济宁,泰安,威海,日照'");
        call(connection, "2024210000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆24A',85,'郑州,洛阳,开封,平顶山,安阳,鹤壁,新乡,焦作,濮阳,许昌'");
        call(connection, "2024211000,@g2024,2024,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造24A',65,'长沙,株洲,湘潭,衡阳,邵阳,岳阳,常德,益阳,郴州,永州'");
        call(connection, "2025209000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械25A',150,'武汉,黄石,宜昌,襄阳,鄂州,荆门,孝感,荆州,黄冈,咸宁,十堰,随州'");
        call(connection, "2025210000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆25A',90,'合肥,芜湖,蚌埠,马鞍山,淮北,铜陵,安庆,阜阳,宿州,滁州'");
        call(connection, "2025211000,@g2025,2025,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造25A',70,'成都,自贡,攀枝花,泸州,德阳,绵阳,广元,遂宁,内江,乐山'");
        call(connection, "2026209000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械26A',160,'南京,无锡,苏州,常州,南通,徐州,扬州,镇江,泰州,盐城,连云港'");
        call(connection, "2026210000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆26A',95,'南昌,景德镇,萍乡,九江,新余,鹰潭,赣州,吉安,宜春,抚州'");
        call(connection, "2026211000,@g2026,2026,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造26A',75,'西安,宝鸡,咸阳,渭南,延安,汉中,安康,商洛,榆林,铜川'");

        // ========== 中院翻倍：CE/Law/CH/Med/LS 加适量 ==========

        // CE (+600: 每级+150)
        call(connection, "2023213000,@g2023,2023,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木23A',80,'重庆,万州,涪陵,渝中,江北,沙坪坝,南岸,北碚,巴南'");
        call(connection, "2023214000,@g2023,2023,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管CE23A',50,'贵阳,遵义,六盘水,安顺,毕节,铜仁,兴义,凯里'");
        call(connection, "2024213000,@g2024,2024,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木24A',85,'昆明,曲靖,玉溪,保山,昭通,丽江,普洱,临沧'");
        call(connection, "2024214000,@g2024,2024,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管CE24A',55,'南宁,柳州,桂林,梧州,北海,钦州,贵港,玉林'");
        call(connection, "2025213000,@g2025,2025,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木25A',90,'成都,绵阳,德阳,南充,达州,宜宾,泸州,乐山,自贡,内江'");
        call(connection, "2025214000,@g2025,2025,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管CE25A',60,'西安,宝鸡,咸阳,渭南,延安,汉中,榆林,安康,商洛,铜川'");
        call(connection, "2026213000,@g2026,2026,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木26A',95,'武汉,宜昌,襄阳,十堰,荆州,黄冈,孝感,恩施,咸宁,黄石'");
        call(connection, "2026214000,@g2026,2026,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管CE26A',65,'长沙,株洲,衡阳,岳阳,常德,郴州,永州,邵阳,益阳,怀化'");

        // Law (+500: 每级+125)
        call(connection, "2023216000,@g2023,2023,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学23A',60,'北京,上海,广州,深圳,杭州,南京,武汉,成都,西安,济南'");
        call(connection, "2023217000,@g2023,2023,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产23A',35,'天津,重庆,石家庄,沈阳,大连,长春,哈尔滨,青岛,厦门'");
        call(connection, "2024216000,@g2024,2024,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学24A',65,'济南,青岛,烟台,潍坊,威海,日照,临沂,德州,淄博,泰安'");
        call(connection, "2024217000,@g2024,2024,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产24A',40,'武汉,长沙,南昌,合肥,福州,南宁,海口,昆明,贵阳'");
        call(connection, "2025216000,@g2025,2025,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学25A',70,'南京,苏州,无锡,杭州,宁波,温州,厦门,青岛,大连,西安'");
        call(connection, "2025217000,@g2025,2025,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产25A',45,'成都,重庆,西安,兰州,西宁,银川,乌鲁木齐'");
        call(connection, "2026216000,@g2026,2026,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学26A',75,'广州,深圳,东莞,佛山,珠海,中山,惠州,江门,汕头,湛江'");
        call(connection, "2026217000,@g2026,2026,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产26A',50,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,许昌,平顶山'");

        // CH (+400: 每级+100)
        call(connection, "2023219000,@g2023,2023,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工23A',55,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州,盐城'");
        call(connection, "2023220000,@g2023,2023,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化23A',35,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州,衢州'");
        call(connection, "2024219000,@g2024,2024,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工24A',60,'济南,青岛,淄博,东营,烟台,潍坊,济宁,泰安,威海,日照'");
        call(connection, "2024220000,@g2024,2024,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化24A',40,'合肥,芜湖,蚌埠,马鞍山,淮北,铜陵,安庆,滁州,阜阳'");
        call(connection, "2025219000,@g2025,2025,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工25A',65,'武汉,黄石,宜昌,襄阳,孝感,荆州,黄冈,鄂州,十堰'");
        call(connection, "2025220000,@g2025,2025,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化25A',45,'长沙,衡阳,株洲,湘潭,岳阳,常德,郴州,永州,邵阳'");
        call(connection, "2026219000,@g2026,2026,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工26A',70,'成都,德阳,绵阳,南充,宜宾,泸州,乐山,自贡,眉山,内江'");
        call(connection, "2026220000,@g2026,2026,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化26A',50,'西安,宝鸡,咸阳,延安,汉中,榆林,渭南,安康,商洛,铜川'");

        // Med (+350: 每级+90)
        call(connection, "2023222000,@g2023,2023,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床23A',55,'重庆,成都,贵阳,昆明,西安,武汉,长沙,广州,南宁'");
        call(connection, "2023223000,@g2023,2023,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理23A',40,'济南,青岛,郑州,合肥,南昌,福州,杭州,南京,上海'");
        call(connection, "2024222000,@g2024,2024,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床24A',60,'成都,绵阳,德阳,南充,宜宾,泸州,自贡,内江,眉山'");
        call(connection, "2024223000,@g2024,2024,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理24A',45,'武汉,长沙,南昌,合肥,南京,杭州,福州,厦门,广州'");
        call(connection, "2025222000,@g2025,2025,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床25A',65,'北京,天津,石家庄,太原,呼和浩特,沈阳,大连,长春,哈尔滨'");
        call(connection, "2025223000,@g2025,2025,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理25A',50,'西安,兰州,西宁,银川,乌鲁木齐,拉萨,昆明,贵阳,南宁'");
        call(connection, "2026222000,@g2026,2026,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床26A',70,'广州,深圳,东莞,佛山,珠海,中山,江门,湛江,茂名,汕头'");
        call(connection, "2026223000,@g2026,2026,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理26A',55,'上海,北京,天津,重庆,杭州,南京,武汉,成都,西安,长沙'");

        // LS (+350: 每级+90)
        call(connection, "2023225000,@g2023,2023,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技23A',55,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁,恩施,黄石'");
        call(connection, "2023226000,@g2023,2023,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品23A',40,'长沙,株洲,衡阳,岳阳,常德,湘潭,郴州,永州,邵阳'");
        call(connection, "2024225000,@g2024,2024,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技24A',60,'南京,苏州,无锡,常州,徐州,南通,扬州,镇江,泰州,盐城'");
        call(connection, "2024226000,@g2024,2024,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品24A',45,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州,衢州'");
        call(connection, "2025225000,@g2025,2025,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技25A',65,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,威海,日照'");
        call(connection, "2025226000,@g2025,2025,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品25A',50,'成都,绵阳,德阳,宜宾,南充,泸州,乐山,自贡,内江'");
        call(connection, "2026225000,@g2026,2026,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技26A',70,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州,六安,宣城'");
        call(connection, "2026226000,@g2026,2026,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品26A',55,'南昌,九江,赣州,吉安,宜春,上饶,景德镇,萍乡'");

        // ========== 小院加少量 ==========
        // FL (+300: 每级+75)
        call(connection, "2023228000,@g2023,2023,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语23A',45,'上海,北京,广州,深圳,杭州,南京,成都,重庆,武汉,长沙'");
        call(connection, "2023229000,@g2023,2023,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语23A',30,'大连,青岛,厦门,苏州,无锡,宁波,天津,西安,沈阳'");
        call(connection, "2024228000,@g2024,2024,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语24A',50,'济南,青岛,烟台,威海,日照,潍坊,淄博,泰安,济宁,临沂'");
        call(connection, "2024229000,@g2024,2024,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语24A',35,'福州,厦门,泉州,漳州,龙岩,三明,南平,莆田,宁德'");
        call(connection, "2025228000,@g2025,2025,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语25A',55,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州,盐城,徐州'");
        call(connection, "2025229000,@g2025,2025,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语25A',40,'广州,深圳,珠海,东莞,佛山,中山,惠州,汕头,湛江'");
        call(connection, "2026228000,@g2026,2026,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语26A',60,'武汉,宜昌,襄阳,荆州,黄冈,孝感,黄石,十堰,咸宁'");
        call(connection, "2026229000,@g2026,2026,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语26A',45,'成都,重庆,昆明,贵阳,南宁,海口,拉萨,西安,兰州'");

        // EDU (+300: 每级+75)
        call(connection, "2023231000,@g2023,2023,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育23A',45,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,信阳,周口'");
        call(connection, "2023232000,@g2023,2023,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前23A',35,'武汉,长沙,南昌,合肥,南京,杭州,福州,济南'");
        call(connection, "2024231000,@g2024,2024,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育24A',50,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂,威海,日照'");
        call(connection, "2024232000,@g2024,2024,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前24A',40,'成都,重庆,贵阳,昆明,西安,兰州,西宁,银川'");
        call(connection, "2025231000,@g2025,2025,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育25A',55,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州,六安,宣城'");
        call(connection, "2025232000,@g2025,2025,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前25A',45,'石家庄,唐山,邯郸,保定,秦皇岛,沧州,廊坊,张家口'");
        call(connection, "2026231000,@g2026,2026,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育26A',60,'广州,深圳,东莞,佛山,珠海,中山,惠州,汕头,湛江'");
        call(connection, "2026232000,@g2026,2026,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前26A',50,'昆明,曲靖,玉溪,大理,保山,红河,普洱,文山'");

        // JR (+250: 每级+60)
        call(connection, "2023234000,@g2023,2023,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻23A',35,'北京,上海,广州,深圳,杭州,南京,武汉,成都,西安'");
        call(connection, "2023235000,@g2023,2023,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新23A',25,'长沙,重庆,天津,郑州,济南,青岛,合肥,南昌'");
        call(connection, "2024234000,@g2024,2024,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻24A',40,'福州,厦门,泉州,漳州,龙岩,三明,南平,莆田,宁德'");
        call(connection, "2024235000,@g2024,2024,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新24A',30,'沈阳,大连,长春,哈尔滨,吉林,牡丹江,大庆'");
        call(connection, "2025234000,@g2025,2025,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻25A',45,'成都,绵阳,德阳,宜宾,南充,泸州,乐山,自贡,眉山'");
        call(connection, "2025235000,@g2025,2025,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新25A',35,'南昌,九江,赣州,吉安,上饶,景德镇,宜春,抚州'");
        call(connection, "2026234000,@g2026,2026,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻26A',50,'武汉,宜昌,襄阳,荆州,黄冈,孝感,黄石,十堰,咸宁'");
        call(connection, "2026235000,@g2026,2026,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新26A',40,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州,六安'");

        // MS (+250: 每级+60)
        call(connection, "2023237000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学23A',35,'南京,杭州,合肥,济南,武汉,长沙,成都'");
        call(connection, "2023238000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计23A',25,'上海,北京,深圳,广州,西安,郑州,南昌'");
        call(connection, "2023239000,@g2023,2023,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计23A',20,'苏州,无锡,宁波,厦门,福州,青岛,大连'");
        call(connection, "2024237000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学24A',40,'济南,青岛,烟台,潍坊,淄博,泰安,济宁,临沂'");
        call(connection, "2024238000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计24A',30,'武汉,宜昌,襄阳,荆州,黄冈,孝感,咸宁,十堰'");
        call(connection, "2024239000,@g2024,2024,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计24A',25,'长沙,株洲,衡阳,岳阳,常德,湘潭,郴州,永州'");
        call(connection, "2025237000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学25A',45,'成都,绵阳,德阳,南充,宜宾,泸州,乐山,自贡,眉山'");
        call(connection, "2025238000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计25A',35,'郑州,洛阳,开封,新乡,安阳,商丘,南阳,许昌,周口'");
        call(connection, "2025239000,@g2025,2025,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计25A',30,'合肥,芜湖,蚌埠,马鞍山,安庆,阜阳,滁州,宣城'");
        call(connection, "2026237000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学26A',50,'广州,深圳,东莞,佛山,珠海,中山,惠州,汕头,湛江'");
        call(connection, "2026238000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计26A',40,'南京,苏州,无锡,常州,南通,扬州,镇江,泰州,盐城'");
        call(connection, "2026239000,@g2026,2026,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计26A',35,'南昌,九江,赣州,吉安,宜春,上饶,景德镇,抚州'");

        // AD (+250: 每级+60)
        call(connection, "2023241000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传23A',35,'上海,北京,杭州,成都,重庆,武汉,南京,广州,深圳'");
        call(connection, "2023242000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设23A',25,'苏州,无锡,宁波,厦门,青岛,大连,长沙,西安'");
        call(connection, "2023243000,@g2023,2023,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒23A',20,'天津,沈阳,济南,合肥,南昌,福州,昆明,南宁'");
        call(connection, "2024241000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传24A',40,'成都,重庆,西安,昆明,贵阳,南宁,长沙,武汉,郑州'");
        call(connection, "2024242000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设24A',30,'杭州,南京,苏州,无锡,宁波,温州,嘉兴,绍兴'");
        call(connection, "2024243000,@g2024,2024,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒24A',25,'广州,深圳,东莞,佛山,珠海,中山,厦门,福州'");
        call(connection, "2025241000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传25A',45,'北京,上海,深圳,杭州,南京,成都,重庆,武汉,长沙,广州'");
        call(connection, "2025242000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设25A',35,'西安,郑州,济南,青岛,合肥,南昌,福州,厦门,大连'");
        call(connection, "2025243000,@g2025,2025,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒25A',30,'苏州,无锡,常州,南通,扬州,镇江,泰州,盐城,徐州'");
        call(connection, "2026241000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传26A',50,'上海,北京,深圳,杭州,南京,成都,重庆,武汉,长沙,广州'");
        call(connection, "2026242000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设26A',40,'西安,郑州,济南,青岛,合肥,南昌,福州,厦门'");
        call(connection, "2026243000,@g2026,2026,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒26A',35,'沈阳,大连,长春,哈尔滨,天津,石家庄,太原'");

        // 更新招生计划
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

        exec(connection, "DROP PROCEDURE IF EXISTS add_stu");
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void call(Connection connection, String args) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CALL add_stu(" + args + ")");
        }
    }
}
