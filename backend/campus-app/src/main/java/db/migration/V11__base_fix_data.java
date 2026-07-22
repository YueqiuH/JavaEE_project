package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import java.sql.Connection;
import java.sql.Statement;

public class V11__base_fix_data extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        exec(connection, """
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
            WHERE s.dept_id IS NULL
        """);

        exec(connection, """
            UPDATE student s
            JOIN (
                SELECT student_id, dept_id,
                       (SELECT major_id FROM major WHERE dept_id = s2.dept_id ORDER BY RAND() LIMIT 1) AS new_major
                FROM student s2 WHERE s2.major_id IS NULL
            ) fix ON fix.student_id = s.student_id
            SET s.major_id = fix.new_major
            WHERE s.major_id IS NULL
        """);

        exec(connection, """
            UPDATE student SET student_name = CONCAT(
                ELT(1+FLOOR(RAND()*10), '张','王','李','赵','陈','刘','杨','黄','周','吴'),
                ELT(1+FLOOR(RAND()*10), '子轩','雨涵','浩然','思琪','天宇','梦瑶','志远','若曦','博文','晓彤'),
                ELT(1+FLOOR(RAND()*5), '','怡','铭','辰','然','桐','哲','琳','博','萱','皓','欣','泽','逸')
            ) WHERE student_name LIKE '%延毕%' OR student_name LIKE '%退伍%'
        """);

        exec(connection, "SELECT '修复后检查' AS ''");
        exec(connection, "SELECT '缺院系: ', COUNT(*) FROM student WHERE dept_id IS NULL");
        exec(connection, "SELECT '缺专业: ', COUNT(*) FROM student WHERE major_id IS NULL");
        exec(connection, "SELECT '假名字: ', COUNT(*) FROM student WHERE student_name LIKE '%延毕%' OR student_name LIKE '%退伍%'");
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
