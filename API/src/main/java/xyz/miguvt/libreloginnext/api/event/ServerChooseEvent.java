/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.api.event;

import org.jetbrains.annotations.Nullable;

/**
 * An abstract event for events, that require server choosing
 *
 * @author miguvt, kyngs
 */
public interface ServerChooseEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Gets the server
     *
     * @return null, if default will be used
     */
    @Nullable
    S getServer();

    /**
     * Set the server
     *
     * @param server the server, if null, the default will be used
     */
    void setServer(@Nullable S server);

}
