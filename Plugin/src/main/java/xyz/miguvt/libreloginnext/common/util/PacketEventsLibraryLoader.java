/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.util;

import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;

public final class PacketEventsLibraryLoader {

    private static final String PACKET_EVENTS_VERSION = "2.13.0";

    private PacketEventsLibraryLoader() {
    }

    public static void loadPlatform(LibraryManager libraryManager, String artifactId) {
        libraryManager.loadLibrary(Library.builder()
                .groupId("com.github.retrooper")
                .artifactId(artifactId)
                .version(PACKET_EVENTS_VERSION)
                .relocate("com{}github{}retrooper{}packetevents", "xyz{}miguvt{}libreloginnext{}lib{}packetevents{}api")
                .relocate("io{}github{}retrooper{}packetevents", "xyz{}miguvt{}libreloginnext{}lib{}packetevents{}platform")
                .build());
    }
}
