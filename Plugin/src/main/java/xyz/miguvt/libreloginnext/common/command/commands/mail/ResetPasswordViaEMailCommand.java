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
import xyz.miguvt.libreloginnext.common.config.ConfigurationKeys;
import xyz.miguvt.libreloginnext.common.util.GeneralUtil;
import xyz.miguvt.libreloginnext.common.util.RateLimiter;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@CommandAlias("resetpassword")
public class ResetPasswordViaEMailCommand<P> extends EMailCommand<P> {

    private final RateLimiter<UUID> limiter = new RateLimiter<>(1, TimeUnit.MINUTES);

    public ResetPasswordViaEMailCommand(AuthenticLibreLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @Default
    @Syntax("{@@syntax.reset-password}")
    @CommandCompletion("%autocomplete.reset-password")
    public CompletionStage<Void> onResetPassword(Audience audience, UUID uuid) {
        return runAsync(() -> {
            var player = getPlayer(uuid);
            var user = getUser(uuid);

            if (user.getEmail() == null)
                throw new InvalidCommandArgument(getMessage("error-no-email"));

            if (limiter.tryAndLimit(uuid)) {
                throw new InvalidCommandArgument(getMessage("error-mail-throttle"));
            }

            var token = GeneralUtil.generateAlphanumericText(16);

            audience.sendMessage(getMessage("info-mail-sending"));

            try {
                mailHandler.sendPasswordResetMail(user.getEmail(), token, user.getLastNickname(), plugin.getPlatformHandle().getIP(player));
                getAuthorizationProvider().getPasswordResetCache().put(uuid, token);
            } catch (Exception e) {
                if (plugin.getConfiguration().get(ConfigurationKeys.DEBUG)) {
                    getLogger().debug("Cannot send verification mail to " + user.getEmail() + " for " + player);
                    e.printStackTrace();
                }
                throw new InvalidCommandArgument(getMessage("error-mail-not-sent"));
            }

            audience.sendMessage(getMessage("info-reset-password-mail-sent"));
        });
    }
}
