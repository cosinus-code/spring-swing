/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.file;

import lombok.Getter;
import org.cosinus.swing.boot.cleanup.ApplicationShutDown;
import org.cosinus.swing.file.api.DiskEvent;
import org.cosinus.swing.file.api.DiskEventListener;
import org.cosinus.swing.file.api.DiskMonitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DiskMonitorController {

    private final ApplicationShutDown applicationShutDown;

    private final DiskMonitor diskMonitor;

    private final List<DiskEventListener> listeners;

    @Getter
    private final long expirationLimitInMillis;

    private final Map<String, Long> recentEvents = new ConcurrentHashMap<>();

    public DiskMonitorController(final ApplicationShutDown applicationShutDown,
                                 final DiskMonitor diskMonitor,
                                 final long expirationLimitInMillis) {
        this.applicationShutDown = applicationShutDown;
        this.diskMonitor = diskMonitor;
        this.listeners = new ArrayList<>();
        this.expirationLimitInMillis = expirationLimitInMillis;
    }

    public void start() {
        try {
            diskMonitor.start(this);
            applicationShutDown.register(diskMonitor);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public void register(final DiskEventListener listener) {
        this.listeners.add(listener);
    }

    public void unregister(final DiskEventListener listener) {
        this.listeners.remove(listener);
    }

    public void fireDiskEvent(final DiskEvent event) {
        long now = System.currentTimeMillis();
        String key = event.getKey();

        Long lastEventTime = recentEvents.get(key);
        if (lastEventTime == null || (now - lastEventTime) > getExpirationLimitInMillis()) {
            recentEvents.put(key, now);
            listeners.forEach(listener -> listener.onEvent(event));
            cleanup(now);
        }
    }

    private void cleanup(long now) {
        recentEvents.values().removeIf(eventTime ->
            (now - eventTime) > getExpirationLimitInMillis() * 2);
    }
}
