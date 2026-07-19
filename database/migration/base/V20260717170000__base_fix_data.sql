-- 修复：延毕生名字 + 院系专业归属
USE school_spring;

-- 1. 给所有缺院系/专业的学生随机分配（按 enroll_year 匹配合理院系）
UPDATE student s
JOIN (
    SELECT s2.student_id,
           CASE WHEN s2.enroll_year <= 2020 THEN
               ELT(1+FLOOR(RAND()*6),(SELECT dept_id FROM department WHERE dept_code='CS'),
                                      (SELECT dept_id FROM department WHERE dept_code='ME'),
                                      (SELECT dept_id FROM department WHERE dept_code='EM'),
                                      (SELECT dept_id FROM department WHERE dept_code='EE'),
                                      (SELECT dept_id FROM department WHERE dept_code='CE'),
                                      (SELECT dept_id FROM department WHERE dept_code='CH'))
           ELSE
               ELT(1+FLOOR(RAND()*6),(SELECT dept_id FROM department WHERE dept_code='CS'),
                                      (SELECT dept_id FROM department WHERE dept_code='EE'),
                                      (SELECT dept_id FROM department WHERE dept_code='EM'),
                                      (SELECT dept_id FROM department WHERE dept_code='ME'),
                                      (SELECT dept_id FROM department WHERE dept_code='MED'),
                                      (SELECT dept_id FROM department WHERE dept_code='LS'))
           END AS new_dept
    FROM student s2
    WHERE s2.dept_id IS NULL
) fix ON fix.student_id = s.student_id
SET s.dept_id = fix.new_dept
WHERE s.dept_id IS NULL;

-- 2. 根据院系分配专业
UPDATE student s
JOIN (
    SELECT student_id, dept_id,
           (SELECT major_id FROM major WHERE dept_id = s2.dept_id ORDER BY RAND() LIMIT 1) AS new_major
    FROM student s2 WHERE s2.major_id IS NULL
) fix ON fix.student_id = s.student_id
SET s.major_id = fix.new_major
WHERE s.major_id IS NULL;

-- 3. 修复假名字：用正常姓名替换
-- 姓氏 + 常见名
UPDATE student SET student_name = CONCAT(
    ELT(1+FLOOR(RAND()*10), '张','王','李','赵','陈','刘','杨','黄','周','吴'),
    ELT(1+FLOOR(RAND()*10), '子轩','雨涵','浩然','思琪','天宇','梦瑶','志远','若曦','博文','晓彤'),
    ELT(1+FLOOR(RAND()*5), '','怡','铭','辰','然','桐','哲','琳','博','萱','皓','欣','泽','逸')
) WHERE student_name LIKE '%延毕%' OR student_name LIKE '%退伍%';

SELECT '修复后检查' AS '';
SELECT '缺院系: ', COUNT(*) FROM student WHERE dept_id IS NULL;
SELECT '缺专业: ', COUNT(*) FROM student WHERE major_id IS NULL;
SELECT '假名字: ', COUNT(*) FROM student WHERE student_name LIKE '%延毕%' OR student_name LIKE '%退伍%';
