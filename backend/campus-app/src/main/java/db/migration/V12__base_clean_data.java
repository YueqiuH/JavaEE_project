package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Connection;
import java.sql.Statement;

public class V12__base_clean_data extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        exec(connection, "DELETE FROM student");
        exec(connection, "DELETE FROM enrollment");

        exec(connection, "DELETE FROM grade");
        exec(connection, "INSERT INTO grade (grade_name) VALUES ('2022级'),('2023级'),('2024级'),('2025级')");

        exec(connection, "SET @g22 := (SELECT grade_id FROM grade WHERE grade_name='2022级')");
        exec(connection, "SET @g23 := (SELECT grade_id FROM grade WHERE grade_name='2023级')");
        exec(connection, "SET @g24 := (SELECT grade_id FROM grade WHERE grade_name='2024级')");
        exec(connection, "SET @g25 := (SELECT grade_id FROM grade WHERE grade_name='2025级')");

        exec(connection, "SET @d_cs  := (SELECT dept_id FROM department WHERE dept_code='CS')");
        exec(connection, "SET @d_ee  := (SELECT dept_id FROM department WHERE dept_code='EE')");
        exec(connection, "SET @d_em  := (SELECT dept_id FROM department WHERE dept_code='EM')");
        exec(connection, "SET @d_me  := (SELECT dept_id FROM department WHERE dept_code='ME')");
        exec(connection, "SET @d_ce  := (SELECT dept_id FROM department WHERE dept_code='CE')");
        exec(connection, "SET @d_law := (SELECT dept_id FROM department WHERE dept_code='LAW')");
        exec(connection, "SET @d_ch  := (SELECT dept_id FROM department WHERE dept_code='CH')");
        exec(connection, "SET @d_med := (SELECT dept_id FROM department WHERE dept_code='MED')");
        exec(connection, "SET @d_ls  := (SELECT dept_id FROM department WHERE dept_code='LS')");
        exec(connection, "SET @d_fl  := (SELECT dept_id FROM department WHERE dept_code='FL')");
        exec(connection, "SET @d_edu := (SELECT dept_id FROM department WHERE dept_code='EDU')");
        exec(connection, "SET @d_jr  := (SELECT dept_id FROM department WHERE dept_code='JR')");
        exec(connection, "SET @d_ms  := (SELECT dept_id FROM department WHERE dept_code='MS')");
        exec(connection, "SET @d_ad  := (SELECT dept_id FROM department WHERE dept_code='AD')");

        exec(connection, "SET @surnames = '张,王,李,赵,陈,刘,杨,黄,周,吴,徐,孙,胡,朱,高,林,何,郭,马,罗,梁,宋,郑,谢,韩,唐,冯,于,董,萧,程,曹,袁,邓,许,傅,沈,曾,彭,吕,苏,卢,蒋,蔡,贾,丁,魏,薛,叶,阎,余,潘,杜,戴,夏,钟,汪,田,任,姜,范,方,石,姚,谭,廖,邹,熊,金,陆,郝,孔,白,崔,康,毛,邱,秦,江,史,顾,侯,邵,孟,龙,万,段,雷,钱,汤,尹,易,常,武,乔,贺,赖,龚,文'");
        exec(connection, "SET @male_names  = '伟,强,磊,涛,鹏,军,勇,杰,明,辉,宇,浩,洋,博,翔,峰,斌,凯,超,亮,飞,建,国,志,刚,宁,帅,旭,龙,威,恒,毅,远,达,哲,晟,睿,皓,轩,铭'");
        exec(connection, "SET @female_names = '娜,敏,静,丽,婷,雪,芳,娟,霞,玲,燕,萍,红,莉,艳,慧,颖,倩,洁,丹,琳,佳,瑶,薇,怡,琪,媛,妍,彤,蕾,思,萌,萱,涵,婉,菲'");
        exec(connection, "SET @origins = '山东,河南,江苏,浙江,广东,四川,湖北,安徽,湖南,河北,陕西,福建,辽宁,江西,山西,广西,云南,贵州,吉林,黑龙江,甘肃,内蒙古,新疆,海南,宁夏,青海,西藏,北京,上海,天津,重庆'");

        exec(connection, "DROP PROCEDURE IF EXISTS gen_real");

        exec(connection, """
            CREATE PROCEDURE gen_real(
                IN base_no BIGINT, IN ey INT, IN gid BIGINT,
                IN did BIGINT, IN mid BIGINT,
                IN cprefix VARCHAR(32), IN cnt INT
            )
            BEGIN
                DECLARE i INT DEFAULT 1;
                DECLARE sn INT;
                DECLARE nm VARCHAR(64);
                DECLARE g CHAR(1);
                DECLARE sv INT;
                DECLARE ori VARCHAR(32);
                WHILE i <= cnt DO
                    SET g = IF(RAND()<0.48, '2', '1');
                    SET nm = CONCAT(
                        SUBSTRING_INDEX(SUBSTRING_INDEX(@surnames, ',', 1+FLOOR(RAND()*100)), ',', -1),
                        IF(g='1',
                            SUBSTRING_INDEX(SUBSTRING_INDEX(@male_names, ',', 1+FLOOR(RAND()*40)), ',', -1),
                            SUBSTRING_INDEX(SUBSTRING_INDEX(@female_names, ',', 1+FLOOR(RAND()*36)), ',', -1)),
                        IF(RAND()<0.35, SUBSTRING_INDEX(SUBSTRING_INDEX(@male_names, ',', 1+FLOOR(RAND()*20)), ',', -1), '')
                    );
                    SET ori = SUBSTRING_INDEX(SUBSTRING_INDEX(@origins, ',', 1+FLOOR(RAND()*31)), ',', -1);
                    IF ey = 2022 THEN
                        SET sv = CASE
                            WHEN i <= cnt * 0.93 THEN 3
                            WHEN i <= cnt * 0.97 THEN 1
                            WHEN i <= cnt * 0.99 THEN 2
                            ELSE 0 END;
                    ELSE
                        SET sv = 1;
                    END IF;
                    INSERT INTO student (
                        student_no, student_name, gender, student_birth, student_age,
                        student_address, grade_id, dept_id, major_id, class_name,
                        origin_place, enroll_year, status
                    ) VALUES (
                        base_no + i, nm, IF(g='1',1,2),
                        DATE_ADD(CONCAT(ey-18,'-09-01'), INTERVAL (i*23%365)-180 DAY),
                        0,
                        CONCAT(ori, '区'),
                        gid, did, mid,
                        CONCAT(cprefix, LPAD(1+(i-1)DIV 40, 2, '0')),
                        ori, ey, sv
                    );
                    SET i = i + 1;
                END WHILE;
            END
        """);

        // CS
        exec(connection, "CALL gen_real(2022100000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件22',130)");
        exec(connection, "CALL gen_real(2022102000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能22',110)");
        exec(connection, "CALL gen_real(2022103000,2022,@g22,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据22',100)");
        exec(connection, "CALL gen_real(2023100000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件23',140)");
        exec(connection, "CALL gen_real(2023102000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能23',120)");
        exec(connection, "CALL gen_real(2023103000,2023,@g23,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据23',110)");
        exec(connection, "CALL gen_real(2024100000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件24',150)");
        exec(connection, "CALL gen_real(2024102000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能24',130)");
        exec(connection, "CALL gen_real(2024103000,2024,@g24,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据24',120)");
        exec(connection, "CALL gen_real(2025100000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS01'),'软件25',160)");
        exec(connection, "CALL gen_real(2025102000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS02'),'智能25',140)");
        exec(connection, "CALL gen_real(2025103000,2025,@g25,@d_cs,(SELECT major_id FROM major WHERE major_code='CS03'),'数据25',130)");

        // EE
        exec(connection, "CALL gen_real(2022200000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信22',130)");
        exec(connection, "CALL gen_real(2022202000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信22',90)");
        exec(connection, "CALL gen_real(2022203000,2022,@g22,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联22',80)");
        exec(connection, "CALL gen_real(2023200000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信23',140)");
        exec(connection, "CALL gen_real(2023202000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信23',100)");
        exec(connection, "CALL gen_real(2023203000,2023,@g23,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联23',90)");
        exec(connection, "CALL gen_real(2024200000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信24',150)");
        exec(connection, "CALL gen_real(2024202000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信24',110)");
        exec(connection, "CALL gen_real(2024203000,2024,@g24,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联24',100)");
        exec(connection, "CALL gen_real(2025200000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE01'),'电信25',160)");
        exec(connection, "CALL gen_real(2025202000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE02'),'通信25',120)");
        exec(connection, "CALL gen_real(2025203000,2025,@g25,@d_ee,(SELECT major_id FROM major WHERE major_code='EE03'),'物联25',110)");

        // EM
        exec(connection, "CALL gen_real(2022300000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计22',110)");
        exec(connection, "CALL gen_real(2022302000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管22',80)");
        exec(connection, "CALL gen_real(2022303000,2022,@g22,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融22',70)");
        exec(connection, "CALL gen_real(2023300000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计23',120)");
        exec(connection, "CALL gen_real(2023302000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管23',85)");
        exec(connection, "CALL gen_real(2023303000,2023,@g23,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融23',75)");
        exec(connection, "CALL gen_real(2024300000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计24',130)");
        exec(connection, "CALL gen_real(2024302000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管24',90)");
        exec(connection, "CALL gen_real(2024303000,2024,@g24,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融24',80)");
        exec(connection, "CALL gen_real(2025300000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM01'),'会计25',140)");
        exec(connection, "CALL gen_real(2025302000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM02'),'工管25',95)");
        exec(connection, "CALL gen_real(2025303000,2025,@g25,@d_em,(SELECT major_id FROM major WHERE major_code='EM03'),'金融25',85)");

        // ME
        exec(connection, "CALL gen_real(2022400000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械22',120)");
        exec(connection, "CALL gen_real(2022402000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆22',80)");
        exec(connection, "CALL gen_real(2022403000,2022,@g22,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造22',60)");
        exec(connection, "CALL gen_real(2023400000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械23',130)");
        exec(connection, "CALL gen_real(2023402000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆23',85)");
        exec(connection, "CALL gen_real(2023403000,2023,@g23,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造23',65)");
        exec(connection, "CALL gen_real(2024400000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械24',140)");
        exec(connection, "CALL gen_real(2024402000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆24',90)");
        exec(connection, "CALL gen_real(2024403000,2024,@g24,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造24',70)");
        exec(connection, "CALL gen_real(2025400000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME01'),'机械25',150)");
        exec(connection, "CALL gen_real(2025402000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME02'),'车辆25',95)");
        exec(connection, "CALL gen_real(2025403000,2025,@g25,@d_me,(SELECT major_id FROM major WHERE major_code='ME03'),'智造25',75)");

        // CE
        exec(connection, "CALL gen_real(2022500000,2022,@g22,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木22',110)");
        exec(connection, "CALL gen_real(2022502000,2022,@g22,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管22',70)");
        exec(connection, "CALL gen_real(2023500000,2023,@g23,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木23',120)");
        exec(connection, "CALL gen_real(2023502000,2023,@g23,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管23',75)");
        exec(connection, "CALL gen_real(2024500000,2024,@g24,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木24',130)");
        exec(connection, "CALL gen_real(2024502000,2024,@g24,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管24',80)");
        exec(connection, "CALL gen_real(2025500000,2025,@g25,@d_ce,(SELECT major_id FROM major WHERE major_code='CE01'),'土木25',140)");
        exec(connection, "CALL gen_real(2025502000,2025,@g25,@d_ce,(SELECT major_id FROM major WHERE major_code='CE02'),'工管25',85)");

        // Law
        exec(connection, "CALL gen_real(2022600000,2022,@g22,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学22',110)");
        exec(connection, "CALL gen_real(2022602000,2022,@g22,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产22',50)");
        exec(connection, "CALL gen_real(2023600000,2023,@g23,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学23',120)");
        exec(connection, "CALL gen_real(2023602000,2023,@g23,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产23',55)");
        exec(connection, "CALL gen_real(2024600000,2024,@g24,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学24',130)");
        exec(connection, "CALL gen_real(2024602000,2024,@g24,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产24',60)");
        exec(connection, "CALL gen_real(2025600000,2025,@g25,@d_law,(SELECT major_id FROM major WHERE major_code='LAW01'),'法学25',140)");
        exec(connection, "CALL gen_real(2025602000,2025,@g25,@d_law,(SELECT major_id FROM major WHERE major_code='LAW02'),'知产25',65)");

        // Med
        exec(connection, "CALL gen_real(2022700000,2022,@g22,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床22',80)");
        exec(connection, "CALL gen_real(2022702000,2022,@g22,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理22',50)");
        exec(connection, "CALL gen_real(2023700000,2023,@g23,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床23',85)");
        exec(connection, "CALL gen_real(2023702000,2023,@g23,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理23',55)");
        exec(connection, "CALL gen_real(2024700000,2024,@g24,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床24',90)");
        exec(connection, "CALL gen_real(2024702000,2024,@g24,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理24',60)");
        exec(connection, "CALL gen_real(2025700000,2025,@g25,@d_med,(SELECT major_id FROM major WHERE major_code='MED01'),'临床25',95)");
        exec(connection, "CALL gen_real(2025702000,2025,@g25,@d_med,(SELECT major_id FROM major WHERE major_code='MED02'),'护理25',65)");

        // CH
        exec(connection, "CALL gen_real(2022800000,2022,@g22,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工22',70)");
        exec(connection, "CALL gen_real(2022802000,2022,@g22,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化22',40)");
        exec(connection, "CALL gen_real(2023800000,2023,@g23,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工23',75)");
        exec(connection, "CALL gen_real(2023802000,2023,@g23,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化23',45)");
        exec(connection, "CALL gen_real(2024800000,2024,@g24,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工24',80)");
        exec(connection, "CALL gen_real(2024802000,2024,@g24,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化24',50)");
        exec(connection, "CALL gen_real(2025800000,2025,@g25,@d_ch,(SELECT major_id FROM major WHERE major_code='CH01'),'化工25',85)");
        exec(connection, "CALL gen_real(2025802000,2025,@g25,@d_ch,(SELECT major_id FROM major WHERE major_code='CH02'),'应化25',55)");

        // LS
        exec(connection, "CALL gen_real(2022900000,2022,@g22,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技22',60)");
        exec(connection, "CALL gen_real(2022902000,2022,@g22,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品22',40)");
        exec(connection, "CALL gen_real(2023900000,2023,@g23,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技23',65)");
        exec(connection, "CALL gen_real(2023902000,2023,@g23,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品23',45)");
        exec(connection, "CALL gen_real(2024900000,2024,@g24,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技24',70)");
        exec(connection, "CALL gen_real(2024902000,2024,@g24,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品24',50)");
        exec(connection, "CALL gen_real(2025900000,2025,@g25,@d_ls,(SELECT major_id FROM major WHERE major_code='LS01'),'生技25',75)");
        exec(connection, "CALL gen_real(2025902000,2025,@g25,@d_ls,(SELECT major_id FROM major WHERE major_code='LS02'),'食品25',55)");

        // FL
        exec(connection, "CALL gen_real(2023000000,2022,@g22,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语22',60)");
        exec(connection, "CALL gen_real(2023002000,2022,@g22,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语22',40)");
        exec(connection, "CALL gen_real(2024000000,2023,@g23,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语23',65)");
        exec(connection, "CALL gen_real(2024002000,2023,@g23,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语23',45)");
        exec(connection, "CALL gen_real(2025000000,2024,@g24,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语24',70)");
        exec(connection, "CALL gen_real(2025002000,2024,@g24,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语24',50)");
        exec(connection, "CALL gen_real(2026000000,2025,@g25,@d_fl,(SELECT major_id FROM major WHERE major_code='FL01'),'英语25',75)");
        exec(connection, "CALL gen_real(2026002000,2025,@g25,@d_fl,(SELECT major_id FROM major WHERE major_code='FL02'),'日语25',55)");

        // EDU
        exec(connection, "CALL gen_real(2026100000,2022,@g22,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育22',60)");
        exec(connection, "CALL gen_real(2026102000,2022,@g22,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前22',40)");
        exec(connection, "CALL gen_real(2027100000,2023,@g23,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育23',65)");
        exec(connection, "CALL gen_real(2027102000,2023,@g23,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前23',45)");
        exec(connection, "CALL gen_real(2028100000,2024,@g24,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育24',70)");
        exec(connection, "CALL gen_real(2028102000,2024,@g24,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前24',50)");
        exec(connection, "CALL gen_real(2029100000,2025,@g25,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU01'),'教育25',75)");
        exec(connection, "CALL gen_real(2029102000,2025,@g25,@d_edu,(SELECT major_id FROM major WHERE major_code='EDU02'),'学前25',55)");

        // JR
        exec(connection, "CALL gen_real(2030100000,2022,@g22,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻22',55)");
        exec(connection, "CALL gen_real(2030102000,2022,@g22,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新22',35)");
        exec(connection, "CALL gen_real(2031100000,2023,@g23,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻23',60)");
        exec(connection, "CALL gen_real(2031102000,2023,@g23,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新23',40)");
        exec(connection, "CALL gen_real(2032100000,2024,@g24,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻24',65)");
        exec(connection, "CALL gen_real(2032102000,2024,@g24,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新24',45)");
        exec(connection, "CALL gen_real(2033100000,2025,@g25,@d_jr,(SELECT major_id FROM major WHERE major_code='JR01'),'新闻25',70)");
        exec(connection, "CALL gen_real(2033102000,2025,@g25,@d_jr,(SELECT major_id FROM major WHERE major_code='JR02'),'网新25',50)");

        // MS
        exec(connection, "CALL gen_real(2034100000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学22',40)");
        exec(connection, "CALL gen_real(2034102000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计22',30)");
        exec(connection, "CALL gen_real(2034103000,2022,@g22,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计22',25)");
        exec(connection, "CALL gen_real(2035100000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学23',45)");
        exec(connection, "CALL gen_real(2035102000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计23',35)");
        exec(connection, "CALL gen_real(2035103000,2023,@g23,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计23',30)");
        exec(connection, "CALL gen_real(2036100000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学24',50)");
        exec(connection, "CALL gen_real(2036102000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计24',40)");
        exec(connection, "CALL gen_real(2036103000,2024,@g24,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计24',35)");
        exec(connection, "CALL gen_real(2037100000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS01'),'数学25',55)");
        exec(connection, "CALL gen_real(2037102000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS02'),'统计25',45)");
        exec(connection, "CALL gen_real(2037103000,2025,@g25,@d_ms,(SELECT major_id FROM major WHERE major_code='MS03'),'数计25',40)");

        // AD
        exec(connection, "CALL gen_real(2038100000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传22',40)");
        exec(connection, "CALL gen_real(2038102000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设22',30)");
        exec(connection, "CALL gen_real(2038103000,2022,@g22,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒22',25)");
        exec(connection, "CALL gen_real(2039100000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传23',45)");
        exec(connection, "CALL gen_real(2039102000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设23',35)");
        exec(connection, "CALL gen_real(2039103000,2023,@g23,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒23',30)");
        exec(connection, "CALL gen_real(2040100000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传24',50)");
        exec(connection, "CALL gen_real(2040102000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设24',40)");
        exec(connection, "CALL gen_real(2040103000,2024,@g24,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒24',35)");
        exec(connection, "CALL gen_real(2041100000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD01'),'视传25',55)");
        exec(connection, "CALL gen_real(2041102000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD02'),'环设25',45)");
        exec(connection, "CALL gen_real(2041103000,2025,@g25,@d_ad,(SELECT major_id FROM major WHERE major_code='AD03'),'数媒25',40)");

        // status updates
        exec(connection, "UPDATE student SET status = 2 WHERE status = 1 AND enroll_year >= 2023 ORDER BY RAND() LIMIT 10");
        exec(connection, "UPDATE student SET status = 0 WHERE status = 1 AND enroll_year >= 2023 ORDER BY RAND() LIMIT 8");
        exec(connection, "UPDATE student SET student_age = TIMESTAMPDIFF(YEAR, student_birth, '2026-07-17')");
        exec(connection, "UPDATE student SET student_birth = DATE_SUB(student_birth, INTERVAL 1 YEAR), student_age = student_age + 1 WHERE RAND() < 0.03");
        exec(connection, "UPDATE student SET student_age = TIMESTAMPDIFF(YEAR, student_birth, '2026-07-17')");

        // enrollment
        exec(connection, """
            INSERT INTO enrollment (major_id, year, plan_count, actual_count, report_rate)
            SELECT m.major_id, y.year,
                   CASE WHEN m.major_code IN ('CS01','CS02','CS03','EE01','EE02','EE03','EM01','EM03')
                        THEN 100+FLOOR(RAND()*100)
                        WHEN m.major_code IN ('ME01','ME02','CE01','MED01')
                        THEN 70+FLOOR(RAND()*80)
                        ELSE 40+FLOOR(RAND()*60) END AS plan,
                   0, NULL
            FROM major m CROSS JOIN (SELECT 2022 AS year UNION ALL SELECT 2023 UNION ALL SELECT 2024) y
        """);

        exec(connection, """
            UPDATE enrollment e
            JOIN (SELECT major_id, enroll_year AS year, COUNT(*) AS cnt FROM student WHERE status=1 GROUP BY major_id, enroll_year) s
                ON s.major_id=e.major_id AND s.year=e.year
            SET e.actual_count=s.cnt, e.report_rate=ROUND(s.cnt/e.plan_count*100,2)
            WHERE e.plan_count>0
        """);

        exec(connection, "DROP PROCEDURE IF EXISTS gen_real");

        exec(connection, "SELECT '========== 最终统计 ==========' AS ''");
        exec(connection, """
            SELECT '年级', enroll_year, '总数', COUNT(*),
                   '在读', SUM(status=1), '毕业', SUM(status=3),
                   '休学', SUM(status=2), '退学', SUM(status=0)
            FROM student GROUP BY enroll_year ORDER BY enroll_year
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
