/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.bungeecord;

import static xyz.miguvt.libreloginnext.common.config.ConfigurationKeys.ALLOWED_COMMANDS_WHILE_UNAUTHORIZED;
import static xyz.miguvt.libreloginnext.common.config.ConfigurationKeys.LIMBO;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import xyz.miguvt.libreloginnext.common.config.HoconPluginConfiguration;
import xyz.miguvt.libreloginnext.api.authorization.AuthorizationProvider;

public class Blockers implements Listener {

    private final AuthorizationProvider<ProxiedPlayer> authorizationProvider;
    private final HoconPluginConfiguration configuration;

    public Blockers(BungeeCordLibreLoginNext plugin) {
        this.authorizationProvider = plugin.getAuthorizationProvider();
        this.configuration = plugin.getConfiguration();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if (event.isCommand()) {
            onCommand(event);
            return;
        }

        if (event.getSender() instanceof ProxiedPlayer player) {
            if (!authorizationProvider.isAuthorized(player) || authorizationProvider.isAwaiting2FA(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(ChatEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer player)) return;
        if (authorizationProvider.isAuthorized(player) && !authorizationProvider.isAwaiting2FA(player))
            return;

        var command = event.getMessage().substring(1).split(" ")[0];

        for (String allowed : configuration.get(ALLOWED_COMMANDS_WHILE_UNAUTHORIZED)) {
            if (command.equals(allowed)) return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerConnect(ServerConnectEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) && event.getReason() != ServerConnectEvent.Reason.JOIN_PROXY) {
            event.setCancelled(true);
        } else if (authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            if (!configuration.get(LIMBO).contains(event.getTarget().getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerKick(ServerKickEvent event) {
        if (!authorizationProvider.isAuthorized(event.getPlayer()) || authorizationProvider.isAwaiting2FA(event.getPlayer())) {
            var reason = event.getReason();
            if (reason == null) {
                event.getPlayer().disconnect(new net.md_5.bungee.api.chat.TextComponent("Limbo not running"));
            } else {
                event.getPlayer().disconnect(reason);
            }
        }
    }

}
