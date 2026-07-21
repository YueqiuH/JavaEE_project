package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V5__student_affairs_role_workflows extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        addColumn(connection, "student", "counselor_id", "BIGINT DEFAULT NULL");
        addColumn(connection, "scholarship", "counselor_id", "BIGINT DEFAULT NULL");
        addColumn(connection, "scholarship", "counselor_opinion", "VARCHAR(256) DEFAULT NULL");
        addColumn(connection, "scholarship", "counselor_reviewed_at", "DATETIME DEFAULT NULL");
        addColumn(connection, "scholarship", "academic_reviewer_id", "BIGINT DEFAULT NULL");
        addColumn(connection, "scholarship", "academic_opinion", "VARCHAR(256) DEFAULT NULL");
        addColumn(connection, "scholarship", "academic_reviewed_at", "DATETIME DEFAULT NULL");
        addIndex(connection, "student", "idx_student_counselor", "counselor_id");
        addIndex(connection, "scholarship", "idx_scholarship_counselor_status", "counselor_id", "status");

        execute(connection, """
                INSERT INTO `permission` (`permission_code`, `permission_name`, `status`) VALUES
                    ('evaluation:result:read-counseled', '查看所带学生提交的评教结果', 1),
                    ('evaluation:result:read-all', '查看全校评教结果', 1),
                    ('competition:oversight:read', '查看全部竞赛及参赛队伍', 1)
                ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`), `status` = 1
                """);
        execute(connection, "UPDATE `role` SET `role_name` = '教务处' WHERE `role_code` = 'ADMIN'");

        execute(connection, """
                UPDATE `user` SET
                    `user_type` = CASE `username` WHEN '700001' THEN 3 WHEN '800001' THEN 2 ELSE `user_type` END,
                    `real_name` = CASE `username`
                        WHEN '700001' THEN '演示辅导员'
                        WHEN '800001' THEN '演示教师'
                        WHEN 'admin' THEN '教务处' ELSE `real_name` END,
                    `status` = 1
                WHERE `username` IN ('700001', '800001', 'admin')
                """);
        execute(connection, """
                DELETE ur FROM `user_role` ur
                JOIN `user` u ON u.user_id = ur.user_id
                WHERE u.username IN ('700001', '800001', 'admin')
                """);
        execute(connection, """
                INSERT INTO `user_role` (`user_id`, `role_id`)
                SELECT u.user_id, r.role_id FROM `user` u JOIN `role` r ON
                    (u.username = '700001' AND r.role_code = 'COUNSELOR') OR
                    (u.username = '800001' AND r.role_code = 'TEACHER') OR
                    (u.username = 'admin' AND r.role_code = 'ADMIN')
                """);

        execute(connection, """
                UPDATE `student` s JOIN `user` counselor ON counselor.username = '700001'
                SET s.counselor_id = counselor.user_id WHERE s.counselor_id IS NULL
                """);
        execute(connection, """
                UPDATE `competition` c
                JOIN `user` old_owner ON old_owner.user_id = c.publisher_id AND old_owner.username = '700001'
                JOIN `user` new_owner ON new_owner.username = '800001'
                SET c.publisher_id = new_owner.user_id
                """);
        execute(connection, """
                UPDATE `lab` l
                JOIN `user` old_owner ON old_owner.user_id = l.manager_id AND old_owner.username = '700001'
                JOIN `user` new_owner ON new_owner.username = '800001'
                SET l.manager_id = new_owner.user_id
                """);

        execute(connection, """
                UPDATE `scholarship`
                SET academic_reviewer_id = COALESCE(academic_reviewer_id, reviewer_id),
                    academic_opinion = COALESCE(academic_opinion, review_opinion),
                    academic_reviewed_at = COALESCE(academic_reviewed_at, reviewed_at)
                WHERE status IN (3, 4, 6)
                """);

        execute(connection, """
                DELETE rp FROM `role_permission` rp
                JOIN `role` r ON r.role_id = rp.role_id
                JOIN `permission` p ON p.permission_id = rp.permission_id
                WHERE r.role_code = 'TEACHER'
                  AND p.permission_code IN ('status:review:read', 'status:review:submit',
                                            'scholarship:review:read', 'scholarship:review:submit',
                                            'scholarship:result:generate')
                """);
        grant(connection, "COUNSELOR", "status:review:read", "status:review:submit",
                "scholarship:review:read", "scholarship:review:submit",
                "evaluation:result:read-counseled", "competition:read", "competition:oversight:read",
                "lab:read", "lab:manage-self", "lab:resource:manage-self", "lab:slot:manage-self",
                "lab:booking:read-managed", "lab:booking:complete-managed");
        grant(connection, "TEACHER", "evaluation:result:read-self", "competition:read",
                "competition:publish", "competition:manage-self", "competition:review:read-self",
                "competition:review:submit-self", "lab:read", "lab:manage-self",
                "lab:resource:manage-self", "lab:slot:manage-self", "lab:booking:read-managed",
                "lab:booking:complete-managed");
        execute(connection, """
                INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
                SELECT r.role_id, p.permission_id FROM `role` r CROSS JOIN `permission` p
                WHERE r.role_code = 'ADMIN'
                """);
    }

    private static void addColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (!columnExists(connection, table, column)) {
            execute(connection, "ALTER TABLE `" + table + "` ADD COLUMN `" + column + "` " + definition);
        }
    }

    private static void addIndex(Connection connection, String table, String index, String... columns)
            throws SQLException {
        if (indexExists(connection, table, index)) {
            return;
        }
        String joined = String.join("`,`", columns);
        execute(connection, "CREATE INDEX `" + index + "` ON `" + table + "` (`" + joined + "`)");
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getColumns(connection.getCatalog(), null, table, column)) {
            return result.next();
        }
    }

    private static boolean indexExists(Connection connection, String table, String index) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet result = metadata.getIndexInfo(connection.getCatalog(), null, table, false, false)) {
            while (result.next()) {
                if (index.equalsIgnoreCase(result.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void grant(Connection connection, String roleCode, String... permissions) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(permissions.length, "?"));
        String sql = "INSERT IGNORE INTO role_permission (role_id, permission_id) "
                + "SELECT r.role_id, p.permission_id FROM role r JOIN permission p "
                + "ON p.permission_code IN (" + placeholders + ") WHERE r.role_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (String permission : permissions) {
                statement.setString(index++, permission);
            }
            statement.setString(index, roleCode);
            statement.executeUpdate();
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
