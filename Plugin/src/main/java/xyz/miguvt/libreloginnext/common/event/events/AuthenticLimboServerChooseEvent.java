/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.event.events;

import org.jetbrains.annotations.Nullable;

import xyz.miguvt.libreloginnext.common.event.AuthenticServerChooseEvent;
import xyz.miguvt.libreloginnext.api.LibreLoginNextPlugin;
import xyz.miguvt.libreloginnext.api.database.User;
import xyz.miguvt.libreloginnext.api.event.events.LimboServerChooseEvent;

public class AuthenticLimboServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LimboServerChooseEvent<P, S> {
    public AuthenticLimboServerChooseEvent(@Nullable User user, P player, LibreLoginNextPlugin<P, S> plugin) {
        super(user, player, plugin);
    }
}
