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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class LinuxUIThemeProvider implements UIThemeProvider {

    private static final String DEFAULT_GNOME_ICON_THEME = "Default";

    private static final String GNOME_ICON_THEME_NAME_PROPERTY = "gnome.Net/IconThemeName";

    private static final String GNOME_DESKTOP_INTERFACE_SCHEMA = "org.gnome.desktop.interface";

    private static final String CINAMON_THEME_SCHEMA = "org.cinnamon.theme";

    private final ProcessExecutor processExecutor;

    public LinuxUIThemeProvider(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    @Override
    public UIThemeChecksum getUITheme() {
        UIThemeChecksum uiTheme = new UIThemeChecksum();

        String gnomeTheme = getGnomeTheme().orElse(null);
        String gnomeIconTheme = getGnomeIconTheme().orElse(null);
        String gnomeCursorTheme = getGnomeCursorTheme().orElse(null);
        String cinamonTheme = getCinamonSetting("name").orElse(null);

        uiTheme.setUiThemeChecksum(buildChecksum(gnomeTheme, cinamonTheme));
        uiTheme.setIconThemeChecksum(buildChecksum(gnomeIconTheme, cinamonTheme));
        uiTheme.setCursorThemeChecksum(buildChecksum(gnomeCursorTheme));

        return uiTheme;
    }

    protected String buildChecksum(String... pieces) {
        return stream(pieces)
            .filter(Objects::nonNull)
            .map(piece -> piece.replaceAll("(\\'|\\r|\\n)", ""))
            .collect(Collectors.joining("|"));
    }

    /**
     * Check if the current theme of the Operating System is dark.
     *
     * @return true if the current OS theme is dark
     */
    @Override
    public boolean isDarkOsTheme() {
        return getGnomeTheme()
            .map(theme -> theme.contains(OS_DARK_THEME))
            .orElse(false);
    }

    public Optional<String> getGnomeTheme() {
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
            return processExecutor.executeAndGetOutput("gsettings", "get", schema, name);
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
}
