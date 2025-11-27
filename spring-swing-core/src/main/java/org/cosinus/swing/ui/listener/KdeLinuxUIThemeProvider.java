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
import org.cosinus.swing.exec.Command;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.IconTheme;

import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
public class KdeLinuxUIThemeProvider implements UIThemeProvider {

    private static final String KDE_GLOBALS = "kdeglobals";

    private static final String KDE_INPUT = "kcminputrc";

    private static final String KDE_PLASMA = "plasmarc";

    private static final String KDE_READ_CONFIG = "kreadconfig";

    private static final String KDE_READ_CONFIG_FILE = "--file";

    private static final String KDE_READ_CONFIG_GROUP = "--group";

    private static final String KDE_READ_CONFIG_KEY = "--key";

    public static final IconTheme ICON_THEME_BREEZE = new IconTheme("breeze", "breeze-dark");

    public static final Stream<IconTheme> MAIN_ICON_THEMES = Stream.of(ICON_THEME_BREEZE);

    private final ProcessExecutor processExecutor;

    private final ApplicationUIHandler uiHandler;

    private String kdeReadConfig;

    public KdeLinuxUIThemeProvider(final ProcessExecutor processExecutor,
                                   final ApplicationUIHandler uiHandler) {
        this.processExecutor = processExecutor;
        this.uiHandler = uiHandler;
    }

    @Override
    public UIThemeChecksum getUIThemeChecksum() {
        UIThemeChecksum uiThemeChecksum = new UIThemeChecksum();

        String kdeTheme = getUITheme().orElse(null);
        String iconTheme = getIconTheme().orElse(null);
        Optional<String> widgetStyle = getKdeGlobalSetting("KDE", "widgetStyle");
        Optional<String> colorScheme = getKdeGlobalSetting("General", "ColorSchemeHash");
        Optional<String> plasmaTheme = getKdePlasmaSetting("Theme", "name");
        Optional<String> mouseCursorTheme = getKdeInputSetting("Mouse", "cursorTheme");
        Optional<String> cursorThemeName = getKdeInputSetting("CursorTheme", "name");

        uiThemeChecksum.setUIThemeChecksum(kdeTheme, widgetStyle, plasmaTheme, colorScheme, iconTheme);
        uiThemeChecksum.setIconThemeChecksum(kdeTheme, iconTheme);
        uiThemeChecksum.setCursorThemeChecksum(mouseCursorTheme, cursorThemeName);

        return uiThemeChecksum;
    }

    @Override
    public Optional<String> getUITheme() {
        return getKdeGlobalSetting("KDE", "LookAndFeelPackage");
    }

    @Override
    public Optional<String> getIconTheme() {
        return getKdeGlobalSetting("Icons", "Theme");
    }

    @Override
    public Optional<String> getDefaultLookAndFeel() {
        return uiHandler.findLookAndFeelByName("GTK");
    }

    @Override
    public Stream<IconTheme> getMainIconThemes() {
        return MAIN_ICON_THEMES;
    }

    protected Optional<String> getKdeGlobalSetting(String groupName, String keyName) {
        return getKdeSetting(KDE_GLOBALS, groupName, keyName);
    }

    protected Optional<String> getKdePlasmaSetting(String groupName, String keyName) {
        return getKdeSetting(KDE_PLASMA, groupName, keyName);
    }

    protected Optional<String> getKdeInputSetting(String groupName, String keyName) {
        return getKdeSetting(KDE_INPUT, groupName, keyName);
    }

    protected Optional<String> getKdeSetting(String configFile, String groupName, String keyName) {
        return processExecutor.executeAndGetOutput(
                kdeReadConfig(),
                KDE_READ_CONFIG_FILE, configFile,
                KDE_READ_CONFIG_GROUP, groupName,
                KDE_READ_CONFIG_KEY, keyName)
            .map(UIThemeChecksum::cleanup);
    }

    protected String kdeReadConfig() {
        if (kdeReadConfig == null) {
            try {
                kdeReadConfig = processExecutor.executePipelineAndGetOutput(
                        Command.of("plasmashell", "--version"),
                        Command.of("grep", "-oE", "[0-9]+"))
                    .map(output -> output.split("\\n")[0])
                    .map(KDE_READ_CONFIG::concat)
                    .orElse(KDE_READ_CONFIG);
            } catch (Exception ex) {
                log.warn("Failed to find KDE version", ex);
                kdeReadConfig = KDE_READ_CONFIG;
            }
        }

        return kdeReadConfig;
    }
}
