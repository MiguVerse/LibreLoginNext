/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.api.database;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.libreloginnext.api.database.connector.DatabaseConnector;
import xyz.miguvt.libreloginnext.api.util.ThrowableFunction;

/**
 * The registration of a read database provider.
 *
 * @param factory           a factory that creates a new instance of the provider
 * @param id                the id of the provider, suggested format: "plugin_name-connector_id" (e.g. "libreloginnext-mysql", or "authme-sqlite")
 * @param databaseConnector The class of the database connector you wish to use, should match the C type parameter. If null, no connector will be supplied in {@link #factory}
 * @param <R>               The type of the read database provider described by this registration
 * @param <C>               The type of the database connector
 * @param <E>               The type of the common exception thrown by the database. (e.g. SQLException)
 * @author miguvt, kyngs
 */
public record ReadDatabaseProviderRegistration<R extends ReadDatabaseProvider, C extends DatabaseConnector<E, ?>, E extends Exception>(
        ThrowableFunction<C, R, E> factory, String id, @Nullable Class<C> databaseConnector) {

    /**
     * Creates an instance of a read database provider using the given database connector.
     *
     * @param connector the database connector to be used
     * @return the created object
     * @throws E if an error occurs during the creation process
     */
    @SuppressWarnings("unchecked") // Type safety is ensured by the caller providing the correct connector type
    public R create(DatabaseConnector<?, ?> connector) throws E {
        return factory.apply((C) connector);
    }
}
