/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.bungeecord;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder;
import net.byteflux.libby.BungeeLibraryManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.miguvt.libreloginnext.api.provider.LibreLoginNextProvider;
import xyz.miguvt.libreloginnext.api.util.SemanticVersion;

public class BungeeCordBootstrap extends Plugin implements LibreLoginNextProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordLibreLoginNext libreLoginNext;

    @Override
    public void onLoad() {
        var libraryManager = new BungeeLibraryManager(this);

        getLogger().info("Loading libraries...");

        libraryManager.configureFromJSON();

        // Initialize PacketEvents after libraries are loaded
        PacketEvents.setAPI(BungeePacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false);
        PacketEvents.getAPI().load();

        libreLoginNext = new BungeeCordLibreLoginNext(this);
    }

    @Override
    public void onEnable() {
        // Initialize PacketEvents
        PacketEvents.getAPI().init();

        libreLoginNext.enable();
    }

    @Override
    public void onDisable() {
        libreLoginNext.disable();
        PacketEvents.getAPI().terminate();
    }

    @Override
    public BungeeCordLibreLoginNext getLibreLoginNext() {
        return libreLoginNext;
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public SemanticVersion getParsedVersion() {
        return SemanticVersion.parse(getVersion());
    }

}
