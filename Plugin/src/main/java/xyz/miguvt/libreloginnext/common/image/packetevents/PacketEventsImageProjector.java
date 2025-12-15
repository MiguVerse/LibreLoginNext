/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.image.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTInt;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHeldItemChange;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.common.image.AuthenticImageProjector;

import java.awt.image.BufferedImage;
import java.util.Collections;

/**
 * PacketEvents-based image projector for displaying QR codes to players.
 * <p>
 * This implementation uses PacketEvents to send map data packets to players,
 * allowing the display of images (primarily QR codes) on maps in their hotbar.
 * </p>
 * <b>This implementation only really renders pure black and everything else as transparent.
 * Shouldn't be used for anything else than a QR code.</b>
 *
 * @param <P> The player type
 * @param <S> The server type
 * @author miguvt
 */
public class PacketEventsImageProjector<P, S> extends AuthenticImageProjector<P, S> {

    // Minimum Minecraft version supported (1.13)
    private static final ClientVersion MIN_VERSION = ClientVersion.V_1_13;
    // Maximum Minecraft version supported (1.21.11 - protocol 774)
    // TODO: When upgrading PacketEvents, check if newer Minecraft versions are supported and update MAX_VERSION accordingly.
    // See: https://github.com/retrooper/packetevents#supported-versions
    private static final ClientVersion MAX_VERSION = ClientVersion.V_1_21_11; // Latest supported in PacketEvents
    
    // Map image size (128x128 is standard Minecraft map size)
    private static final int MAP_SIZE = 128;
    
    // Map color for black pixels (QR code foreground)
    private static final byte COLOR_BLACK = 116;
    // Map color for white/transparent pixels (QR code background)  
    private static final byte COLOR_WHITE = 56;
    
    // Hotbar slot for the map (slot 36 = first hotbar slot in inventory)
    private static final int HOTBAR_SLOT = 36;
    
    public PacketEventsImageProjector(AuthenticLibreLoginNext<P, S> plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        // No additional initialization needed for PacketEvents
        // PacketEvents should already be initialized by the platform
    }

    /**
     * Projects an image (QR code) to the player by sending map packets.
     * <p>
     * The process involves:
     * 1. Creating a filled map item and placing it in the player's hotbar
     * 2. Switching the player's held item to the map slot
     * 3. Sending the map data with the QR code pixels
     * </p>
     *
     * @param image  The image to render (should be a QR code)
     * @param player The player to render the image to
     */
    @Override
    public void project(BufferedImage image, P player) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        
        if (user == null) {
            plugin.getLogger().warn("Could not find PacketEvents user for player: " + player);
            return;
        }
        
        var clientVersion = user.getClientVersion();
        
        // Create the filled map item
        ItemStack mapItem = createMapItem(clientVersion);
        
        // Send the map item to the player's hotbar (slot 36 = first hotbar slot)
        sendSetSlot(user, HOTBAR_SLOT, mapItem);
        
        // Switch the player's held item to the first hotbar slot
        sendHeldItemChange(user, 0);
        
        // Resize image if necessary
        BufferedImage processedImage = ensureMapSize(image);
        
        // Convert image to map data
        byte[] mapData = convertToMapData(processedImage);
        
