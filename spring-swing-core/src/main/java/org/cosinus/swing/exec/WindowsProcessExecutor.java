/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.exec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Optional;

import static org.cosinus.swing.ui.ApplicationUIHandler.OS_LIGHT_THEME;
import static org.cosinus.swing.util.WindowsUtils.getRegistryBooleanValue;
import static org.cosinus.swing.ui.ApplicationUIHandler.OS_DARK_THEME;

/**
 * Implementation of {@link ProcessExecutor} for Windows
 */
public class WindowsProcessExecutor implements ProcessExecutor {

    private static final String PERSONALIZE_REGISTRY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    private static final String APPS_USE_LIGHT_THEME = "AppsUseLightTheme";

    private static final Logger LOG = LoggerFactory.getLogger(WindowsProcessExecutor.class);

    @Override
    public void executeFile(File file) {
        execute(file.getParentFile(),
                "rundll32", "url.dll,FileProtocolHandler", file.getName());
    }

    @Override
    public Optional<String> getOsTheme() {
        return Optional.of(getRegistryBooleanValue(PERSONALIZE_REGISTRY, APPS_USE_LIGHT_THEME) ?
                                   OS_LIGHT_THEME :
                                   OS_DARK_THEME);
    }

    @Override
    public Logger logger() {
        return LOG;
    }
}
