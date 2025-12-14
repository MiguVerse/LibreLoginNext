/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.api.event.events;

import xyz.miguvt.libreloginnext.api.event.PlayerBasedEvent;

/**
 * This event is called after the player has authenticated
 * Note, that this event will both be called if the player has authenticated manually, or automatically.
 * Use {@link #getReason()} to check the reason for authentication.
 *
 * @author miguvt, kyngs
 */
public interface AuthenticatedEvent<P, S> extends PlayerBasedEvent<P, S> {

    /**
     * Gets the reason for authentication.
     *
     * @return The reason for authentication
     */
    AuthenticationReason getReason();

    /**
     * Possible reasons for authentication.
     */
    enum AuthenticationReason {
        /**
         * The player has used the /login command, or they've been logged in by an admin
         */
        LOGIN,
        /**
         * The player has registered
         */
        REGISTER,
        /**
         * The player has been logged in automatically due to having enabled the auto-login feature
         */
        PREMIUM,
        /**
         * The player has been logged in due to having a valid session
         */
        SESSION
    }

}
