package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Statement;

public class V54__counselor_evaluation extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `counselor_evaluation` (
                        `evaluation_id` BIGINT NOT NULL AUTO_INCREMENT,
                        `student_id` BIGINT NOT NULL,
                        `counselor_id` BIGINT NOT NULL,
                        `semester` VARCHAR(32) NOT NULL,
                        `score_teaching` INT NOT NULL,
                        `score_content` INT NOT NULL,
                        `score_method` INT NOT NULL,
                        `comment` TEXT DEFAULT NULL,
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (`evaluation_id`),
                        UNIQUE KEY `uk_counselor_evaluation_scope` (`student_id`, `counselor_id`, `semester`),
                        KEY `idx_counselor_evaluation_target` (`counselor_id`, `semester`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='辅导员评教表'
                    """);
        }
    }
}
