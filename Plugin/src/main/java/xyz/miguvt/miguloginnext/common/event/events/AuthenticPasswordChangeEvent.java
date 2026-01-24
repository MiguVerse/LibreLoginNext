/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.event.events;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.miguloginnext.common.event.AuthenticPlayerBasedEvent;
import xyz.miguvt.miguloginnext.api.MiguLoginNextPlugin;
import xyz.miguvt.miguloginnext.api.crypto.HashedPassword;
import xyz.miguvt.miguloginnext.api.database.User;
import xyz.miguvt.miguloginnext.api.event.events.PasswordChangeEvent;

public class AuthenticPasswordChangeEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements PasswordChangeEvent<P, S> {
    private final HashedPassword oldPassword;

    public AuthenticPasswordChangeEvent(@Nullable User user, @Nullable P player, MiguLoginNextPlugin<P, S> plugin, HashedPassword oldPassword) {
        super(user, player, plugin);
        this.oldPassword = oldPassword;
    }

    @Override
    public HashedPassword getOldPassword() {
        return oldPassword;
    }
}
