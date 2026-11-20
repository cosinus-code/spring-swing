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
package org.cosinus.swing.file.mac;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.file.api.Application;
import org.cosinus.swing.file.api.FileCompatibleApplications;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.cosinus.swing.icon.IconSize.X32;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Setting the default application of a file type on macOS, with the Launch Services call recorded.
 */
public class MacDefaultApplicationTest {

    private static final String PLAIN_TEXT = "public.plain-text";

    private final Application textEdit = application("com.apple.TextEdit", "TextEdit");

    private final Application editor = application("com.example.Editor", "Editor");

    private final List<String> calls = new ArrayList<>();

    private UniformType plainText;

    private boolean launchServicesResult;

    private MacFileSystem fileSystem;

    @Before
    public void setUp() {
        plainText = new UniformType(PLAIN_TEXT, textEdit, "Plain Text", Map.of());
        launchServicesResult = true;
        fileSystem = new MacFileSystem(null, new TestFileInfoProvider(), new ObjectMapper(),
            (uniformTypeIdentifier, bundleId) -> {
                calls.add(uniformTypeIdentifier + "|" + bundleId);
                return launchServicesResult;
            });
    }

    @Test
    public void testSetDefaultApplication() {
        fileSystem.setDefaultApplicationToExecuteFile("com.example.Editor", new File("notes.txt"));

        assertEquals(List.of("public.plain-text|com.example.Editor"), calls);
        assertSame(editor, plainText.getDefaultApplication());
    }

    /**
     * The new default application is found with the compatible applications, without reloading the types.
     */
    @Test
    public void testNewDefaultApplicationIsFound() {
        fileSystem.setDefaultApplicationToExecuteFile("com.example.Editor", new File("notes.txt"));

        FileCompatibleApplications applications =
            fileSystem.findCompatibleApplicationsToExecuteFile(new File("notes.txt"));

        assertSame(editor, applications.getDefaultApplication());
    }

    @Test
    public void testFailedLaunchServicesCallKeepsDefaultApplication() {
        launchServicesResult = false;

        fileSystem.setDefaultApplicationToExecuteFile("com.example.Editor", new File("notes.txt"));

        assertEquals(1, calls.size());
        assertSame(textEdit, plainText.getDefaultApplication());
    }

    @Test
    public void testUnknownUniformType() {
        fileSystem.setDefaultApplicationToExecuteFile("com.example.Editor", new File("unknown.xyz"));

        assertTrue(calls.isEmpty());
    }

    @Test
    public void testBlankApplicationId() {
        fileSystem.setDefaultApplicationToExecuteFile(" ", new File("notes.txt"));
        fileSystem.setDefaultApplicationToExecuteFile(null, new File("notes.txt"));

        assertTrue(calls.isEmpty());
    }

    /**
     * An application set as default but not registered yet is set in Launch Services only.
     */
    @Test
    public void testUnregisteredApplication() {
        fileSystem.setDefaultApplicationToExecuteFile("com.example.New", new File("notes.txt"));

        assertEquals(List.of("public.plain-text|com.example.New"), calls);
        assertSame(textEdit, plainText.getDefaultApplication());
    }

    private static Application application(final String bundleId, final String name) {
        return new Application(bundleId, name, null, null, null, "open %f", X32, null, false);
    }

    /**
     * {@link MacFileInfoProvider} knowing only the ".txt" files, without running "mdls" or "lsregister".
     */
    private class TestFileInfoProvider extends MacFileInfoProvider {

        TestFileInfoProvider() {
            super(null, null);
        }

        @Override
        public Optional<String> findUniformTypeIdentifier(final File file) {
            return file.getName().endsWith(".txt") ? Optional.of(PLAIN_TEXT) : Optional.empty();
        }

        @Override
        public UniformType getUniformType(final String uniformTypeIdentifier) {
            return PLAIN_TEXT.equals(uniformTypeIdentifier) ? plainText : null;
        }

        @Override
        public Set<Application> getCompatibleApplications(final String uniformTypeIdentifier) {
            return Set.of(textEdit, editor);
        }

        @Override
        public Optional<Application> findApplication(final String bundleId) {
            return Optional.of(textEdit)
                .filter(application -> application.getId().equals(bundleId))
                .or(() -> Optional.of(editor).filter(application -> application.getId().equals(bundleId)));
        }
    }
}
