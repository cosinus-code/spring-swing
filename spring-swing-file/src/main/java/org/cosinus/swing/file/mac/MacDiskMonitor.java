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

package org.cosinus.swing.file.mac;

import org.cosinus.swing.exec.CommandProcess;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.DiskMonitorController;
import org.cosinus.swing.file.api.DiskEvent;
import org.cosinus.swing.file.api.DiskEventType;
import org.cosinus.swing.file.api.DiskMonitor;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.file.api.DiskEventType.MOUNTED;
import static org.cosinus.swing.file.api.DiskEventType.UNMOUNTED;

public class MacDiskMonitor implements DiskMonitor {

    private final ProcessExecutor processExecutor;

    private CommandProcess process;

    public MacDiskMonitor(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    @Async
    public void start(final DiskMonitorController controller) throws IOException {
        process = processExecutor.startProcess("diskutil", "activity");
        process.monitorOutput(
            line -> {
                DiskEventType eventType = toEventType(line);
                String device = extractDevice(line);
                if (eventType != null && device != null) {
                    controller.fireDiskEvent(new DiskEvent(eventType, device));
                }
            });
    }

    private DiskEventType toEventType(String rawEventType) {
        return rawEventType.toLowerCase().contains("mounted") ?
            MOUNTED :
            rawEventType.equals("unmounted") ?
                UNMOUNTED :
                null;
    }

    private String extractDevice(String line) {
        return stream(line.split(" "))
            .filter(token -> token.startsWith("disk"))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void close() throws Exception {
        ofNullable(process)
            .ifPresent(CommandProcess::destroy);
    }
}