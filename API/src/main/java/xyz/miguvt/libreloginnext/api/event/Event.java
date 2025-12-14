/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.api.event;

import xyz.miguvt.libreloginnext.api.LibreLoginNextPlugin;
import xyz.miguvt.libreloginnext.api.PlatformHandle;

/**
 * An abstract event for all events
 *
 * @author miguvt, kyngs
 */
public interface Event<P, S> {

    /**
     * Gets the plugin instance
     *
     * @return the plugin instance
     */
    LibreLoginNextPlugin<P, S> getPlugin();

    /**
     * Gets the platform handle
     *
     * @return the platform handle
     */
    PlatformHandle<P, S> getPlatformHandle();

}
