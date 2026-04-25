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

package org.cosinus.swing.file.config;

import org.cosinus.swing.boot.cleanup.ApplicationShutDown;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.DefaultDiskMonitor;
import org.cosinus.swing.file.DiskEventListenerInitializer;
import org.cosinus.swing.file.DiskMonitorController;
import org.cosinus.swing.file.api.DiskMonitor;
import org.cosinus.swing.file.linux.LinuxDiskMonitor;
import org.cosinus.swing.file.mac.MacDiskMonitor;
import org.cosinus.swing.file.windows.WindowsDiskMonitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@AutoConfiguration
@ConditionalOnListeningDiskEvents
public class SpringSwingFileDiskEventAutoConfiguration {

    @Bean
    @ConditionalOnLinux
    public DiskMonitor linuxDiskMonitor(final ProcessExecutor processExecutor) {
        return new LinuxDiskMonitor(processExecutor);
    }

    @Bean
    @ConditionalOnMac
    public DiskMonitor macDiskMonitor(final ProcessExecutor processExecutor) {
        return new MacDiskMonitor(processExecutor);
    }

    @Bean
    @ConditionalOnWindows
    public DiskMonitor windowsDiskMonitor(final ProcessExecutor processExecutor) {
        return new WindowsDiskMonitor(processExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public DiskMonitor defaultDiskMonitor() {
        return new DefaultDiskMonitor();
    }

    @Bean
    public DiskMonitorController diskMonitorController(
        final ApplicationShutDown applicationShutDown,
        final DiskMonitor diskMonitor,
        @Value("${file.disk.event.listener.expirationLimit:2000}") long expirationLimit) {
        DiskMonitorController controller = new DiskMonitorController(applicationShutDown, diskMonitor, expirationLimit);
        controller.start();
        return controller;
    }

    @Bean
    public DiskEventListenerInitializer diskEventListenerInitializer(
        final DiskMonitorController diskMonitorController) {
        return new DiskEventListenerInitializer(diskMonitorController);
    }
}
