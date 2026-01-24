/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.command.commands.staff;

import org.jetbrains.annotations.NotNull;

import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.common.command.Command;
import xyz.miguvt.miguloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.miguloginnext.api.database.User;

public class StaffCommand<P> extends Command<P> {
    public StaffCommand(AuthenticMiguLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @NotNull
    protected User getUserOtherWiseInform(String name) {
        var user = plugin.getDatabaseProvider().getByName(name);

        if (user == null) throw new InvalidCommandArgument(getMessage("error-unknown-user"));

        return user;
    }

    protected void requireOffline(User user) {
        if (plugin.isPresent(user.getUuid()))
            throw new InvalidCommandArgument(getMessage("error-player-online"));
    }

    protected P requireOnline(User user) {
        if (plugin.multiProxyEnabled())
            throw new InvalidCommandArgument(getMessage("error-not-available-on-multi-proxy"));
        var player = plugin.getPlayerForUUID(user.getUuid());
        if (player == null)
            throw new InvalidCommandArgument(getMessage("error-player-offline"));
        return player;
    }

    protected P getPossiblyOnlinePlayerOnThisProxy(User user) {
        if (plugin.multiProxyEnabled()) {
            if (plugin.isPresent(user.getUuid())) {
                throw new InvalidCommandArgument(getMessage("error-player-online"));
            } else {
                return null;
            }
        }

        return plugin.getPlayerForUUID(user.getUuid());
    }

    protected void requireUnAuthorized(P player) {
        if (plugin.getAuthorizationProvider().isAuthorized(player))
            throw new InvalidCommandArgument(getMessage("error-player-authorized"));
    }

    protected void requireRegistered(User user) {
        if (!user.isRegistered())
            throw new InvalidCommandArgument(getMessage("error-player-not-registered"));
    }

}
