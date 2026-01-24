/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.command.commands.premium;

import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.miguloginnext.common.command.commands.authorization.AuthorizationCommand;
import xyz.miguvt.miguloginnext.api.database.User;

public class PremiumCommand<P> extends AuthorizationCommand<P> {
    public PremiumCommand(AuthenticMiguLoginNext<P, ?> premium) {
        super(premium);
    }

    protected void checkCracked(User user) {
        if (user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-cracked"));
    }

    protected void checkPremium(User user) {
        if (!user.autoLoginEnabled()) throw new InvalidCommandArgument(getMessage("error-not-premium"));
    }

}
