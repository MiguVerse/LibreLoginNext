/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import net.byteflux.libby.VelocityLibraryManager;
import xyz.miguvt.libreloginnext.api.LibreLoginNextPlugin;
import xyz.miguvt.libreloginnext.api.provider.LibreLoginNextProvider;
import xyz.miguvt.libreloginnext.api.util.SemanticVersion;
import xyz.miguvt.libreloginnext.common.BuildConstants;

import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(
        id = "libreloginnext",
        name = "LibreLoginNext",
        version = BuildConstants.VERSION,
        authors = {"miguvt", "kyngs"},
        dependencies = {
                @Dependency(id = "floodgate", optional = true),
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "redisbungee", optional = true),
                @Dependency(id = "nanolimbovelocity", optional = true)
        }
)
public class VelocityBootstrap implements LibreLoginNextProvider<Player, RegisteredServer> {

    @Inject
    PluginDescription pluginDescription;

    ProxyServer server;
    PluginContainer container;
    Logger logger;
    Path dataDirectory;
    private final VelocityLibreLoginNext libreLoginNext;

    @Inject
    public VelocityBootstrap(ProxyServer server, Injector injector, Logger logger, PluginContainer container, @com.velocitypowered.api.plugin.annotation.DataDirectory Path dataDirectory) {
        this.server = server;
        this.container = container;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        // This is a very ugly hack to be able to load libraries in the constructor
        // We cannot pass this as a parameter to the constructor because the plugin is technically still not loaded
        // And, we cannot past the container as a parameter to the constructor because the proxy still did not assign the instance to it.
        // So, we have to "mock" the container and pass this as the instance. I'm kinda surprised this works, but in theory could break in the future.
        var libraryManager = new VelocityLibraryManager<>(logger, Path.of("plugins", "libreloginnext"), server.getPluginManager(), new PluginContainer() {
            @Override
            public PluginDescription getDescription() {
                return container.getDescription();
            }

            @Override
            public Optional<?> getInstance() {
                return Optional.of(this);
            }

            @Override
            public ExecutorService getExecutorService() {
                return Executors.newSingleThreadExecutor();
            }
        });

        logger.info("Loading libraries...");

        libraryManager.configureFromJSON();

        // Initialize PacketEvents after libraries are loaded
        PacketEvents.setAPI(VelocityPacketEventsBuilder.build(server, container, logger, dataDirectory));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false);
        PacketEvents.getAPI().load();

        libreLoginNext = new VelocityLibreLoginNext(this);
        injector.injectMembers(libreLoginNext);
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        // Initialize PacketEvents
        PacketEvents.getAPI().init();

        libreLoginNext.enable();

        server.getEventManager().register(this, new Blockers(libreLoginNext.getAuthorizationProvider(), libreLoginNext.getConfiguration(), libreLoginNext.getMessages()));
        server.getEventManager().register(this, new VelocityListeners(libreLoginNext));
    }

    @Override
    public LibreLoginNextPlugin<Player, RegisteredServer> getLibreLoginNext() {
        return libreLoginNext;
    }

    @Override
    public String getVersion() {
        return pluginDescription.getVersion().orElse(null);
    }

    @Override
    public SemanticVersion getParsedVersion() {
        var version = getVersion();
        return version == null ? null : SemanticVersion.parse(version);
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        libreLoginNext.disable();
        PacketEvents.getAPI().terminate();
    }
}
