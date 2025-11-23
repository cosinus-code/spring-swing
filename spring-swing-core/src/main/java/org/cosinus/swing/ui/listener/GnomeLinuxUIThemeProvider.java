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
import org.cosinus.swing.ui.ApplicationUIHandler;

import java.util.Optional;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class GnomeLinuxUIThemeProvider implements UIThemeProvider {

    private static final String DEFAULT_GNOME_ICON_THEME = "Default";

    private static final String GNOME_ICON_THEME_NAME_PROPERTY = "gnome.Net/IconThemeName";

    private static final String GNOME_DESKTOP_INTERFACE_SCHEMA = "org.gnome.desktop.interface";

    private static final String CINAMON_THEME_SCHEMA = "org.cinnamon.theme";

    private final ProcessExecutor processExecutor;

    private final ApplicationUIHandler uiHandler;

    public GnomeLinuxUIThemeProvider(final ProcessExecutor processExecutor,
                                     final ApplicationUIHandler uiHandler) {
        this.processExecutor = processExecutor;
        this.uiHandler = uiHandler;
    }

    @Override
    public UIThemeChecksum getUIThemeChecksum() {
        UIThemeChecksum uiTheme = new UIThemeChecksum();

        Optional<String> gnomeTheme = getUITheme();
        Optional<String> gnomeIconTheme = getGnomeIconTheme();
        Optional<String> gnomeCursorTheme = getGnomeCursorTheme();
        Optional<String> cinamonTheme = getCinamonSetting("name");
        Optional<String> iconTheme = getIconTheme();

        uiTheme.setUIThemeChecksum(gnomeTheme, cinamonTheme);
        uiTheme.setIconThemeChecksum(iconTheme, gnomeIconTheme, gnomeTheme, cinamonTheme);
        uiTheme.setCursorThemeChecksum(gnomeCursorTheme);

        return uiTheme;
    }

    @Override
    public Optional<String> getUITheme() {
        return getGnomeSetting("gtk-theme");
    }

    public Optional<String> getGnomeIconTheme() {
        return getGnomeSetting("icon-theme");
    }

    public Optional<String> getGnomeCursorTheme() {
        return getGnomeSetting("cursor-theme");
    }

    protected Optional<String> getGnomeSetting(String name) {
        return getSetting(GNOME_DESKTOP_INTERFACE_SCHEMA, name);
    }

    protected Optional<String> getCinamonSetting(String name) {
        return getSetting(CINAMON_THEME_SCHEMA, name);
    }

    protected Optional<String> getSetting(String schema, String name) {
        try {
            return processExecutor.executeAndGetOutput("gsettings", "get", schema, name)
                .map(UIThemeChecksum::cleanup);
        } catch (ProcessExecutionException ex) {
            return empty();
        }
    }

    @Override
    public Optional<String> getIconTheme() {
        return ofNullable(getDefaultToolkit().getDesktopProperty(GNOME_ICON_THEME_NAME_PROPERTY))
            .map(Object::toString)
            .or(() -> Optional.of(DEFAULT_GNOME_ICON_THEME));
    }

    @Override
    public Optional<String> getDefaultLookAndFeel() {
        return uiHandler.findLookAndFeelByName("GTK");
    }
}
