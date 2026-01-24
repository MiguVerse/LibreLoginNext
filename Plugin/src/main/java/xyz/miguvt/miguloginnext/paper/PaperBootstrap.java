/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.paper;

import xyz.miguvt.miguloginnext.api.provider.MiguLoginNextProvider;
import xyz.miguvt.miguloginnext.api.util.SemanticVersion;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperBootstrap extends JavaPlugin implements MiguLoginNextProvider<Player, World> {

    private PaperMiguLoginNext libreLoginNext;

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return id == null ?
                null
                : id.equals("void") ? new VoidWorldGenerator() : null;
    }

    @Override
    public void onLoad() {
        getLogger().info("Analyzing server setup...");

        try {
            var adventureClass = Class.forName("net.kyori.adventure.audience.Audience");

            if (!adventureClass.isAssignableFrom(Player.class)) {
                throw new ClassNotFoundException();
            }
        } catch (ClassNotFoundException e) {
            unsupportedSetup();
        }

        getLogger().info("Detected Adventure-compatible server distribution - " + getServer().getName() + " " + getServer().getVersion());

        // Libraries are now loaded automatically by GeneratedLibbyLoader during plugin initialization
        // No need for manual libby configuration here anymore

        libreLoginNext = new PaperMiguLoginNext(this);
    }

    @Override
    public void onEnable() {
        getLogger().info("Bootstrapping MiguLoginNext...");
        libreLoginNext.enable();
    }

    private void unsupportedSetup() {
        getLogger().severe("***********************************************************");

        getLogger().severe("Detected an unsupported server distribution. Please use Paper or its forks. SPIGOT IS NOT SUPPORTED!");

        getLogger().severe("***********************************************************");

        stopServer();
    }

    private void stopServer() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignored) {
        }

        System.exit(1);
    }

    @Override
    public void onDisable() {
        libreLoginNext.disable();
    }

    @Override
    public PaperMiguLoginNext getMiguLoginNext() {
        return libreLoginNext;
    }

    @Override
    public String getVersion() {
        return getPluginMeta().getVersion();
    }

    @Override
    public SemanticVersion getParsedVersion() {
        return SemanticVersion.parse(getVersion());
    }

    protected void disable() {
        setEnabled(false);
    }

}
