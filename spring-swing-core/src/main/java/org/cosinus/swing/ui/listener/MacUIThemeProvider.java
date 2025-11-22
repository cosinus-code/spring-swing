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

package org.cosinus.swing.ui.listener;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.exec.ProcessExecutor;

import java.util.Optional;

import static java.util.Optional.empty;

@Slf4j
public class MacUIThemeProvider implements UIThemeProvider {

    private final ProcessExecutor processExecutor;

    public MacUIThemeProvider(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public UIThemeChecksum getUIThemeChecksum() {
        UIThemeChecksum uiThemeChecksum = new UIThemeChecksum();
        uiThemeChecksum.setUIThemeChecksum(getUITheme());
        uiThemeChecksum.setIconThemeChecksum(getUITheme());
        return uiThemeChecksum;
    }

    @Override
    public Optional<String> getUITheme() {
        try {
            return processExecutor.executeAndGetOutput("defaults", "read", "-g", "AppleInterfaceStyle")
                .map(UIThemeChecksum::cleanup);
        } catch (Exception ex) {
            log.debug("Failed to get the ui theme: {}", ex.getMessage());
            return empty();
        }
    }

    @Override
    public Optional<String> getIconTheme() {
        return empty();
    }
}
