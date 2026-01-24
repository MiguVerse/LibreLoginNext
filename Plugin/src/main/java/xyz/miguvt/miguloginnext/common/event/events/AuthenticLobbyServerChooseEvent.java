/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.event.events;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.miguloginnext.common.event.AuthenticServerChooseEvent;
import xyz.miguvt.miguloginnext.api.MiguLoginNextPlugin;
import xyz.miguvt.miguloginnext.api.database.User;
import xyz.miguvt.miguloginnext.api.event.events.LobbyServerChooseEvent;

public class AuthenticLobbyServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LobbyServerChooseEvent<P, S> {

    private final Boolean fallback;
    private boolean cancelled = false;

    public AuthenticLobbyServerChooseEvent(@Nullable User user, P player, MiguLoginNextPlugin<P, S> plugin, Boolean fallback) {
        super(user, player, plugin);

        this.fallback = fallback;
    }

    @Override
    public Boolean isFallback() {
        return fallback;
    }


    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
