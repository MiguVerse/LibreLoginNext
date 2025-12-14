/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.config.migrate.config;

import xyz.miguvt.libreloginnext.common.config.ConfigurateHelper;
import xyz.miguvt.libreloginnext.common.config.migrate.ConfigurationMigrator;
import xyz.miguvt.libreloginnext.api.Logger;

public class SecondConfigurationMigrator implements ConfigurationMigrator {
    @Override
    public void migrate(ConfigurateHelper helper, Logger logger) {
        var list = helper.getStringList("allowed-commands-while-unauthorized");
        list.add("2faconfirm");
        helper.set("allowed-commands-while-unauthorized", list);
    }
}
