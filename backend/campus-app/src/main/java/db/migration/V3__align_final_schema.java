package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V3__align_final_schema extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        SchemaAlignment.alignFinalSchema(context.getConnection());
    }
}
