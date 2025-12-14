/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands.premium;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticWrongPasswordEvent;
import xyz.miguvt.libreloginnext.api.event.events.WrongPasswordEvent.AuthenticationSource;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("premium|autologin")
public class PremiumEnableCommand<P> extends PremiumCommand<P> {

    public PremiumEnableCommand(AuthenticLibreLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.premium}")
    @CommandCompletion("%autocomplete.premium")
    public CompletionStage<Void> onPremium(Audience sender, UUID uuid, @Single String password) {
        return runAsync(() -> {
            var player = getPlayer(uuid);
            var user = getUser(uuid);
            checkCracked(user);

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (!crypto.matches(password, hashed)) {
                plugin.getEventProvider()
                        .unsafeFire(plugin.getEventTypes().wrongPassword,
                                new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.PREMIUM_ENABLE));
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            plugin.getCommandProvider().registerConfirm(uuid);

            sender.sendMessage(getMessage("prompt-confirm"));
        });
    }

}
