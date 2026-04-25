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

package org.cosinus.swing.file.windows;

import org.cosinus.swing.exec.CommandProcess;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.DiskMonitorController;
import org.cosinus.swing.file.api.DiskEvent;
import org.cosinus.swing.file.api.DiskEventType;
import org.cosinus.swing.file.api.DiskMonitor;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.file.api.DiskEventType.*;

public class WindowsDiskMonitor implements DiskMonitor {

    private final ProcessExecutor processExecutor;

    private CommandProcess process;

    public WindowsDiskMonitor(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    @Async
    public void start(final DiskMonitorController controller) throws IOException {
        String script = "Register-WmiEvent -Query \"SELECT * FROM Win32_VolumeChangeEvent\"";

        process = processExecutor.startProcess("powershell", "-Command", script);
        process.monitorOutput(
            line -> {
                DiskEventType eventType = toEventType(line);
                String device = extractDevice(line);
                if (eventType != null && device != null) {
                    controller.fireDiskEvent(new DiskEvent(eventType, device));
                }
            });
    }

    private DiskEventType toEventType(String line) {
        return line.contains("EventType = 2") ?
            ADDED :
            line.contains("EventType = 3") ?
                REMOVED :
                null;
    }

    private String extractDevice(String line) {
        int index = line.indexOf(":");
        return index > 0 ? line.substring(index - 1, index + 1) : null;
    }

    @Override
    public void close() throws Exception {
        ofNullable(process)
            .ifPresent(CommandProcess::destroy);
    }
}