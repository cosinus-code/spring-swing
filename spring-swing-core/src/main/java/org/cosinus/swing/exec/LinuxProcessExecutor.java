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

import static java.util.stream.IntStream.range;

/**
 * Implementation of {@link ProcessExecutor} for Linux
 */
public class LinuxProcessExecutor implements ProcessExecutor {

    private static final String SUDO = "sudo";
    private static final String PASSWORD_IS_REQUIRED = "sudo: a password is required";

    private static final String PK_EXEC = "pkexec";

    @Override
    public void executeFile(File file) {
        execute(false, false, "xdg-open", file.getAbsolutePath());
    }

    @Override
    public Optional<String> executeWithPrivilegesAndGetOutput(String... command) {
        return executeAndGetOutput(sudoCommand(command))
            .flatMap(output -> output.contains(PASSWORD_IS_REQUIRED) ?
                executeAndGetOutput(commandWithPassword(command)) :
                Optional.of(output));
    }

    @Override
    public Optional<String> executePipelineWithPrivilegesAndGetOutput(String[]... commands) {
        return executePipelineAndGetOutput(range(0, commands.length)
            .mapToObj(index -> index == 0 ? commandWithPassword(commands[index]) : commands[index])
            .toArray(String[][]::new));
    }

    @Override
    public void executeWithPrivileges(String... command) {
        executeWithPrivilegesAndGetOutput(command);
    }

    private String[] sudoCommand(String... command) {
        return Command.prefixed(SUDO, command);
    }

    private String[] commandWithPassword(String... command) {
        return Command.prefixed(PK_EXEC, command);
    }
}
