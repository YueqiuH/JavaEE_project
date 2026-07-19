package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V4__complete_role_permissions extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        SchemaAlignment.alignFinalPermissions(context.getConnection());
    }
}
