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

import org.cosinus.swing.ui.ApplicationUIHandler;

import java.util.Optional;

import static org.cosinus.swing.util.WindowsUtils.getRegistryBooleanValue;

public class WindowsUIThemeProvider implements UIThemeProvider {

    private static final String PERSONALIZE_REGISTRY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    private static final String APPS_USE_LIGHT_THEME = "AppsUseLightTheme";

    private final ApplicationUIHandler uiHandler;

    public WindowsUIThemeProvider(final ApplicationUIHandler uiHandler) {
        this.uiHandler = uiHandler;
    }

    @Override
    public UIThemeChecksum getUIThemeChecksum() {
        return new UIThemeChecksum();
    }

    @Override
    public Optional<String> getUITheme() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getIconTheme() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getDefaultLookAndFeel() {
        return uiHandler.findLookAndFeelByName("Windows");
    }

    @Override
    public boolean isDarkOsTheme() {
        return !getRegistryBooleanValue(PERSONALIZE_REGISTRY, APPS_USE_LIGHT_THEME);
    }
}
