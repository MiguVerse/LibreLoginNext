/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.database;

import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.api.database.ReadWriteDatabaseProvider;
import xyz.miguvt.libreloginnext.api.database.connector.DatabaseConnector;

public abstract class AuthenticDatabaseProvider<C extends DatabaseConnector<?, ?>> implements ReadWriteDatabaseProvider {

    protected final C connector;
    protected final AuthenticLibreLoginNext<?, ?> plugin;

    protected AuthenticDatabaseProvider(C connector, AuthenticLibreLoginNext<?, ?> plugin) {
        this.connector = connector;
        this.plugin = plugin;
    }

    public void validateSchema() {
    }

}
