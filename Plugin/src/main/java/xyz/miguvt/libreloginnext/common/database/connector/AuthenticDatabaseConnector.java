/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.database.connector;

import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.config.key.ConfigurationKey;
import xyz.miguvt.libreloginnext.api.database.connector.DatabaseConnector;

public abstract class AuthenticDatabaseConnector<E extends Exception, I> implements DatabaseConnector<E, I> {

    protected final AuthenticLibreLoginNext<?, ?> plugin;
    private final String prefix;
    protected boolean connected = true;

    public AuthenticDatabaseConnector(AuthenticLibreLoginNext<?, ?> plugin, String prefix) {
        this.plugin = plugin;
        this.prefix = prefix;
    }

    @Override
    public boolean connected() {
        return connected;
    }

    public <T> T get(ConfigurationKey<T> key) {
        var value = key.getter().apply(plugin.getConfiguration().getHelper(), prefix + key.key());
        return value == null ? key.defaultValue() : value;
    }

}
