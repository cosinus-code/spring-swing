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

import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.ui.ApplicationUIHandler;

import java.util.Optional;

public class XfceUIThemeProvider implements UIThemeProvider {

    private final ProcessExecutor processExecutor;

    private final ApplicationUIHandler uiHandler;

    public XfceUIThemeProvider(final ProcessExecutor processExecutor,
                               final ApplicationUIHandler uiHandler) {
        this.processExecutor = processExecutor;
        this.uiHandler = uiHandler;
    }

    @Override
    public UIThemeChecksum getUIThemeChecksum() {
        UIThemeChecksum uiThemeChecksum = new UIThemeChecksum();
        uiThemeChecksum.setUIThemeChecksum(getUITheme());
        uiThemeChecksum.setIconThemeChecksum(getIconTheme());
        return uiThemeChecksum;
    }

    @Override
    public Optional<String> getUITheme() {
        return getUiSetting("/Net/ThemeName");
    }

    @Override
    public Optional<String> getIconTheme() {
        return getUiSetting("/Net/IconThemeName");
    }

    @Override
    public Optional<String> getDefaultLookAndFeel() {
        return uiHandler.findLookAndFeelByName("GTK");
    }

    public Optional<String> getUiSetting(String name) {
        return processExecutor.executeAndGetOutput("xfconf-query", "-c", "xsettings", "-p", name);
    }
}
