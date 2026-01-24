/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.config.migrate.config;

import xyz.miguvt.miguloginnext.common.config.ConfigurateHelper;
import xyz.miguvt.miguloginnext.common.config.migrate.ConfigurationMigrator;
import xyz.miguvt.miguloginnext.api.Logger;

/**
 * Migrates old 'librelogin-*' database types to 'miguloginnext-*'.
 * This is part of the MiguLoginNext rebranding.
 */
public class NinthConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var databaseType = helper.getString("database.type");
        
        if (databaseType != null) {
            String newType = switch (databaseType) {
                case "librelogin-mysql" -> "miguloginnext-mysql";
                case "librelogin-sqlite" -> "miguloginnext-sqlite";
                case "librelogin-postgresql" -> "miguloginnext-postgresql";
                default -> null;
            };
            
            if (newType != null) {
                logger.info("Migrating database type from '%s' to '%s'".formatted(databaseType, newType));
                helper.set("database.type", newType);
            }
        }
    }
}
