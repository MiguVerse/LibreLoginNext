/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.config.migrate.config;

import xyz.miguvt.libreloginnext.common.config.ConfigurateHelper;
import xyz.miguvt.libreloginnext.common.config.migrate.ConfigurationMigrator;
import xyz.miguvt.libreloginnext.api.Logger;

/**
 * Migrates old 'librelogin-*' database types to 'libreloginnext-*'.
 * This is part of the LibreLoginNext rebranding.
 */
public class NinthConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var databaseType = helper.getString("database.type");
        
        if (databaseType != null) {
            String newType = switch (databaseType) {
                case "librelogin-mysql" -> "libreloginnext-mysql";
                case "librelogin-sqlite" -> "libreloginnext-sqlite";
                case "librelogin-postgresql" -> "libreloginnext-postgresql";
                default -> null;
            };
            
            if (newType != null) {
                logger.info("Migrating database type from '%s' to '%s'".formatted(databaseType, newType));
                helper.set("database.type", newType);
            }
        }
    }
}
