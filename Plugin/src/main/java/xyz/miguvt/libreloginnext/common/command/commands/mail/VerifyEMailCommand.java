/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands.mail;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.command.InvalidCommandArgument;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("verifyemail")
public class VerifyEMailCommand<P> extends EMailCommand<P> {

    public VerifyEMailCommand(AuthenticLibreLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.verify-email}")
    @CommandCompletion("%autocomplete.verify-email")
    public CompletionStage<Void> onVerifyMail(Audience sender, UUID uuid, String token) {
        return runAsync(() -> {
            var user = getUser(uuid);

            var cached = plugin.getAuthorizationProvider().getEmailConfirmCache().getIfPresent(user.getUuid());
            if (cached == null) {
                throw new InvalidCommandArgument(getMessage("error-no-mail-confirm"));
            }
            if (!cached.token().equals(token)) {
                throw new InvalidCommandArgument(getMessage("error-wrong-mail-verify"));
            }
            plugin.getAuthorizationProvider().getEmailConfirmCache().invalidate(user.getUuid());

            user.setEmail(cached.email());
            getDatabaseProvider().updateUser(user);

            sender.sendMessage(getMessage("info-mail-verified"));
        });
    }
}
