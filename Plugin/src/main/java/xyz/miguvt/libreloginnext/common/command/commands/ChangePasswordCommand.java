/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.command.Command;
import xyz.miguvt.libreloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticPasswordChangeEvent;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticWrongPasswordEvent;
import xyz.miguvt.libreloginnext.api.event.events.WrongPasswordEvent.AuthenticationSource;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("changepassword|changepass|passwd|passch")
public class ChangePasswordCommand<P> extends Command<P> {
    public ChangePasswordCommand(AuthenticLibreLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.change-password}")
    @CommandCompletion("%autocomplete.change-password")
    public CompletionStage<Void> onPasswordChange(Audience sender, UUID uuid, String oldPass, @Single String newPass) {
        return runAsync(() -> {
            var player = getPlayer(uuid);
            var user = getUser(uuid);

            if (!user.isRegistered()) {
                throw new InvalidCommandArgument(getMessage("error-no-password"));
            }

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (!crypto.matches(oldPass, hashed)) {
                plugin.getEventProvider()
                        .unsafeFire(plugin.getEventTypes().wrongPassword,
                                new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.CHANGE_PASSWORD));
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            setPassword(sender, user, newPass, "info-editing");

            getDatabaseProvider().updateUser(user);

            sender.sendMessage(getMessage("info-edited"));

            plugin.getEventProvider().unsafeFire(plugin.getEventTypes().passwordChange, new AuthenticPasswordChangeEvent<>(user, player, plugin, hashed));
        });
    }

}
