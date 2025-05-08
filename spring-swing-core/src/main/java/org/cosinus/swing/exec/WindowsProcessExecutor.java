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

import java.io.File;
import java.util.Optional;

import static org.cosinus.swing.ui.ApplicationUIHandler.OS_DARK_THEME;
import static org.cosinus.swing.ui.ApplicationUIHandler.OS_LIGHT_THEME;
import static org.cosinus.swing.util.WindowsUtils.getRegistryBooleanValue;

/**
 * Implementation of {@link ProcessExecutor} for Windows
 */
public class WindowsProcessExecutor implements ProcessExecutor {

    private static final String PERSONALIZE_REGISTRY = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    private static final String APPS_USE_LIGHT_THEME = "AppsUseLightTheme";

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
    public Optional<String> executeWithPrivilegesAndGetOutput(String... command) {
        //TODO
        return Optional.empty();
    }

    @Override
    public Optional<String> executePipelineWithPrivilegesAndGetOutput(String[]... commands) {
        return Optional.empty();
    }

    @Override
    public void executeWithPrivileges(String... command) {
    }

}
