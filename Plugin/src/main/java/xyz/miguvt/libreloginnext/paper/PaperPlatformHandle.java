/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.paper;

import com.google.common.base.MoreObjects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import xyz.miguvt.libreloginnext.api.PlatformHandle;
import xyz.miguvt.libreloginnext.api.server.ServerPing;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperPlatformHandle implements PlatformHandle<Player, World> {

    private final PaperLibreLoginNext plugin;

    public PaperPlatformHandle(PaperLibreLoginNext plugin) {
        this.plugin = plugin;
    }

    @Override
    public Audience getAudienceForPlayer(Player player) {
        return player;
    }

    @Override
    public UUID getUUIDForPlayer(Player player) {
        return player.getUniqueId();
    }

    @Override
    public CompletableFuture<Throwable> movePlayer(Player player, World to) {
        return player.teleportAsync(to.getSpawnLocation())
                .thenApply(success -> success ? null : new RuntimeException("Unknown cause"));
    }

    @Override
    public void kick(Player player, Component reason) {
        PaperUtil.runSyncAndWait(() -> player.kick(reason), plugin);
    }

    /**
     * Gets or creates a world/server for the given name.
     * Note: Contains deprecated API calls (setKeepSpawnInMemory, GameRule constants) that are
     * marked for removal in future Paper versions. These will need migration when alternatives exist.
     */
    @SuppressWarnings("removal")
    @Override
    public World getServer(String name, boolean limbo) {
        var world = Bukkit.getWorld(name);

        if (world != null) return world;

        var file = new File(name);
        var exists = file.exists();

        if (exists) {
            plugin.getLogger().info("Found world file for " + name + ", loading...");
        } else {
            plugin.getLogger().info("World file for " + name + " not found, creating...");
        }

        var creator = new WorldCreator(name);

        if (limbo) {
            creator.generator("libreloginnext:void");
        }

        world = Bukkit.createWorld(creator);

        if (limbo) {
            world.setSpawnLocation(new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5));
            // Note: setKeepSpawnInMemory is deprecated for removal in 1.20.5, but no direct replacement exists.
            // This ensures spawn chunks remain loaded for limbo functionality.
            world.setKeepSpawnInMemory(true);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_INSOMNIA, false);
        }

        return world;
    }

    @Override
    public Class<World> getServerClass() {
        return World.class;
    }

    @Override
    public Class<Player> getPlayerClass() {
        return Player.class;
    }

    @Override
    public String getIP(Player player) {
        return player.getAddress().getAddress().getHostAddress();
    }

    @Override
    public ServerPing ping(World server) {
        return new ServerPing(Integer.MAX_VALUE);
    }

    @Override
    public Collection<World> getServers() {
        return Bukkit.getWorlds();
    }

    @Override
    public String getServerName(World server) {
        return server.getName();
    }

    @Override
    public int getConnectedPlayers(World server) {
        return server.getPlayerCount();
    }

    @Override
    public String getPlayersServerName(Player player) {
        return player.getWorld().getName();
    }

    @Override
    public String getPlayersVirtualHost(Player player) {
        return null;
    }

    @Override
    public String getUsernameForPlayer(Player player) {
        return player.getName();
    }

    @Override
    public String getPlatformIdentifier() {
        return "paper";
    }

    @SuppressWarnings("null") // Bukkit API lacks null annotations
    @Override
    public ProxyData getProxyData() {
        return new ProxyData(
                Bukkit.getName() + " " + Bukkit.getVersion(),
                getServers().stream().map(this::fromWorld).toList(),
                Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(p ->
                        MoreObjects.toStringHelper(p)
                                .add("name", p.getName())
                                .add("version", p.getPluginMeta().getVersion())
                                .add("authors", p.getPluginMeta().getAuthors())
                                .toString()
                ).toList(),
                plugin.getServerHandler().getLimboServers().stream().map(this::fromWorld).toList(),
                plugin.getServerHandler().getLobbyServers().values().stream().map(this::fromWorld).toList()
        );
    }

    @SuppressWarnings("null") // Bukkit API lacks null annotations
    private String fromWorld(World world) {
        return MoreObjects.toStringHelper(world)
                .add("name", world.getName())
                .add("environment", world.getEnvironment())
                .add("difficulty", world.getDifficulty())
                .add("players", world.getPlayers().size())
                .toString();

    }

}
