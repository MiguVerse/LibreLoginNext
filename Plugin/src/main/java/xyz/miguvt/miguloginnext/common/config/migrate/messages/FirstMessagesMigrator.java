/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.config.migrate.messages;

import xyz.miguvt.miguloginnext.common.config.ConfigurateHelper;
import xyz.miguvt.miguloginnext.common.config.migrate.ConfigurationMigrator;
import xyz.miguvt.miguloginnext.api.Logger;

public class FirstMessagesMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var autoLoginText = helper.getString("info-automatically-logged-in");

        if (autoLoginText != null) {
            helper.set("info-premium-logged-in", autoLoginText);
            helper.set("info-session-logged-in", autoLoginText);
        }

        helper.set("info-automatically-logged-in", null);
    }
}
