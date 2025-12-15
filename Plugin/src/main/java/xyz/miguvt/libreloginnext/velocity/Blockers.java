/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import xyz.miguvt.libreloginnext.common.config.ConfigurationKeys;
import xyz.miguvt.libreloginnext.common.config.HoconPluginConfiguration;
import xyz.miguvt.libreloginnext.api.authorization.AuthorizationProvider;
import xyz.miguvt.libreloginnext.api.configuration.Messages;

public class Blockers {

    private final AuthorizationProvider<Player> authorizationProvider;
    private final HoconPluginConfiguration configuration;

    public Blockers(AuthorizationProvider<Player> authorizationProvider, HoconPluginConfiguration configuration, Messages messages) {
        this.authorizationProvider = authorizationProvider;
        this.configuration = configuration;
    }

    /**
     * Blocks chat messages from unauthorized players.
     * Note: setResult() is deprecated in Velocity but there's no direct replacement
     * for denying chat without sending to server. Using @SuppressWarnings until
     * Velocity provides an alternative API.
     */
    @SuppressWarnings("deprecation")
    @Subscribe(priority = Short.MIN_VALUE)
    public void onChat(PlayerChatEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) || authorizationProvider.isAwaiting2FA(event.getPlayer()))
            event.setResult(PlayerChatEvent.ChatResult.denied());
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;
        if (authorizationProvider.isAuthorized(player) && !authorizationProvider.isAwaiting2FA(player))
            return;

        var command = event.getCommand().split(" ")[0];

        for (String allowed : configuration.get(ConfigurationKeys.ALLOWED_COMMANDS_WHILE_UNAUTHORIZED)) {
            if (command.equals(allowed)) return;
        }

        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onServerConnect(ServerPreConnectEvent event) {
        if (authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            if (!configuration.get(ConfigurationKeys.LIMBO).contains(event.getOriginalServer().getServerInfo().getName())) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
        }
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onServerKick(KickedFromServerEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) || authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            event.getPlayer().disconnect(event.getServerKickReason().orElse(Component.text("Limbo not running")));
        }
    }

}
