/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.migrate;

import xyz.miguvt.libreloginnext.api.Logger;
import xyz.miguvt.libreloginnext.api.database.connector.SQLDatabaseConnector;

public abstract class SQLMigrateReadProvider extends MigrateReadProvider {

    protected final String tableName;
    protected final Logger logger;
    protected final SQLDatabaseConnector connector;

    public SQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        this.tableName = tableName;
        this.logger = logger;
        this.connector = connector;
    }
}
