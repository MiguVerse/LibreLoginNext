/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.miguloginnext.common.image;

import xyz.miguvt.miguloginnext.common.AuthenticHandler;
import xyz.miguvt.miguloginnext.common.AuthenticMiguLoginNext;
import xyz.miguvt.miguloginnext.api.image.ImageProjector;

public abstract class AuthenticImageProjector<P, S> extends AuthenticHandler<P, S> implements ImageProjector<P> {

    public AuthenticImageProjector(AuthenticMiguLoginNext<P, S> plugin) {
        super(plugin);
    }

    public abstract void enable();

}
