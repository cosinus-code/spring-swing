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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.lang.ProcessBuilder.startPipeline;

/**
 * Interface for a process executor
 */
public interface ProcessExecutor {

    Logger LOG = LogManager.getLogger(ProcessExecutor.class);

    /**
     * Execute a file.
     *
     * @param file the file to execute
     */
    void executeFile(File file);

    /**
     * Get the current OS theme.
     *
     * @return the current OS theme
     */
    Optional<String> getOsTheme();

    /**
     * Execute a command on user home working directory.
     *
     * @param command the command to execute
     */
    default void execute(String... command) {
        execute(new File(System.getProperty("user.home")), command);
    }

    /**
     * Execute a command in given working directory.
     *
     * @param workingDir the working directory
     * @param command the command to execute
     */
    default void execute(File workingDir, String... command) {
        try {
            new ProcessBuilder(command)
                .inheritIO()
                .directory(workingDir)
                .start();
        } catch (IOException ex) {
            LOG.error("Failed to run command: {}", Arrays.toString(command), ex);
        }
    }

    /**
     * Execute a command and get the output as string.
     *
     * @param command the command to execute
     * @return the output of execution, or {@link Optional#empty}, if execution failed
     */
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
            LOG.error("Failed to run command: {}", Arrays.toString(command), ex);
        }
        return Optional.empty();
    }

    default Optional<String> executePipeline(String[] command1, String[] command2) {
        try {
            Process process = startPipeline(List.of(
                new ProcessBuilder(command1)
                    .inheritIO().redirectOutput(PIPE),
                new ProcessBuilder(command2)
                    .redirectError(INHERIT)))
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IOException("Failed to execute pipeline command"));

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = IOUtils.toString(reader);
                process.waitFor();
                return Optional.of(output);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
