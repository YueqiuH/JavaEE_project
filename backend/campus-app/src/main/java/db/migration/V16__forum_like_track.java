package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.Statement;

public class V16__forum_like_track extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        exec(connection, """
            CREATE TABLE IF NOT EXISTS `forum_post_like` (
                `id`      BIGINT   NOT NULL AUTO_INCREMENT,
                `post_id` BIGINT   NOT NULL,
                `user_id` BIGINT   NOT NULL,
                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (`id`),
                UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
        """);
    }

    private static void exec(Connection connection, String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}
