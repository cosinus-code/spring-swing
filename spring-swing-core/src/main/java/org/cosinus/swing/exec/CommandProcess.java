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
import org.cosinus.swing.error.ProcessExecutionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CommandProcess implements AutoCloseable {

    private final Process process;

    private final String command;

    private Thread worker;

    private volatile boolean running = true;

    public CommandProcess(final Process process) {
        this(process, process
            .info()
            .commandLine()
            .orElse("Unknown command"));
    }

    public CommandProcess(final Process process, final String command) {
        this.process = process;
        this.command = command;
    }

    public static CommandProcess startProcess(String... command) throws IOException {
        return new CommandProcess(
            new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start(),
            Arrays.toString(command));
    }

    public void monitorOutput(ProcessOutputListener outputListener) {
        worker = new Thread(() -> {
            try {
                try (Reader reader = new InputStreamReader(process.getInputStream());
                     BufferedReader bufferedReader = new BufferedReader(reader)) {

                    String line;
                    while (running && (line = bufferedReader.readLine()) != null) {
                        outputListener.process(line);
                    }
                }
            } catch (IOException ex) {
                throw new ProcessExecutionException("Failed to execute command: " + command, ex);
            }
        });
        worker.start();
    }

    public Optional<String> getOutput() throws IOException, InterruptedException {
        try (Reader reader = new InputStreamReader(process.getInputStream())) {
            String output = IOUtils.toString(reader);
            process.waitFor();
            if (process.exitValue() != 0) {
                throw new ProcessExecutionException(process.exitValue(), output);
            }
            return Optional.of(output);
        }
    }

    public void destroy() {
        running = false;
        ofNullable(process)
            .ifPresent(process -> {
                try {
                    process.getInputStream().close();
                } catch (IOException ignored) {
                }

                if (worker != null) {
                    worker.interrupt();
                }

                process.destroy();
                try {
                    if (!process.waitFor(1, SECONDS)) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException ignored) {
                }
            });
    }

    @Override
    public void close() throws Exception {
        destroy();
    }
}
