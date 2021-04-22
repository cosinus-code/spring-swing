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

/**
 * Implementation of {@link ProcessExecutor} for Windows
 */
public class LinuxProcessExecutor implements ProcessExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(LinuxProcessExecutor.class);

    @Override
    public void executeFile(File file) {
//        execute(file.getParentFile(),
//                "kfmclient", "exec", file.getName());
        execute("xdg-open", file.getAbsolutePath());
    }

    @Override
    public Optional<String> getOsTheme() {
        return Optional.empty();
    }

    @Override
    public Logger logger() {
        return LOG;
    }
}
