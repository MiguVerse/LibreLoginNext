/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.database;

import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.api.database.ReadWriteDatabaseProvider;
import xyz.miguvt.miguloginnext.api.database.connector.DatabaseConnector;

public abstract class AuthenticDatabaseProvider<C extends DatabaseConnector<?, ?>> implements ReadWriteDatabaseProvider {

    protected final C connector;
    protected final AuthenticMiguLoginNext<?, ?> plugin;

    protected AuthenticDatabaseProvider(C connector, AuthenticMiguLoginNext<?, ?> plugin) {
        this.connector = connector;
        this.plugin = plugin;
    }

    public void validateSchema() {
    }

}
