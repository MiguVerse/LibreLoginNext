/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.miguvt.libreloginnext.common.event;

import xyz.miguvt.libreloginnext.common.AuthenticHandler;
import xyz.miguvt.libreloginnext.common.AuthenticLibreLoginNext;
import xyz.miguvt.libreloginnext.api.event.Event;
import xyz.miguvt.libreloginnext.api.event.EventProvider;
import xyz.miguvt.libreloginnext.api.event.EventType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AuthenticEventProvider<P, S> extends AuthenticHandler<P, S> implements EventProvider<P, S> {

    private final Map<EventType<P, S, ?>, Set<Consumer<Event<P, S>>>> listeners;

    public AuthenticEventProvider(AuthenticLibreLoginNext<P, S> plugin) {
        super(plugin);
        this.listeners = new ConcurrentHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked") // Type safety is guaranteed by EventType parameter matching
    public <E extends Event<P, S>> Consumer<E> subscribe(EventType<P, S, E> type, Consumer<E> handler) {
        listeners.computeIfAbsent(type, x -> new HashSet<>()).add((Consumer<Event<P, S>>) handler);
        return handler;
    }

    @Override
    public void unsubscribe(Consumer<? extends Event<P, S>> handler) {
        listeners.values().forEach(x -> x.remove(handler));
    }

    @Override
    public <E extends Event<P, S>> void fire(EventType<P, S, E> type, E event) {
        var set = listeners.get(type);

        if (set == null || set.isEmpty()) return;

        for (Consumer<Event<P, S>> consumer : set) {
            consumer.accept(event);
        }
    }

    @SuppressWarnings("unchecked") // This method is intentionally unsafe - used for cross-type event firing
    public void unsafeFire(EventType<?, ?, ?> type, Event<?, ?> event) {
        var set = listeners.get(type);

        if (set == null || set.isEmpty()) return;

        for (Consumer<Event<P, S>> consumer : set) {
            consumer.accept((Event<P, S>) event);
        }
    }
}
