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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Implementation of {@link ProcessExecutor} for Mac
 */
public class MacProcessExecutor implements ProcessExecutor {

    private static final String OSASCRIPT_COMMAND = "osascript";

    private static final String OSASCRIPT_EXPRESSION = "-e";

    /**
     * Runs a shell command as administrator, asking the user for the password in the macOS dialog.
     */
    private static final String APPLESCRIPT_WITH_PRIVILEGES = "do shell script %s with administrator privileges";

    @Override
    public void executeFile(File file) {
        execute(false, file.getParentFile(), false, "open", "-a", file.getName());
    }

    /**
     * Execute a command as administrator through AppleScript, which asks the user for the password.
     *
     * @param command the command to execute
     * @return the command output
     */
    @Override
    public Optional<String> executeWithPrivilegesAndGetOutput(String... command) {
        return executeAndGetOutput(OSASCRIPT_COMMAND, OSASCRIPT_EXPRESSION,
            APPLESCRIPT_WITH_PRIVILEGES.formatted(appleScriptString(shellCommand(command))));
    }

    @Override
    public Optional<String> executePipelineWithPrivilegesAndGetOutput(String[]... commands) {
        return Optional.empty();
    }

    @Override
    public void executeWithPrivileges(String... command) {
        executeWithPrivilegesAndGetOutput(command);
    }

    /**
     * Join a command into a shell command line, with every argument single-quoted.
     *
     * @param command the command
     * @return the shell command line
     */
    static String shellCommand(String... command) {
        return stream(command)
            .map(argument -> "'" + argument.replace("'", "'\\''") + "'")
            .collect(joining(" "));
    }

    /**
     * Quote a text as an AppleScript string, escaping the backslashes and the double quotes.
     *
     * @param text the text
     * @return the AppleScript string
     */
    static String appleScriptString(String text) {
        return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

}
