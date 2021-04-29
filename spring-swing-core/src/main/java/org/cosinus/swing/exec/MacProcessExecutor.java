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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;

/**
 * Implementation of {@link ProcessExecutor} for Windows
 */
public class MacProcessExecutor implements ProcessExecutor {

    private static final Logger LOG = LogManager.getLogger(MacProcessExecutor.class);

    @Override
    public void executeFile(File file) {
        execute(file.getParentFile(),
                "open", file.getName());
    }

    @Override
    public Optional<String> getOsTheme() {
        return executeAndGetOutput("defaults", "read", "-g", "AppleInterfaceStyle");
    }

    @Override
    public Logger logger() {
        return LOG;
    }
}
