/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.command.commands.mail;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.common.authorization.AuthenticAuthorizationProvider;
import xyz.miguvt.miguloginnext.common.command.InvalidCommandArgument;
import xyz.miguvt.miguloginnext.common.config.ConfigurationKeys;
import xyz.miguvt.miguloginnext.common.event.events.AuthenticWrongPasswordEvent;
import xyz.miguvt.miguloginnext.common.util.GeneralUtil;
import xyz.miguvt.miguloginnext.common.util.RateLimiter;
import xyz.miguvt.miguloginnext.api.event.events.WrongPasswordEvent.AuthenticationSource;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@CommandAlias("setemail")
public class SetEMailCommand<P> extends EMailCommand<P> {

    private final RateLimiter<UUID> limiter = new RateLimiter<>(1, TimeUnit.MINUTES);

    public SetEMailCommand(AuthenticMiguLoginNext<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.set-email}")
    @CommandCompletion("%autocomplete.set-email")
    public CompletionStage<Void> onSetMail(Audience sender, UUID uuid, String mail, @Single String password) {
        return runAsync(() -> {
            var player = getPlayer(uuid);
            var user = getUser(uuid);

            var hashed = user.getHashedPassword();
            var crypto = getCrypto(hashed);

            if (!crypto.matches(password, hashed)) {
                plugin.getEventProvider()
                        .unsafeFire(plugin.getEventTypes().wrongPassword,
                                new AuthenticWrongPasswordEvent<>(user, player, plugin, AuthenticationSource.SET_EMAIL));
                throw new InvalidCommandArgument(getMessage("error-password-wrong"));
            }

            if (limiter.tryAndLimit(uuid)) {
                throw new InvalidCommandArgument(getMessage("error-mail-throttle"));
            }

            var token = GeneralUtil.generateAlphanumericText(16);

            sender.sendMessage(getMessage("info-mail-sending"));

            try {
                mailHandler.sendVerificationMail(mail, token, user.getLastNickname());
                getAuthorizationProvider().getEmailConfirmCache().put(uuid, new AuthenticAuthorizationProvider.EmailVerifyData(mail, token, uuid));
            } catch (Exception e) {
                if (plugin.getConfiguration().get(ConfigurationKeys.DEBUG)) {
                    getLogger().debug("Cannot send verification mail to " + mail + " for " + player);
                    e.printStackTrace();
                }
                throw new InvalidCommandArgument(getMessage("error-mail-not-sent"));
            }

            sender.sendMessage(getMessage("info-verification-mail-sent"));
        });
    }
}
