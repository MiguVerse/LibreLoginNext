/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageKeys;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.authorization.AuthenticAuthorizationProvider;
import xyz.miguvt.libreloginnext.common.util.GeneralUtil;
import xyz.miguvt.libreloginnext.api.Logger;
import xyz.miguvt.libreloginnext.api.configuration.Messages;
import xyz.miguvt.libreloginnext.api.crypto.CryptoProvider;
import xyz.miguvt.libreloginnext.api.crypto.HashedPassword;
import xyz.miguvt.libreloginnext.api.database.ReadWriteDatabaseProvider;
import xyz.miguvt.libreloginnext.api.database.User;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class Command<P> extends BaseCommand {

    protected final AuthenticLibreLoginNext<P, ?> plugin;

    public Command(AuthenticLibreLoginNext<P, ?> plugin) {
        this.plugin = plugin;
    }

    protected ReadWriteDatabaseProvider getDatabaseProvider() {
        return plugin.getDatabaseProvider();
    }

    protected Logger getLogger() {
        return plugin.getLogger();
    }

    protected Messages getMessages() {
        return plugin.getMessages();
    }

    protected TextComponent getMessage(String key, String... replacements) {
        return getMessages().getMessage(key, replacements);
    }

    protected AuthenticAuthorizationProvider<P, ?> getAuthorizationProvider() {
        return plugin.getAuthorizationProvider();
    }

    protected P getPlayer(UUID uuid) {
        var player = plugin.getPlayerForUUID(uuid);
        if (player == null)
            throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
        return player;
    }

    protected void checkAuthorized(UUID uuid) {
        var player = getPlayer(uuid);
        if (!getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-not-authorized"));
        }
    }

    protected CryptoProvider getCrypto(HashedPassword password) {
        return plugin.getCryptoProvider(password.algo());
    }

    public CompletionStage<Void> runAsync(Runnable runnable) {
        return GeneralUtil.runAsync(runnable);
    }

    protected User getUser(UUID uuid) {
        if (uuid == null)
            throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

        if (plugin.fromFloodgate(uuid)) throw new InvalidCommandArgument(getMessage("error-from-floodgate"));

        return plugin.getDatabaseProvider().getByUUID(uuid);
    }

    protected void setPassword(Audience sender, User user, String password, String messageKey) {
        if (!plugin.validPassword(password))
            throw new InvalidCommandArgument(getMessage("error-forbidden-password"));

        sender.sendMessage(getMessage(messageKey));

        var defaultProvider = plugin.getDefaultCryptoProvider();

        var hash = defaultProvider.createHash(password);

        if (hash == null) {
            throw new InvalidCommandArgument(getMessage("error-password-too-long"));
        }

        user.setHashedPassword(hash);
    }

}
