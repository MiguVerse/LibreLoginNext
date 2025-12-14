/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands.authorization;

import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.command.Command;
import xyz.miguvt.libreloginnext.common.command.InvalidCommandArgument;

import java.util.UUID;

public class AuthorizationCommand<P> extends Command<P> {

    public AuthorizationCommand(AuthenticLibreLoginNext<P, ?> premium) {
        super(premium);
    }

    protected void checkUnauthorized(UUID uuid) {
        var player = getPlayer(uuid);
        if (getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-already-authorized"));
        }
    }

}
