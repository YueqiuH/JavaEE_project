package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V17__forum_read_permission extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        exec(connection, """
            INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
                ('forum:read', '查看新闻与论坛')
            ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`)
        """);
        exec(connection, """
            INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
            SELECT r.role_id, p.permission_id
            FROM `role` r CROSS JOIN `permission` p
            WHERE p.permission_code = 'forum:read'
        """);
        exec(connection, """
            DELETE rp FROM `role_permission` rp
            JOIN `role` r ON r.role_id = rp.role_id
            JOIN `permission` p ON p.permission_id = rp.permission_id
            WHERE r.role_code = 'STUDENT' AND p.permission_code = 'base:read'
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
