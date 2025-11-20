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

import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;

import java.util.Optional;

public class MacUIThemeProvider implements UIThemeProvider {

    private final ProcessExecutor processExecutor;

    public MacUIThemeProvider(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public UIThemeChecksum getUITheme() {
        return null;
    }

    @Override
    public boolean isDarkOsTheme() {
        try {
            return processExecutor.executeAndGetOutput("defaults", "read", "-g", "AppleInterfaceStyle")
                .map(theme -> theme.contains(OS_DARK_THEME))
                .orElse(false);
        } catch (ProcessExecutionException e) {
            return false;
        }
    }

    @Override
    public Optional<String> getIconTheme() {
        return Optional.empty();
    }
}
