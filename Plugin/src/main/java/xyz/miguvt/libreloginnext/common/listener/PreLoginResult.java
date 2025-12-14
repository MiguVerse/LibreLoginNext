/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.listener;

import net.kyori.adventure.text.Component;
import xyz.miguvt.libreloginnext.api.database.User;

import org.jetbrains.annotations.Nullable;

public record PreLoginResult(PreLoginState state, @Nullable Component message, User user) {
}
