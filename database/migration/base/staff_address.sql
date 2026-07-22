SET @d_cs  = (SELECT dept_id FROM department WHERE dept_code = 'CS');
SET @d_ee  = (SELECT dept_id FROM department WHERE dept_code = 'EE');
SET @d_em  = (SELECT dept_id FROM department WHERE dept_code = 'EM');
SET @d_me  = (SELECT dept_id FROM department WHERE dept_code = 'ME');
SET @d_med = (SELECT dept_id FROM department WHERE dept_code = 'MED');
SET @d_ce  = (SELECT dept_id FROM department WHERE dept_code = 'CE');
SET @d_law = (SELECT dept_id FROM department WHERE dept_code = 'LAW');
SET @d_ch  = (SELECT dept_id FROM department WHERE dept_code = 'CH');
SET @d_ls  = (SELECT dept_id FROM department WHERE dept_code = 'LS');
SET @d_edu = (SELECT dept_id FROM department WHERE dept_code = 'EDU');
SET @d_jr  = (SELECT dept_id FROM department WHERE dept_code = 'JR');
SET @d_fl  = (SELECT dept_id FROM department WHERE dept_code = 'FL');
SET @d_ms  = (SELECT dept_id FROM department WHERE dept_code = 'MS');
SET @d_ad  = (SELECT dept_id FROM department WHERE dept_code = 'AD');

UPDATE `user` SET address = CASE
  WHEN dept_id = @d_cs  THEN ELT(1+FLOOR(RAND()*6), '北京市海淀区中关村南大街5号院3号楼', '深圳市南山区科技园南路16号华润城', '杭州市西湖区文三路478号华星时代广场', '成都市武侯区天府大道中段688号大源国际', '武汉市洪山区珞喻路1037号华科喻园', '南京市鼓楼区汉口路22号南大鼓楼校区')
  WHEN dept_id = @d_ee  THEN ELT(1+FLOOR(RAND()*5), '成都市高新区西源大道2006号电子科大清水河', '重庆市沙坪坝区大学城中路55号富力城', '西安市雁塔区太白南路2号西电社区', '武汉市武昌区珞珈山路16号武大工学部', '长沙市岳麓区麓山南路932号中南大学校本部')
  WHEN dept_id = @d_em  THEN ELT(1+FLOOR(RAND()*5), '上海市浦东新区世纪大道100号环球金融中心', '北京市朝阳区建国路88号SOHO现代城', '深圳市福田区深南大道7088号招商银行大厦', '广州市天河区体育西路111号维多利广场', '杭州市上城区钱江路399号华润大厦')
  WHEN dept_id = @d_me  THEN ELT(1+FLOOR(RAND()*5), '沈阳市铁西区建设东路158号远大花园', '长春市南关区人民大街526号吉大南岭校区', '哈尔滨市南岗区西大直街92号哈工大科技园', '武汉市洪山区雄楚大道688号康桥小区', '重庆市渝北区空港大道99号青麓雅园')
  WHEN dept_id = @d_med THEN ELT(1+FLOOR(RAND()*4), '成都市锦江区红星路二段82号华西坝', '广州市越秀区中山二路74号中大北校区', '武汉市江汉区解放大道1277号协和家属区', '南京市秦淮区汉中路140号南医大五台校区')
  WHEN dept_id = @d_ce  THEN ELT(1+FLOOR(RAND()*4), '成都市金牛区交大路144号交大九里堤', '重庆市南岸区学府大道66号交大菁园', '西安市碑林区雁塔路13号建大雁塔校区', '长沙市天心区韶山南路498号林大福邸')
  WHEN dept_id = @d_law THEN ELT(1+FLOOR(RAND()*4), '北京市海淀区西土城路25号法大家属院', '上海市长宁区万航渡路1575号华政长宁校区', '武汉市洪山区南湖大道182号中南财经首义', '重庆市渝北区宝圣大道301号西政渝北校区')
  WHEN dept_id = @d_ch  THEN ELT(1+FLOOR(RAND()*3), '南京市鼓楼区汉口路22号南大鼓楼校区', '天津市南开区卫津路94号南大西南村', '广州市天河区五山路483号华农大五山')
  WHEN dept_id = @d_ls  THEN ELT(1+FLOOR(RAND()*3), '武汉市武昌区八一路299号武大珞珈山', '杭州市西湖区余杭塘路866号浙大紫金港', '青岛市市南区鱼山路5号海大鱼山校区')
  WHEN dept_id = @d_edu THEN ELT(1+FLOOR(RAND()*3), '北京市海淀区新街口外大街19号北师大院内', '上海市普陀区中山北路3663号华师大一村', '南京市鼓楼区宁海路122号南师随园校区')
  WHEN dept_id = @d_jr  THEN ELT(1+FLOOR(RAND()*3), '北京市朝阳区定福庄东街1号中传校内', '上海市闵行区东川路800号交大闵行校区', '武汉市洪山区珞南街街道口珞珈山大厦')
  WHEN dept_id = @d_fl  THEN ELT(1+FLOOR(RAND()*3), '北京市海淀区西三环北路2号北外东院', '上海市虹口区大连西路550号上外虹口校区', '西安市长安区文苑南路6号西外长安校区')
  WHEN dept_id = @d_ms  THEN ELT(1+FLOOR(RAND()*3), '济南市历城区山大南路27号山大中心校区', '郑州市高新区科学大道100号郑大新校区', '合肥市蜀山区九龙路111号安大磬苑校区')
  WHEN dept_id = @d_ad  THEN ELT(1+FLOOR(RAND()*3), '北京市朝阳区花家地南街8号央美花家地', '杭州市西湖区转塘街道象山352号国美象山', '南京市鼓楼区北京西路74号南艺黄瓜园')
  ELSE ELT(1+FLOOR(RAND()*5), '北京市海淀区学院路15号语言大学家属区', '上海市杨浦区邯郸路220号复旦邯郸校区', '广州市番禺区大学城外环西路100号广大生活区', '武汉市洪山区鲁磨路388号地大东区', '成都市成华区二仙桥东三路1号理工东苑')
END
WHERE username BETWEEN '200011' AND '200368';

SELECT COUNT(*) AS updated FROM `user` WHERE username BETWEEN '200011' AND '200368' AND address IS NOT NULL;
