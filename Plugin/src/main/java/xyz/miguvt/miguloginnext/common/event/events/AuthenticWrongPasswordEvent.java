/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.event.events;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.miguloginnext.common.event.AuthenticPlayerBasedEvent;
import xyz.miguvt.miguloginnext.api.MiguLoginNextPlugin;
import xyz.miguvt.miguloginnext.api.database.User;
import xyz.miguvt.miguloginnext.api.event.events.WrongPasswordEvent;

public class AuthenticWrongPasswordEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements WrongPasswordEvent<P, S> {

    private final AuthenticationSource source;

    public AuthenticWrongPasswordEvent(@Nullable User user, P player, MiguLoginNextPlugin<P, S> plugin, AuthenticationSource source) {
        super(user, player, plugin);
        this.source = source;
    }

    @Override
    public AuthenticationSource getSource() {
        return source;
    }

}
