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

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;

/**
 * Interface for a process executor
 */
public interface ProcessExecutor {

    void executeFile(File file);

    Optional<String> getOsTheme();

    Logger logger();

    default void execute(String... command) {
        execute(new File(System.getProperty("user.home")), command);
    }

    default void execute(File workingDir, String... command) {
        try {
            new ProcessBuilder(command)
                .inheritIO()
                .directory(workingDir)
                .start();
        } catch (IOException ex) {
            logger().error("Failed to run command: " + Arrays.toString(command), ex);
        }
    }

    default Optional<String> executeAndGetOutput(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = IOUtils.toString(reader);
                process.waitFor();
                return Optional.of(output);
            }
        } catch (IOException | InterruptedException ex) {
            logger().error("Failed to run command: " + Arrays.toString(command), ex);
        }
        return Optional.empty();
    }

}
