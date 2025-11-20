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

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.error.ProcessExecutionException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.ProcessBuilder.startPipeline;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static org.cosinus.swing.exec.Command.outputProcess;
import static org.cosinus.swing.exec.Command.pipeProcess;

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
     * Execute a command with privileges and get the output.
     *
     * @param command the command to execute
     */
    Optional<String> executeWithPrivilegesAndGetOutput(String... command);

    /**
     * Execute a pipeline of commands with privileges and get the output.
     *
     * @param commands the commands to execute
     */
    Optional<String> executePipelineWithPrivilegesAndGetOutput(String[]... commands);

    /**
     * Execute a pipeline of commands with privileges.
     *
     * @param commands the commands to execute
     */
    default void executePipelineWithPrivileges(String[]... commands) {
        executePipelineWithPrivilegesAndGetOutput(commands);
    }

    /**
     * Execute a command with privileges.
     *
     * @param command the command to execute
     */
    void executeWithPrivileges(String... command);

    /**
     * Execute a command on user home working directory.
     *
     * @param command the command to execute
     */
    default void execute(String... command) {
        execute(false, new File(System.getProperty("user.home")), true, command);
    }

    /**
     * Execute a command on user home working directory.
     *
     * @param runInTerminal true if run in terminal
     * @param waitForProcess true if wait for process to finish
     * @param command       the command to execute
     */
    default void execute(boolean runInTerminal, boolean waitForProcess, String... command) {
        execute(runInTerminal, new File(System.getProperty("user.home")), waitForProcess, command);
    }

    /**
     * Execute a command on user home working directory.
     *
     * @param runInTerminal true if run in terminal
     * @param command       the command to execute
     */
    default void execute(boolean runInTerminal, String... command) {
        execute(runInTerminal, new File(System.getProperty("user.home")), true, command);
    }

    /**
     * Execute a command in given working directory.
     *
     * @param runInTerminal true if run in terminal
     * @param workingDir    the working directory
     * @param command       the command to execute
     */
    default void execute(boolean runInTerminal, File workingDir, boolean waitForProcess, String... command) {
        try {
            Process process = new ProcessBuilder(runInTerminal ?
                concat(Stream.of("/bin/sh", "-c"), stream(command)).toArray(String[]::new) :
                command)
                .inheritIO()
                .directory(workingDir)
                .start();
            if (waitForProcess) {
                process.waitFor();
            }
        } catch (IOException | InterruptedException ex) {
            throw new ProcessExecutionException("Failed to execute command: " + Arrays.toString(command), ex);
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

            return readOutput(process);
        } catch (IOException | InterruptedException ex) {
            throw new ProcessExecutionException("Failed to execute command: " + Arrays.toString(command), ex);
        }
    }

    default Optional<String> executePipelineAndGetOutput(String[]... commands) {
        try {
            Process process = startPipeline(range(0, commands.length)
                .mapToObj(index -> index < commands.length - 1 ?
                    pipeProcess(commands[index]) :
                    outputProcess(commands[index]))
                .toList())
                .stream()
                .reduce((first, second) -> second)
                .orElseThrow(() -> new ProcessExecutionException("Failed to execute pipeline command"));

            return readOutput(process);
        } catch (IOException | InterruptedException e) {
            throw new ProcessExecutionException("Failed to execute pipeline command", e);
        }
    }

    private Optional<String> readOutput(final Process process) throws IOException, InterruptedException {
        try (Reader reader = new InputStreamReader(process.getInputStream())) {
            String output = IOUtils.toString(reader);
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new ProcessExecutionException(process.exitValue(), output);
            }
            return Optional.of(output);
        }
    }
}
