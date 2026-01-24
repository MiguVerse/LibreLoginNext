/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.event;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.miguloginnext.api.MiguLoginNextPlugin;
import xyz.miguvt.miguloginnext.api.database.User;
import xyz.miguvt.miguloginnext.api.event.ServerChooseEvent;

public class AuthenticServerChooseEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements ServerChooseEvent<P, S> {

    private S server = null;

    public AuthenticServerChooseEvent(@Nullable User user, P player, MiguLoginNextPlugin<P, S> plugin) {
        super(user, player, plugin);
    }

    @Nullable
    @Override
    public S getServer() {
        return server;
    }

    @Override
    public void setServer(S server) {
        this.server = server;
    }
}
