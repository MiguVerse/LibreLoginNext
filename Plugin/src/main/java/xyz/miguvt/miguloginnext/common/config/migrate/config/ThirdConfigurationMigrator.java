/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.config.migrate.config;

import xyz.miguvt.miguloginnext.common.config.ConfigurateHelper;
import xyz.miguvt.miguloginnext.common.config.migrate.ConfigurationMigrator;
import xyz.miguvt.miguloginnext.api.Logger;

public class ThirdConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var list = helper.getStringList("pass-through");

        helper.set("pass-through", null);
        helper.set("pass-through.root", list);
    }
}
