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
package org.cosinus.swing.file.linux;

import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Setting the default application of a file type on Linux, with the "xdg-mime" commands recorded instead of run.
 */
public class LinuxDefaultApplicationTest {

    private RecordingProcessExecutor processExecutor;

    private LinuxFileSystem fileSystem;

    @Before
    public void setUp() {
        processExecutor = new RecordingProcessExecutor();
        fileSystem = new LinuxFileSystem(processExecutor, null, null, null, new TestMimeTypeResolver(), null);
    }

    @Test
    public void testSetDefaultApplication() {
        fileSystem.setDefaultApplicationToExecuteFile("org.gnome.TextEditor.desktop", new File("notes.txt"));

        assertEquals(List.of(List.of("xdg-mime", "default", "org.gnome.TextEditor.desktop", "text/plain")),
            processExecutor.commands);
    }

    @Test
    public void testBlankApplicationId() {
        fileSystem.setDefaultApplicationToExecuteFile(null, new File("notes.txt"));
        fileSystem.setDefaultApplicationToExecuteFile("", new File("notes.txt"));
        fileSystem.setDefaultApplicationToExecuteFile("  ", new File("notes.txt"));

        assertTrue(processExecutor.commands.isEmpty());
    }

    @Test
    public void testUnknownMimeType() {
        fileSystem.setDefaultApplicationToExecuteFile("org.gnome.TextEditor.desktop", new File("unknown.xyz"));
        fileSystem.setDefaultApplicationToExecuteFile("org.gnome.TextEditor.desktop", new File("blank.blank"));

        assertTrue(processExecutor.commands.isEmpty());
    }

    /**
     * A failure of "xdg-mime" is only logged, it doesn't fail the caller.
     */
    @Test
    public void testFailedXdgMimeIsOnlyLogged() {
        processExecutor.failure = new ProcessExecutionException(1, "", "xdg-mime: no method available");

        fileSystem.setDefaultApplicationToExecuteFile("org.gnome.TextEditor.desktop", new File("notes.txt"));

        assertEquals(1, processExecutor.commands.size());
    }

    /**
     * {@link MimeTypeResolver} knowing only the ".txt" files, and a blank mime type for the ".blank" files.
     */
    private static class TestMimeTypeResolver extends MimeTypeResolver {

        @Override
        public Optional<String> mimeType(final Path path) {
            String name = path.getFileName().toString();
            if (name.endsWith(".txt")) {
                return Optional.of("text/plain");
            }
            return name.endsWith(".blank") ? Optional.of(" ") : Optional.empty();
        }
    }

    /**
     * {@link ProcessExecutor} recording the commands instead of running them, optionally failing.
     */
    private static class RecordingProcessExecutor implements ProcessExecutor {

        private final List<List<String>> commands = new ArrayList<>();

        private ProcessExecutionException failure;

        @Override
        public Optional<String> executeAndGetOutput(final String... command) {
            commands.add(List.of(command));
            if (failure != null) {
                throw failure;
            }
            return Optional.of("");
        }

        @Override
        public void execute(final boolean runInTerminal,
                            final File workingDir,
                            final boolean waitForProcess,
                            final String... command) {
            throw new UnsupportedOperationException("the commands must be run with executeAndGetOutput");
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
}