        // Send the map data packet
        sendMapData(user, mapData);
    }

    @Override
    public boolean canProject(P player) {
        var user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        
        if (user == null) {
            return false;
        }
        
        var clientVersion = user.getClientVersion();
        
        // Check if client version is within supported range
        return clientVersion.isNewerThanOrEquals(MIN_VERSION) 
            && clientVersion.isOlderThanOrEquals(MAX_VERSION);
    }
    
    /**
     * Creates a filled map item stack with appropriate NBT data for the client version.
     *
     * @param clientVersion The client version
     * @return The created ItemStack
     */
    private ItemStack createMapItem(ClientVersion clientVersion) {
        ItemStack.Builder builder = ItemStack.builder()
            .type(ItemTypes.FILLED_MAP)
            .amount(1)
            .version(clientVersion);
        
        // For 1.20.5+, use components (MAP_ID component)
        if (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_20_5)) {
            builder.component(ComponentTypes.MAP_ID, 0);
        }
        // For 1.17 to 1.20.4, use NBT
        else if (clientVersion.isNewerThanOrEquals(ClientVersion.V_1_17)) {
            NBTCompound nbt = new NBTCompound();
            nbt.setTag("map", new NBTInt(0));
            builder.nbt(nbt);
        }
        
        return builder.build();
    }
    
    /**
     * Sends a SetSlot packet to place an item in the player's inventory.
     *
     * @param user The user to send the packet to
     * @param slot The slot index
     * @param item The item to place
     */
    private void sendSetSlot(User user, int slot, ItemStack item) {
        WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(
            0,      // Window ID (0 = player inventory)
            0,      // State ID
            slot,   // Slot
            item    // Item
        );
        user.sendPacket(packet);
    }
    
    /**
     * Sends a HeldItemChange packet to switch the player's selected hotbar slot.
     *
     * @param user The user to send the packet to
     * @param slot The hotbar slot (0-8)
     */
    private void sendHeldItemChange(User user, int slot) {
        WrapperPlayServerHeldItemChange packet = new WrapperPlayServerHeldItemChange(slot);
        user.sendPacket(packet);
    }
    
    /**
     * Ensures the image is the correct size for a Minecraft map (128x128).
     *
     * @param image The source image
     * @return The resized image if necessary, or the original if already correct size
     */
    private BufferedImage ensureMapSize(BufferedImage image) {
        if (image.getWidth() == MAP_SIZE && image.getHeight() == MAP_SIZE) {
            return image;
        }
        
        // Use TYPE_INT_RGB to avoid alpha channel issues during resize
        BufferedImage resized = new BufferedImage(MAP_SIZE, MAP_SIZE, BufferedImage.TYPE_INT_RGB);
        var graphics = resized.createGraphics();
        // Fill with white background first (in case of transparency)
        graphics.setColor(java.awt.Color.WHITE);
        graphics.fillRect(0, 0, MAP_SIZE, MAP_SIZE);
        graphics.drawImage(image, 0, 0, MAP_SIZE, MAP_SIZE, 0, 0, image.getWidth(), image.getHeight(), null);
        graphics.dispose();
        
        return resized;
    }
    
    /**
     * Converts a BufferedImage to Minecraft map color data.
     * <p>
     * This implementation treats dark pixels as the foreground (black)
     * and light pixels as background (white), suitable for QR codes.
     * Uses brightness threshold instead of exact color matching for robustness.
     * </p>
     *
     * @param image The image to convert
     * @return The map color data array
     */
    private byte[] convertToMapData(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        byte[] data = new byte[pixels.length];
        
        for (int i = 0; i < pixels.length; i++) {
            // Extract RGB components and calculate brightness
            int pixel = pixels[i];
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            
            // Calculate perceived brightness (luminance)
            // Using standard luminance formula: 0.299*R + 0.587*G + 0.114*B
            int brightness = (299 * r + 587 * g + 114 * b) / 1000;
            
            // If brightness is below threshold (128 = middle), treat as black
            data[i] = (brightness < 128) ? COLOR_BLACK : COLOR_WHITE;
        }
        
        return data;
    }
    
    /**
     * Sends the map data packet to display the QR code on the map.
     *
     * @param user The user to send the packet to
     * @param data The map color data
     */
    private void sendMapData(User user, byte[] data) {
        // Create the map data packet using PacketEvents wrapper
        WrapperPlayServerMapData packet = new WrapperPlayServerMapData(
            0,                          // Map ID
            (byte) 0,                   // Scale (0 = fully zoomed in)
            false,                      // Tracking position
            false,                      // Locked
            Collections.emptyList(),    // Decorations (none)
            MAP_SIZE,                   // Columns
            MAP_SIZE,                   // Rows  
            0,                          // X offset
            0,                          // Z offset
            data                        // Color data
        );
        
        user.sendPacket(packet);
    }
}
