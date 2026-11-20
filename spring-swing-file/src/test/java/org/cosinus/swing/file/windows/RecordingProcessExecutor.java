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
package org.cosinus.swing.file.windows;

import org.cosinus.swing.exec.ProcessExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_16LE;

/**
 * {@link ProcessExecutor} recording the executed commands instead of running them.
 * <p>
 * The PowerShell scripts, passed as encoded commands, are decoded,
 * and their output is given by {@link #setPowerShellOutput(Function)}.
 */
public class RecordingProcessExecutor implements ProcessExecutor {

    private static final String POWERSHELL = "powershell";

    private final List<List<String>> commands = new ArrayList<>();

    private Function<String, String> powerShellOutput;

    public RecordingProcessExecutor() {
        reset();
    }

    /**
     * Forget the executed commands and the PowerShell output.
     */
    public void reset() {
        commands.clear();
        powerShellOutput = script -> "";
    }

    /**
     * @param powerShellOutput the output of a PowerShell script, by script
     */
    public void setPowerShellOutput(final Function<String, String> powerShellOutput) {
        this.powerShellOutput = powerShellOutput;
    }

    /**
     * @return the executed commands
     */
    public List<List<String>> getCommands() {
        return commands;
    }

    /**
     * @return the decoded PowerShell scripts of the executed commands
     */
    public List<String> getPowerShellScripts() {
        return commands.stream()
            .filter(command -> command.getFirst().equals(POWERSHELL))
            .map(command -> decode(command.toArray(String[]::new)))
            .toList();
    }

    @Override
    public Optional<String> executeAndGetOutput(final String... command) {
        commands.add(List.of(command));
        return Optional.of(command[0].equals(POWERSHELL) ? powerShellOutput.apply(decode(command)) : "");
    }

    private static String decode(final String... command) {
        return new String(Base64.getDecoder().decode(command[command.length - 1]), UTF_16LE);
    }

    @Override
    public void executeFile(final File file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> executeWithPrivilegesAndGetOutput(final String... command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> executePipelineWithPrivilegesAndGetOutput(final String[]... commands) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void executeWithPrivileges(final String... command) {
        throw new UnsupportedOperationException();
    }
}
