package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V56__remove_teacher_fee_overview extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        exec(connection, """
            DELETE rp FROM `role_permission` rp
            JOIN `role` r ON r.role_id = rp.role_id
            JOIN `permission` p ON p.permission_id = rp.permission_id
            WHERE r.role_code = 'TEACHER' AND p.permission_code = 'fee:overview:read'
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
