-- ============================================
-- 真实时间线修正（2026-07-17）
-- 2026 级未报到 / 2022 级已毕业 / 延毕数据 / 招生计划对齐
-- ============================================
USE school_spring;

-- 1. 删除所有 2026 级学生（8 月底才报到）
DELETE FROM student WHERE enroll_year = 2026;

-- 2. 2022 级补充（刚毕业，状态=3）+ 2023-2025 不变
-- 先确保有 2022 级年级
INSERT IGNORE INTO grade (grade_name) VALUES ('2022级');
SET @g2022 := (SELECT grade_id FROM grade WHERE grade_name = '2022级' LIMIT 1);

DROP PROCEDURE IF EXISTS gen_grad;
DELIMITER //
CREATE PROCEDURE gen_grad(
    IN base_no BIGINT, IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT, IN oris TEXT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total_ori INT;
    DECLARE g CHAR(1);
    DECLARE st INT;
    SET total_ori = CHAR_LENGTH(oris) - CHAR_LENGTH(REPLACE(oris, ',', '')) + 1;
    WHILE i <= cnt DO
        SET g = IF(i%4=0,'2','1');
        -- 90% 正常毕业, 7% 延毕(状态仍为在读/休学), 3% 肄业
        SET st = CASE WHEN i <= cnt*0.9 THEN 3
                      WHEN i <= cnt*0.97 THEN IF(RAND()<0.6, 2, 1)
                      ELSE 0 END;
        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i,
            CONCAT(ELT(1+FLOOR(RAND()*8),'张','王','李','赵','陈','刘','杨','黄'),
                   ELT(1+FLOOR(RAND()*6),'伟','娜','强','洋','雪','敏','鹏','婷','昊','芳','磊'),
                   ELT(1+FLOOR(RAND()*4),'轩','涵','铭','瑶','辰','然','宇','桐','哲')),
            IF(g='1',1,2),
            DATE_ADD('2004-06-15', INTERVAL (i*19 % 365) DAY),
            22,
            CONCAT(SUBSTRING_INDEX(SUBSTRING_INDEX(oris,',',1+((i-1)%total_ori)),',',-1),'区'),
            @g2022, did, mid,
            CONCAT(cprefix, LPAD(1+(i-1) DIV 40, 2, '0')),
            SUBSTRING_INDEX(SUBSTRING_INDEX(oris,',',1+((i-1)%total_ori)),',',-1),
            2022, st
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- 为每个院系生成 2022 级毕业生（规模与之前类似）
CALL gen_grad(2022100000,@d_cs,@m_cs01,'软件22',120,'济南,青岛,烟台,郑州,洛阳,石家庄,太原,西安,武汉,成都,南京,合肥');
CALL gen_grad(2022100200,@d_cs,@m_cs02,'智能22', 80,'北京,上海,深圳,广州,杭州,南京,武汉,成都,重庆,天津');
CALL gen_grad(2022100300,@d_cs,@m_cs03,'数据22', 60,'长沙,南昌,福州,南宁,昆明,贵阳,兰州,西宁,银川');
CALL gen_grad(2022100500,@d_ee,@m_ee01,'电信22', 90,'成都,绵阳,德阳,南充,宜宾,泸州,自贡,重庆,贵阳');
CALL gen_grad(2022100600,@d_ee,@m_ee02,'通信22', 60,'西安,咸阳,宝鸡,渭南,汉中,延安,兰州,天水');
CALL gen_grad(2022100700,@d_ee,@m_ee03,'物联22', 50,'深圳,广州,杭州,南京,武汉,成都,北京,上海');
CALL gen_grad(2022100800,@d_em,@m_em01,'会计22',100,'广州,深圳,东莞,佛山,珠海,中山,惠州,江门,肇庆');
CALL gen_grad(2022100900,@d_em,@m_em02,'工管22', 70,'杭州,宁波,温州,嘉兴,绍兴,金华,台州,湖州');
CALL gen_grad(2022101000,@d_em,@m_em03,'金融22', 55,'上海,北京,深圳,广州,杭州,南京,苏州,成都');
CALL gen_grad(2022101100,@d_me,@m_me01,'机械22',100,'沈阳,大连,鞍山,长春,吉林,哈尔滨,大庆,齐齐哈尔');
CALL gen_grad(2022101200,@d_me,@m_me02,'车辆22', 65,'济南,青岛,淄博,烟台,潍坊,济宁,泰安,威海');
CALL gen_grad(2022101300,@d_me,@m_me03,'智造22', 45,'武汉,宜昌,襄阳,荆州,黄冈,十堰');
CALL gen_grad(2022101400,@d_ce,@m_ce01,'土木22', 70,'重庆,成都,贵阳,昆明,南宁,西安,武汉,长沙');
CALL gen_grad(2022101500,@d_ce,@m_ce02,'工管22', 45,'南京,苏州,无锡,合肥,南昌,杭州,宁波');
CALL gen_grad(2022101600,@d_law,@m_law01,'法学22', 55,'济南,青岛,郑州,武汉,长沙,成都,西安');
CALL gen_grad(2022101700,@d_law,@m_law02,'知产22', 35,'北京,上海,广州,深圳,南京,杭州,天津');
CALL gen_grad(2022101800,@d_ch,@m_ch01,'化工22', 50,'南京,苏州,无锡,合肥,武汉,长沙,南昌');
CALL gen_grad(2022101900,@d_ch,@m_ch02,'应化22', 35,'济南,青岛,杭州,宁波,厦门,福州,广州');
CALL gen_grad(2022102000,@d_med,@m_med01,'临床22', 55,'成都,重庆,西安,兰州,昆明,贵阳,南宁');
CALL gen_grad(2022102100,@d_med,@m_med02,'护理22', 45,'郑州,武汉,长沙,南昌,合肥,南京,上海');
CALL gen_grad(2022102200,@d_ls,@m_ls01,'生技22', 45,'济南,青岛,烟台,武汉,长沙,合肥,南昌');
CALL gen_grad(2022102300,@d_ls,@m_ls02,'食品22', 35,'成都,重庆,昆明,贵阳,南宁,广州,福州');
CALL gen_grad(2022102400,@d_fl,@m_fl01,'英语22', 40,'大连,青岛,厦门,苏州,无锡,宁波,福州');
CALL gen_grad(2022102500,@d_fl,@m_fl02,'日语22', 30,'沈阳,长春,哈尔滨,延边,天津,济南,西安');
CALL gen_grad(2022102600,@d_edu,@m_edu01,'教育22', 40,'郑州,济南,武汉,长沙,成都,西安,合肥');
CALL gen_grad(2022102700,@d_edu,@m_edu02,'学前22', 35,'南京,杭州,福州,广州,南宁,昆明,贵阳');
CALL gen_grad(2022102800,@d_jr,@m_jr01,'新闻22', 35,'北京,上海,广州,深圳,杭州,南京,武汉');
CALL gen_grad(2022102900,@d_jr,@m_jr02,'网新22', 30,'成都,重庆,西安,长沙,郑州,济南,合肥');
CALL gen_grad(2022103000,@d_ms,@m_ms01,'数学22', 35,'济南,青岛,郑州,武汉,长沙,合肥,南昌');
CALL gen_grad(2022103100,@d_ms,@m_ms02,'统计22', 25,'南京,杭州,上海,苏州,无锡,宁波,厦门');
CALL gen_grad(2022103200,@d_ms,@m_ms03,'数计22', 20,'成都,重庆,西安,昆明,贵阳,南宁,广州');
CALL gen_grad(2022103300,@d_ad,@m_ad01,'视传22', 30,'北京,上海,杭州,成都,重庆,武汉,南京');
CALL gen_grad(2022103400,@d_ad,@m_ad02,'环设22', 25,'苏州,无锡,宁波,厦门,青岛,大连,长沙');
CALL gen_grad(2022103500,@d_ad,@m_ad03,'数媒22', 20,'天津,沈阳,济南,合肥,南昌,福州,昆明');

-- 3. 补充少量 2019-2021 级延毕学生（读了 5-7 年还没毕业）
DROP PROCEDURE IF EXISTS gen_delay;
DELIMITER //
CREATE PROCEDURE gen_delay(
    IN base_no BIGINT, IN ey INT, IN did BIGINT, IN mid BIGINT,
    IN cprefix VARCHAR(32), IN cnt INT
)
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= cnt DO
        INSERT IGNORE INTO student (
            student_no, student_name, gender, student_birth, student_age,
            student_address, grade_id, dept_id, major_id, class_name,
            origin_place, enroll_year, status
        ) VALUES (
            base_no + i,
            CONCAT(ELT(1+FLOOR(RAND()*5),'张','王','李','赵','陈'),'延毕',i),
            IF(i%3=0,2,1),
            DATE_ADD(CONCAT(ey,'-09-01'), INTERVAL (i*31 % 365) DAY),
            2026 - ey + 18,
            '校内',
            NULL, did, mid,
            CONCAT(cprefix, '01'), '本省', ey,
            IF(RAND()<0.6, 2, 1)
        );
        SET i = i + 1;
    END WHILE;
END//
DELIMITER ;

-- 每个大院留 15-25 个延毕生
CALL gen_delay(2019100000,2019,@d_cs,@m_cs01,'软件19延',25);
CALL gen_delay(2019100100,2019,@d_me,@m_me01,'机械19延',20);
CALL gen_delay(2019100200,2019,@d_em,@m_em01,'会计19延',15);
CALL gen_delay(2019100300,2019,@d_ee,@m_ee01,'电信19延',15);
CALL gen_delay(2020100000,2020,@d_cs,@m_cs01,'软件20延',20);
CALL gen_delay(2020100100,2020,@d_me,@m_me01,'机械20延',15);
CALL gen_delay(2021100000,2021,@d_cs,@m_cs01,'软件21延',15);

DROP PROCEDURE IF EXISTS gen_grad;
DROP PROCEDURE IF EXISTS gen_delay;

-- 4. 更新招生计划：2026 级报到数为 0
UPDATE enrollment SET actual_count = 0, report_rate = 0 WHERE year = 2026;

-- 5. 根据真实 student 表更新其他年度报到数
UPDATE enrollment e
JOIN (
    SELECT major_id, enroll_year AS year, COUNT(*) AS cnt
    FROM student WHERE status = 1
    GROUP BY major_id, enroll_year
) s ON s.major_id = e.major_id AND s.year = e.year
SET e.actual_count = s.cnt,
    e.report_rate = ROUND(s.cnt / e.plan_count * 100, 2)
WHERE e.plan_count > 0 AND e.year != 2026;

SELECT '---------- 修正后统计 ----------' AS '';
SELECT '2026级在读(应为0): ', COUNT(*) FROM student WHERE enroll_year = 2026 AND status = 1;
SELECT '2022级毕业: ', COUNT(*) FROM student WHERE enroll_year = 2022 AND status = 3;
SELECT '延毕/休学: ', COUNT(*) FROM student WHERE enroll_year <= 2021 AND status IN (1,2);
SELECT '在读学生(2023-2025): ', COUNT(*) FROM student WHERE enroll_year BETWEEN 2023 AND 2025 AND status = 1;
SELECT '学生总数: ', COUNT(*) FROM student;
