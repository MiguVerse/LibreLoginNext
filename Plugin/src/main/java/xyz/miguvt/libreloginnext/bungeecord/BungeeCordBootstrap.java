/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.bungeecord;

import net.byteflux.libby.BungeeLibraryManager;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import xyz.miguvt.libreloginnext.api.provider.LibreLoginNextProvider;
import xyz.miguvt.libreloginnext.api.util.SemanticVersion;

public class BungeeCordBootstrap extends Plugin implements LibreLoginNextProvider<ProxiedPlayer, ServerInfo> {

    private BungeeCordLibreLoginNext libreLogin;

    @Override
    public void onLoad() {
        var libraryManager = new BungeeLibraryManager(this);

        getLogger().info("Loading libraries...");

        libraryManager.configureFromJSON();

        libreLogin = new BungeeCordLibreLoginNext(this);
    }

    @Override
    public void onEnable() {
        libreLogin.enable();
    }

    @Override
    public void onDisable() {
        libreLogin.disable();
    }

    @Override
    public BungeeCordLibreLoginNext getLibreLoginNext() {
        return libreLogin;
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
