/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.velocity;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.VelocityCommandIssuer;
import co.aikar.commands.VelocityCommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bstats.charts.CustomChart;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.Nullable;

import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.SLF4JLogger;
import xyz.miguvt.libreloginnext.common.config.ConfigurationKeys;
import xyz.miguvt.libreloginnext.common.image.AuthenticImageProjector;
import xyz.miguvt.libreloginnext.common.image.packetevents.PacketEventsImageProjector;
import xyz.miguvt.libreloginnext.common.util.CancellableTask;
import xyz.miguvt.libreloginnext.velocity.integration.VelocityNanoLimboIntegration;
import xyz.miguvt.libreloginnext.api.Logger;
import xyz.miguvt.libreloginnext.api.PlatformHandle;
import xyz.miguvt.libreloginnext.api.database.User;
import xyz.miguvt.libreloginnext.api.event.exception.EventCancelledException;
import xyz.miguvt.libreloginnext.api.integration.LimboIntegration;

import static xyz.miguvt.libreloginnext.common.config.ConfigurationKeys.DEBUG;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VelocityLibreLoginNext extends AuthenticLibreLoginNext<Player, RegisteredServer> {

    private final VelocityBootstrap bootstrap;
    @Inject
    private org.slf4j.Logger logger;
    @Inject
    @DataDirectory
    private Path dataDir;
    @Inject
    private ProxyServer server;
    @Inject
    private Metrics.Factory factory;
    @Inject
    private PluginDescription description;
    @Nullable
    private VelocityRedisBungeeIntegration redisBungee;
    @Nullable
    private LimboIntegration<RegisteredServer> limboIntegration;

    public VelocityLibreLoginNext(VelocityBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    protected void disable() {
        super.disable();
    }

    public ProxyServer getServer() {
        return server;
    }

    static {
        System.setProperty("auth.forceSecureProfiles", "false");
    }

    @Override
    protected PlatformHandle<Player, RegisteredServer> providePlatformHandle() {
        return new VelocityPlatformHandle(this);
    }

    @Override
    protected Logger provideLogger() {
        return new SLF4JLogger(logger, () -> getConfiguration().get(DEBUG));
    }

    @Override
    public CommandManager<?, ?, ?, ?, ?, ?> provideManager() {
        return new VelocityCommandManager(server, bootstrap);
    }

    @Override
    public Player getPlayerFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getPlayer();
    }

    @Override
    public void authorize(Player player, User user, Audience audience) {
        try {
            var lobby = getServerHandler().chooseLobbyServer(user, player, true, false);
            if (lobby == null) {
                player.disconnect(getMessages().getMessage("kick-no-lobby"));
                return;
            }
            player
                    .createConnectionRequest(
                            lobby
                    )
                    .connect()
                    .whenComplete((result, throwable) -> {
                        if (player.getCurrentServer().isEmpty()) return;
                        if (player.getCurrentServer().get().getServerInfo().getName().equals(result.getAttemptedConnection().getServerInfo().getName()))
                            return;
                        if (throwable != null || !result.isSuccessful())
                            player.disconnect(Component.text("Unable to connect"));
                    });
        } catch (EventCancelledException ignored) {}
    }

    @Override
    public CancellableTask delay(Runnable runnable, long delayInMillis) {
        var task = server.getScheduler()
                .buildTask(bootstrap, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .schedule();
        return task::cancel;
    }

    @Override
    public CancellableTask repeat(Runnable runnable, long delayInMillis, long repeatInMillis) {
        var task = server.getScheduler()
                .buildTask(bootstrap, runnable)
                .delay(delayInMillis, TimeUnit.MILLISECONDS)
                .repeat(repeatInMillis, TimeUnit.MILLISECONDS)
                .schedule();

        return task::cancel;
    }

    @Override
    public boolean pluginPresent(String pluginName) {
        return server.getPluginManager().getPlugin(pluginName).isPresent();
    }

    @Override
    protected AuthenticImageProjector<Player, RegisteredServer> provideImageProjector() {
        getLogger().info("PacketEvents detected, enabling 2FA...");
        return new PacketEventsImageProjector<>(this);
    }

    @Override
    protected void enable() {
        if (pluginPresent("redisbungee")) {
            redisBungee = new VelocityRedisBungeeIntegration();
        }
        super.enable();
        getLogger().info("LibreLoginNext version " + getVersion() + " enabled!");
    }

    @Override
    public String getVersion() {
        return description.getVersion().orElseThrow();
    }

    @Override
    public boolean isPresent(UUID uuid) {
        return redisBungee != null ? redisBungee.isPlayerOnline(uuid) : getPlayerForUUID(uuid) != null;
    }

    @Override
    public boolean multiProxyEnabled() {
        return redisBungee != null;
    }

    @Override
    public Player getPlayerForUUID(UUID uuid) {
        return server.getPlayer(uuid).orElse(null);
    }

    @Override
    protected void initMetrics(CustomChart... charts) {
        var metrics = factory.make(bootstrap, 17981);

        for (CustomChart chart : charts) {
            metrics.addCustomChart(chart);
        }
    }

    @Override
    public Audience getAudienceFromIssuer(CommandIssuer issuer) {
        return ((VelocityCommandIssuer) issuer).getIssuer();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public File getDataFolder() {
        return dataDir.toFile();
    }

    @Nullable
    @Override
    public LimboIntegration<RegisteredServer> getLimboIntegration() {
        if (pluginPresent("nanolimbovelocity") && limboIntegration == null) {
            limboIntegration = new VelocityNanoLimboIntegration(server,
                    getConfiguration().get(ConfigurationKeys.LIMBO_PORT_RANGE));
        }
        return limboIntegration;
    }


}
