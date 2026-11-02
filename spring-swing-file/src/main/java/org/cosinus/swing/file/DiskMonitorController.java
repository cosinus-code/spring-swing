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
import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.boot.cleanup.ApplicationShutDown;
import org.cosinus.swing.file.api.DiskEvent;
import org.cosinus.swing.file.api.DiskEventListener;
import org.cosinus.swing.file.api.DiskMonitor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Controller for the disk events.
 * <p>
 * Plugging or unplugging a disk produces a burst of low level events
 * (one for the disk, one for each partition, followed by "change" events, etc.).
 * To avoid notifying the listeners for each of them,
 * the events are debounced: the listeners are notified only once,
 * with the last received event, after no other event was received
 * for {@link #getExpirationLimitInMillis()} milliseconds.
 */
@Slf4j
public class DiskMonitorController implements AutoCloseable {

    private final ApplicationShutDown applicationShutDown;

    private final DiskMonitor diskMonitor;

    private final List<DiskEventListener> listeners;

    @Getter
    private final long expirationLimitInMillis;

    private ScheduledFuture<?> pendingNotification;

    private final AtomicBoolean started = new AtomicBoolean();

    private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "disk-event-dispatcher");
        thread.setDaemon(true);
        return thread;
    });

    public DiskMonitorController(final ApplicationShutDown applicationShutDown,
                                 final DiskMonitor diskMonitor,
                                 final long expirationLimitInMillis) {
        this.applicationShutDown = applicationShutDown;
        this.diskMonitor = diskMonitor;
        this.listeners = new CopyOnWriteArrayList<>();
        this.expirationLimitInMillis = expirationLimitInMillis;
    }

    /**
     * Start the disk monitor. Only the first call has effect,
     * so that a single native monitoring process is started.
     */
    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        try {
            diskMonitor.start(this);
            applicationShutDown.register(diskMonitor);
            applicationShutDown.register(this);
        } catch (IOException ex) {
            started.set(false);
            throw new UncheckedIOException(ex);
        }
    }

    public void register(final DiskEventListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void unregister(final DiskEventListener listener) {
        this.listeners.remove(listener);
    }

    public synchronized void fireDiskEvent(final DiskEvent event) {
        if (pendingNotification != null) {
            pendingNotification.cancel(false);
        }
        pendingNotification = scheduler.schedule(
            () -> notifyListeners(event),
            expirationLimitInMillis,
            MILLISECONDS);
    }

    private void notifyListeners(final DiskEvent event) {
        listeners.forEach(listener -> {
            try {
                listener.onDiskEvent(event);
            } catch (RuntimeException ex) {
                log.error("Disk event listener failed for event {}", event.getKey(), ex);
            }
        });
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
