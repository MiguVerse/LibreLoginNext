/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticWrongPasswordEvent;
import xyz.miguvt.libreloginnext.api.event.events.AuthenticatedEvent;
import xyz.miguvt.libreloginnext.api.event.events.WrongPasswordEvent.AuthenticationSource;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("login|l|log")
public class LoginCommand<P> extends AuthorizationCommand<P> {

    public LoginCommand(AuthenticLibreLoginNext<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.login}")
    @CommandCompletion("%autocomplete.login")
    public CompletionStage<Void> onLogin(Audience sender, UUID uuid, @Single String password, @Optional String code) {
        return runAsync(() -> {
            checkUnauthorized(uuid);
            var player = getPlayer(uuid);
            var user = getUser(uuid);
            if (!user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-not-registered"));

            sender.sendMessage(getMessage("info-logging-in"));

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (crypto == null) throw new InvalidCommandArgument(getMessage("error-password-corrupted"));

            if (!crypto.matches(password, hashed)) {
                plugin.getEventProvider()
                        .unsafeFire(plugin.getEventTypes().wrongPassword,
                                new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.LOGIN));
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            var secret = user.getSecret();

            if (secret != null) {
                var totp = plugin.getTOTPProvider();

                if (totp != null) {
                    if (code == null) throw new InvalidCommandArgument(getMessage("totp-not-provided"));

                    int parsedCode;

                    try {
                        parsedCode = Integer.parseInt(code.trim().replace(" ", ""));
                    } catch (NumberFormatException e) {
                        throw new InvalidCommandArgument(getMessage("totp-wrong"));
                    }

                    if (!totp.verify(parsedCode, secret)) {
                        plugin.getEventProvider()
                                .unsafeFire(plugin.getEventTypes().wrongPassword,
                                        new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.TOTP));
                        throw new InvalidCommandArgument(getMessage("totp-wrong"));
                    }
                }
            }

            sender.sendMessage(getMessage("info-logged-in"));
            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.LOGIN);
        });
    }

}
