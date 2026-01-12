/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.server;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.Nullable;

import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.config.ConfigurationKeys;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticLimboServerChooseEvent;
import xyz.miguvt.libreloginnext.common.event.events.AuthenticLobbyServerChooseEvent;
import xyz.miguvt.libreloginnext.api.database.User;
import xyz.miguvt.libreloginnext.api.event.exception.EventCancelledException;
import xyz.miguvt.libreloginnext.api.server.ServerHandler;
import xyz.miguvt.libreloginnext.api.server.ServerPing;

import static xyz.miguvt.libreloginnext.common.config.ConfigurationKeys.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

public class AuthenticServerHandler<P, S> implements ServerHandler<P, S> {

    private final LoadingCache<S, Optional<ServerPing>> pingCache;
    private final AuthenticLibreLoginNext<P, S> plugin;
    private final Collection<S> limboServers;
    private final Multimap<String, S> lobbyServers;

    public AuthenticServerHandler(AuthenticLibreLoginNext<P, S> plugin) {
        this.plugin = plugin;

        this.lobbyServers = HashMultimap.create();
        this.limboServers = new ArrayList<>();

        this.pingCache = Caffeine.newBuilder()
                .build(server -> {
                    if (!plugin.getConfiguration().get(ConfigurationKeys.PING_SERVERS))
                        return Optional.of(new ServerPing(Integer.MAX_VALUE));

                    plugin.getLogger().debug("Pinging server " + server);
                    var ping = plugin.getPlatformHandle().ping(server);
                    plugin.getLogger().debug("Pinged server " + server + ": " + ping);

                    return Optional.ofNullable(plugin.getConfiguration().get(IGNORE_MAX_PLAYERS_FROM_BACKEND_PING) ? new ServerPing(Integer.MAX_VALUE) : ping);
                });

        plugin.repeat(() -> pingCache.refreshAll(pingCache.asMap().keySet()), 10000, 10000);

        var handle = plugin.getPlatformHandle();

        if (plugin.getConfiguration().get(ConfigurationKeys.PING_SERVERS))
            plugin.getLogger().info("Pinging servers...");

        for (String limbo : plugin.getConfiguration().get(LIMBO)) {
            var server = handle.getServer(limbo, true);
            if (server != null) {
                registerLimboServer(server);
            } else {
                plugin.getLogger().warn("Limbo server/world " + limbo + " not found!");
            }
        }

        plugin.getConfiguration().get(ConfigurationKeys.LOBBY).forEach((forced, server) -> {
            var s = handle.getServer(server, false);
            if (s != null) {
                registerLobbyServer(s, forced);
            } else {
                plugin.getLogger().warn("Lobby server/world " + server + " not found!");
            }
        });

        plugin.getLogger().debug("List of registered servers: ");


        for (S server : plugin.getPlatformHandle().getServers()) {
            plugin.getLogger().debug("Server: " + plugin.getPlatformHandle().getServerName(server) + " | " + server);
        }

        if (plugin.getConfiguration().get(ConfigurationKeys.PING_SERVERS)) plugin.getLogger().info("Pinged servers...");
    }

    @Override
    public ServerPing getLatestPing(S server) {
        return pingCache.get(server).orElse(null);
    }

    @Override
    public S chooseLobbyServer(@Nullable User user, P player, boolean remember, boolean fallback) {
        return chooseLobbyServerInternal(user, player, remember, fallback);
    }

    public S chooseLobbyServerInternal(@Nullable User user, P player, boolean remember, Boolean fallback) {
        if (user != null && remember && plugin.getConfiguration().get(REMEMBER_LAST_SERVER)) {
            var last = user.getLastServer();

            if (last != null) {
                var server = plugin.getPlatformHandle().getServer(last, false);
                if (server != null) {
                    var ping = getLatestPing(server);
                    if (ping != null && ping.maxPlayers() > plugin.getPlatformHandle().getConnectedPlayers(server)) {
                        return server;
                    }
                }
            }
        }

        var event = new AuthenticLobbyServerChooseEvent<>(user, player, plugin, fallback);

        plugin.getEventProvider().fire(plugin.getEventTypes().lobbyServerChoose, event);

        if (event.isCancelled()) throw new EventCancelledException();

        if (event.getServer() != null) return event.getServer();

        // Handle null player, during AsyncPlayerSpawnLocationEvent, player doesn't exist yet
        var virtual = player != null ? plugin.getPlatformHandle().getPlayersVirtualHost(player) : null;

        // Get username safely, prefer player if available, fallback to user
        String username = player != null ? plugin.getPlatformHandle().getUsernameForPlayer(player) : 
                        user != null ? user.getLastNickname() : "unknown";
        
        plugin.getLogger().debug("Virtual host for player " + username + ": " + virtual);

        var servers = virtual == null ? lobbyServers.get("root") : lobbyServers.get(virtual);

        if (servers.isEmpty()) servers = lobbyServers.get("root");

        return servers.stream()
                .filter(server -> {
                    var ping = getLatestPing(server);

                    return ping != null && ping.maxPlayers() > plugin.getPlatformHandle().getConnectedPlayers(server);
                })
                .min(Comparator.comparingInt(o -> plugin.getPlatformHandle().getConnectedPlayers(o)))
                .orElse(null);
    }

    @Override
    public S chooseLobbyServer(@Nullable User user, P player, boolean remember) {
        return chooseLobbyServerInternal(user, player, remember, null);
    }

    @Override
    public S chooseLimboServer(User user, P player) {
        var event = new AuthenticLimboServerChooseEvent<>(user, player, plugin);

        plugin.getEventProvider().fire(plugin.getEventTypes().limboServerChoose, event);

        if (event.getServer() != null) return event.getServer();

        return limboServers.stream()
                .filter(server -> {
                    var ping = getLatestPing(server);

                    return ping != null && ping.maxPlayers() > plugin.getPlatformHandle().getConnectedPlayers(server);
                })
                .min(Comparator.comparingInt(o -> plugin.getPlatformHandle().getConnectedPlayers(o)))
                .orElse(null);
    }

    @Override
    public Multimap<String, S> getLobbyServers() {
        return lobbyServers;
    }

    @Override
    public Collection<S> getLimboServers() {
        return limboServers;
    }

    @Override
    public void registerLobbyServer(S server, String forcedHost) {
        Objects.requireNonNull(server, "server cannot be null");
        Objects.requireNonNull(forcedHost, "forcedHost cannot be null");
        getLatestPing(server);
        lobbyServers.put(forcedHost, server);
    }

    @Override
    public void registerLimboServer(S server) {
        getLatestPing(server);
        limboServers.add(server);
    }
}
