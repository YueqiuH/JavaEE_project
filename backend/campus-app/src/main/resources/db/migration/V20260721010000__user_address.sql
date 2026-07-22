-- 教职工 user 表新增家庭住址字段
ALTER TABLE `user` ADD COLUMN `address` VARCHAR(128) NULL COMMENT '家庭住址' AFTER `email`;

-- 为已有教职工生成较真实的家庭住址
UPDATE `user` SET `address` = CASE
    WHEN `user_id` % 6 = 0 THEN '四川省成都市郫都区犀安路999号交大教师公寓12栋'
    WHEN `user_id` % 6 = 1 THEN '四川省成都市金牛区二环路北一段111号教师苑7栋'
    WHEN `user_id` % 6 = 2 THEN '四川省成都市高新区天府大道中段688号大源社区'
    WHEN `user_id` % 6 = 3 THEN '四川省成都市成华区建设北路二段4号电子科大东院'
    WHEN `user_id` % 6 = 4 THEN '四川省成都市武侯区一环路南一段24号川大竹林村'
    WHEN `user_id` % 6 = 5 THEN '四川省成都市锦江区静安路5号川师狮子山校区家属区'
END
WHERE `user_type` IN (2, 3) AND `address` IS NULL;

-- 为学生表补充家庭住址（按生源地生成较真实的市级地址）
UPDATE `student` SET `student_address` = CASE
    WHEN `origin_place` LIKE '%四川%' THEN CONCAT('四川省成都市', ELT(1 + `student_id` % 5, '武侯区科华北路', '金牛区交大路', '成华区建设路', '锦江区春熙路', '高新区天府大道'), `student_id` % 200 + 1, '号')
    WHEN `origin_place` LIKE '%重庆%' THEN CONCAT('重庆市', ELT(1 + `student_id` % 3, '渝北区龙山街道', '沙坪坝区大学城', '南岸区学府大道'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%北京%' THEN CONCAT('北京市', ELT(1 + `student_id` % 3, '海淀区学院路', '朝阳区望京街道', '丰台区方庄'), `student_id` % 100 + 1, '号')
    WHEN `origin_place` LIKE '%上海%' THEN CONCAT('上海市', ELT(1 + `student_id` % 3, '杨浦区五角场街道', '闵行区东川路', '松江区文汇路'), `student_id` % 100 + 1, '号')
    WHEN `origin_place` LIKE '%广东%' THEN CONCAT('广东省广州市', ELT(1 + `student_id` % 3, '天河区五山路', '番禺区大学城', '海珠区新港西路'), `student_id` % 200 + 1, '号')
    WHEN `origin_place` LIKE '%浙江%' THEN CONCAT('浙江省杭州市', ELT(1 + `student_id` % 2, '西湖区文三路', '余杭区仓前街道'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%江苏%' THEN CONCAT('江苏省南京市', ELT(1 + `student_id` % 3, '鼓楼区汉口路', '江宁区将军大道', '栖霞区仙林大道'), `student_id` % 200 + 1, '号')
    WHEN `origin_place` LIKE '%湖北%' THEN CONCAT('湖北省武汉市', ELT(1 + `student_id` % 2, '洪山区珞喻路', '武昌区珞珈山路'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%湖南%' THEN CONCAT('湖南省长沙市', ELT(1 + `student_id` % 2, '岳麓区麓山南路', '天心区韶山南路'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%山东%' THEN CONCAT('山东省济南市', ELT(1 + `student_id` % 2, '历下区文化东路', '长清区大学路'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%河南%' THEN CONCAT('河南省郑州市', ELT(1 + `student_id` % 2, '高新区科学大道', '郑东新区龙子湖'), `student_id` % 150 + 1, '号')
    WHEN `origin_place` LIKE '%陕西%' THEN CONCAT('陕西省西安市', ELT(1 + `student_id` % 2, '碑林区友谊西路', '长安区西长安街'), `student_id` % 150 + 1, '号')
    ELSE CONCAT('四川省成都市郫都区犀安路', `student_id` % 999 + 1, '号')
END
WHERE `student_address` IS NULL;
