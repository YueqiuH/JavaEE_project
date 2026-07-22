package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V19__forum_write_permission extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        exec(connection, """
            INSERT INTO `permission` (`permission_code`, `permission_name`) VALUES
                ('forum:write', '发布和操作论坛内容')
            ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`)
        """);
        exec(connection, """
            INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
            SELECT r.role_id, p.permission_id
            FROM `role` r CROSS JOIN `permission` p
            WHERE p.permission_code = 'forum:write'
              AND r.role_code IN ('ADMIN', 'TEACHER', 'COUNSELOR', 'STAFF')
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
