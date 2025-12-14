/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.command.commands.premium;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.kyori.adventure.audience.Audience;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

@CommandAlias("premiumconfirm|confirmpremium")
public class PremiumConfirmCommand<P> extends PremiumCommand<P> {
    public PremiumConfirmCommand(AuthenticLibreLoginNext<P, ?> plugin) {
        super(plugin);
    }

    @Default
    public CompletionStage<Void> onPremiumConfirm(Audience sender, UUID uuid) {
        return runAsync(() -> {
            var player = getPlayer(uuid);
            var user = getUser(uuid);
            checkCracked(user);

            plugin.getCommandProvider().onConfirm(player, sender, user);
        });
    }

}
