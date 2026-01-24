/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.integration.luckperms;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Awaiting2FAContext<P> implements ContextCalculator<P> {

    private final AuthenticMiguLoginNext<P, ?> plugin;

    public Awaiting2FAContext(AuthenticMiguLoginNext<P, ?> plugin) {
        this.plugin = plugin;
    }

    private static final String KEY = "miguloginnext-awaiting2fa";

    @Override
    public void calculate(@NonNull P player, @NonNull ContextConsumer consumer) {
        consumer.accept(KEY, Boolean.toString(plugin.getAuthorizationProvider().isAwaiting2FA(player)));
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }
}
