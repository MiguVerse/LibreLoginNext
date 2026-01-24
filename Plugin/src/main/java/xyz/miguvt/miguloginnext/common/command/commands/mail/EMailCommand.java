/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.command.commands.mail;

import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.common.command.Command;
import xyz.miguvt.miguloginnext.common.mail.AuthenticEMailHandler;

public class EMailCommand<P> extends Command<P> {

    protected final AuthenticEMailHandler mailHandler;

    public EMailCommand(AuthenticMiguLoginNext<P, ?> plugin) {
        super(plugin);
        mailHandler = plugin.getEmailHandler();
        assert mailHandler != null;
    }
}
