/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.migrate;

import xyz.miguvt.miguloginnext.common.database.AuthenticUser;
import xyz.miguvt.miguloginnext.api.Logger;
import xyz.miguvt.miguloginnext.api.crypto.HashedPassword;
import xyz.miguvt.miguloginnext.api.database.User;
import xyz.miguvt.miguloginnext.api.database.connector.SQLDatabaseConnector;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class AuthySQLMigrateReadProvider extends SQLMigrateReadProvider {
    public AuthySQLMigrateReadProvider(String tableName, Logger logger, SQLDatabaseConnector connector) {
        super(tableName, logger, connector);
    }

    @Override
    public Collection<User> getAllUsers() {
        return connector.runQuery(connection -> {
            var ps = connection.prepareStatement("SELECT * FROM `%s`".formatted(tableName));

            var rs = ps.executeQuery();

            var users = new HashSet<User>();

            while (rs.next()) {
                try {
                    var uuid = rs.getString("uuid");
                    var username = rs.getString("username");
                    var ip = rs.getString("ip");
                    var passwordHash = rs.getString("password");

                    if (uuid == null || username == null) continue;

                    var password = new HashedPassword(passwordHash, null, "SHA-256");

                    users.add(new AuthenticUser(
                            UUID.fromString(uuid),
                            null,
                            password,
                            username,
                            Timestamp.from(Instant.now()),
                            Timestamp.from(Instant.now()),
                            null,
                            ip,
                            null,
                            null,
                            null
                    ));
                } catch (Exception e) {
                    logger.error("Error while migrating user from Authy db, omitting");
                }
            }

            return users;
        });
    }
}
