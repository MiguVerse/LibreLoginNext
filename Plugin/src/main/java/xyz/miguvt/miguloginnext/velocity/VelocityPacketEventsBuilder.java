/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.velocity;

import com.github.retrooper.packetevents.PacketEventsAPI;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Bridge class for loading the relocated VelocityPacketEventsBuilder from PacketEvents.
 * 
 * The real builder from PacketEvents is relocated during the shadowJar task:
 * io.github.retrooper.packetevents -> xyz.miguvt.miguloginnext.lib.packetevents.platform
 * 
 * This class uses reflection to call the real builder since it's not available at compile time.
 */
public class VelocityPacketEventsBuilder {
    
    private static final String RELOCATED_BUILDER_CLASS = 
        "xyz.miguvt.miguloginnext.lib.packetevents.platform.velocity.factory.VelocityPacketEventsBuilder";
    
    /**
     * Builds and initializes PacketEvents API for Velocity using the relocated builder.
     * 
     * @param server The Velocity proxy server instance
     * @param container The plugin container
     * @param logger The logger instance
     * @param dataDirectory The plugin's data directory
     * @return The initialized PacketEvents API instance
     */
    public static PacketEventsAPI<?> build(
            ProxyServer server,
            PluginContainer container,
            Logger logger,
            Path dataDirectory
    ) {
        try {
            // Load the relocated builder class using reflection
            Class<?> builderClass = Class.forName(RELOCATED_BUILDER_CLASS);
            
            // Get the build method: build(ProxyServer, PluginContainer, Logger, Path)
            Method buildMethod = builderClass.getMethod(
                "build",
                ProxyServer.class,
                PluginContainer.class,
                Logger.class,
                Path.class
            );
            
            // Invoke the method and cast to PacketEventsAPI
            PacketEventsAPI<?> api = (PacketEventsAPI<?>) buildMethod.invoke(
                null,
                server,
                container,
                logger,
                dataDirectory
            );
            
            return api;
            
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Failed to load relocated VelocityPacketEventsBuilder. " +
                "Make sure packetevents-velocity is included in the shadowJar and properly shaded.",
                e
            );
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(
                "VelocityPacketEventsBuilder.build(ProxyServer, PluginContainer, Logger, Path) method not found. " +
                "Make sure you have the correct version of PacketEvents (2.11.1+).",
                e
            );
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to initialize PacketEvents via VelocityPacketEventsBuilder: " + e.getMessage(),
                e
            );
        }
    }
}

