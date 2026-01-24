/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.miguloginnext.api.event.events.AuthenticatedEvent;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("register|reg")
public class RegisterCommand<P> extends AuthorizationCommand<P> {
    public RegisterCommand(AuthenticMiguLoginNext<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.register}")
    @CommandCompletion("%autocomplete.register")
    public CompletionStage<Void> onRegister(Audience sender, UUID uuid, @Single String password, String passwordRepeat) {
        return runAsync(() -> {
            checkUnauthorized(uuid);
            var player = getPlayer(uuid);
            var user = getUser(uuid);

            if (user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-already-registered"));
            if (!password.contentEquals(passwordRepeat))
                throw new InvalidCommandArgument(getMessage("error-password-not-match"));

            setPassword(sender, user, password, "info-registering");

            sender.sendMessage(getMessage("info-registered"));

            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.REGISTER);
        });
    }

}
