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

package org.cosinus.swing.exec;

import java.io.File;
import java.util.Optional;
import org.cosinus.swing.error.ProcessExecutionException;

/**
 * Implementation of {@link ProcessExecutor} for Mac
 */
public class MacProcessExecutor implements ProcessExecutor {

    @Override
    public void executeFile(File file) {
        execute(false, file.getParentFile(), false, "open", "-a", file.getName());
    }

    @Override
    public Optional<String> getOsTheme() {
        try {
            return executeAndGetOutput("defaults", "read", "-g", "AppleInterfaceStyle");
        } catch (ProcessExecutionException e) {
            return Optional.empty();
        }
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
