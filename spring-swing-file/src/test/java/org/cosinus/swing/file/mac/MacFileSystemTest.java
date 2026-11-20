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
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.api.FilePermissions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The macOS file permissions and share points, with the commands recorded instead of run.
 * <p>
 * The permissions are read from real temporary files, which works on any Unix.
 */
public class MacFileSystemTest {

    /**
     * Share points keyed by name, with the SMB settings in an "smb" object; the path is a format argument.
     */
    private static final String SHARING_JSON_TEMPLATE = """
        {
          "Music": {
            "name": "Music",
            "path": "%s",
            "afp": { "shared": 0, "guest access": 1 },
            "smb": { "name": "Music", "shared": 1, "guest access": 1, "read only": 0 }
          },
          "Public": {
            "name": "Public",
            "path": "/Users/user/Public",
            "smb": { "name": "Public", "shared": "1", "guestAccess": false, "readOnly": true }
          }
        }
        """;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private RecordingProcessExecutor processExecutor;

    private MacFileSystem fileSystem;

    @Before
    public void setUp() {
        processExecutor = new RecordingProcessExecutor();
        fileSystem = new MacFileSystem(processExecutor, null, new ObjectMapper());
    }

    @Test
    public void testParseSharesKeyedByName() {
        List<MacShare> shares = fileSystem.parseShares(SHARING_JSON_TEMPLATE.formatted("/Users/user/Music"));

        assertEquals(List.of(
                new MacShare("Music", "/Users/user/Music", true, true, false),
                new MacShare("Public", "/Users/user/Public", true, false, true)),
            shares);
    }

    /**
     * The share points in an array, with flat SMB settings, and the name only as key.
     */
    @Test
    public void testParseSharesInArrayWithFlatSettings() {
        String json = """
            [
              { "name": "Music", "path": "/Users/user/Music", "smbShared": true, "smb_guest_access": "yes" },
              { "path": "/Users/user/Public", "Shared": 0, "Read Only": 1 }
            ]
            """;

        assertEquals(List.of(new MacShare("Music", "/Users/user/Music", true, true, false)),
            fileSystem.parseShares(json));
        assertEquals(List.of(new MacShare("Public", "/Users/user/Public", false, false, true)),
            fileSystem.parseShares("{\"shares\": {\"Public\": {\"path\": \"/Users/user/Public\", " +
                "\"Shared\": 0, \"Read Only\": 1}}}"));
    }

    @Test
    public void testParseSharesWithoutShares() {
        assertTrue(fileSystem.parseShares("").isEmpty());
        assertTrue(fileSystem.parseShares("{}").isEmpty());
        assertTrue(fileSystem.parseShares("[]").isEmpty());
    }

    @Test(expected = JsonConvertException.class)
    public void testParseSharesWithInvalidJson() {
        fileSystem.parseShares("List of Share Points");
    }

    @Test
    public void testInvalidSharingOutputMeansNotShared() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.output = command -> "not json";

        assertFalse(fileSystem.getFilePermissions(folder).isShared());
    }

    @Test
    public void testToShareName() {
        assertEquals("My Music", fileSystem.toShareName("My Music"));
        assertEquals("a_b_c_d", fileSystem.toShareName("a/b:c*d"));
        assertEquals("share", fileSystem.toShareName("  "));
    }

    @Test
    public void testGetFilePermissions() throws Exception {
        File file = temporaryFolder.newFile("file.txt");
        Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rw-r-----"));
        processExecutor.output = command -> command.getFirst().equals("id") ? "staff admin everyone\n" : "";

        FilePermissions permissions = fileSystem.getFilePermissions(file);

        assertEquals("-rw-r-----", permissions.getTextView());
        assertEquals("640", permissions.getNumberView());
        assertTrue(permissions.isOwnerRead());
        assertTrue(permissions.isOwnerWrite());
        assertFalse(permissions.isOwnerExecute());
        assertTrue(permissions.isGroupRead());
        assertFalse(permissions.isOthersRead());
        assertEquals(Files.getOwner(file.toPath()).getName(), permissions.getOwnerName());
        assertArrayEquals(new String[]{"staff", "admin", "everyone"}, permissions.getAvailableGroupNames());
        assertFalse(permissions.isShared());
        assertEquals("file.txt", permissions.getShareName());
    }

    @Test
    public void testGetFilePermissionsWithStickyDirectory() throws Exception {
        File folder = temporaryFolder.newFolder("shared");
        assertEquals(0, new ProcessBuilder("chmod", "1777", folder.getAbsolutePath()).start().waitFor());

        FilePermissions permissions = fileSystem.getFilePermissions(folder);

        assertEquals("drwxrwxrwt", permissions.getTextView());
        assertEquals("1777", permissions.getNumberView());
        assertTrue(permissions.isSticky());
        assertFalse(permissions.isSetUserId());
    }

    @Test
    public void testGetFilePermissionsOfSharedDirectory() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.output = command -> sharingOutput(command, folder);

        FilePermissions permissions = fileSystem.getFilePermissions(folder);

        assertTrue(permissions.isShared());
        assertEquals("Music", permissions.getShareName());
        assertTrue(permissions.isGuestAccess());
        assertTrue(permissions.isAllowOthersToEdit());
        assertNull(permissions.getShareComment());
    }

    @Test
    public void testGetFilePermissionsOfMissingFile() {
        assertNull(fileSystem.getFilePermissions(new File(temporaryFolder.getRoot(), "missing")));
    }

    @Test
    public void testSetPermissions() throws Exception {
        File file = temporaryFolder.newFile("file.txt");

        fileSystem.setPermissions(file, permissions().ownerRead(true).ownerWrite(true).build().updateViews());

        assertEquals(List.of(List.of("chmod", "600", file.getAbsolutePath())), processExecutor.commands);
    }

    @Test
    public void testCopyPermissions() throws Exception {
        File source = temporaryFolder.newFile("source.txt");
        Files.setPosixFilePermissions(source.toPath(), PosixFilePermissions.fromString("rwxr-x---"));
        File target = temporaryFolder.newFile("target.txt");

        fileSystem.copyPermissions(source, target);

        assertEquals(List.of(List.of("chmod", "750", target.getAbsolutePath())), processExecutor.commands);
    }

    @Test
    public void testCopyPermissionsWithStickyBit() throws Exception {
        File source = temporaryFolder.newFolder("source");
        assertEquals(0, new ProcessBuilder("chmod", "1777", source.getAbsolutePath()).start().waitFor());
        File target = temporaryFolder.newFolder("target");

        fileSystem.copyPermissions(source, target);

        assertEquals(List.of(List.of("chmod", "1777", target.getAbsolutePath())), processExecutor.commands);
    }

    @Test
    public void testCopyPermissionsFromMissingFile() throws Exception {
        fileSystem.copyPermissions(new File(temporaryFolder.getRoot(), "missing"), temporaryFolder.newFile("target"));

        assertTrue(processExecutor.commands.isEmpty());
    }

    @Test
    public void testSetOwnerForFile() {
        File file = new File("/tmp/file.txt");

        fileSystem.setOwnerForFile(file, "user", "staff");
        fileSystem.setOwnerForFile(file, null, "staff");
        fileSystem.setOwnerForFile(file, "user", null);
        fileSystem.setOwnerForFile(file, null, null);

        assertEquals(List.of(
                List.of("chown", "user:staff", file.getAbsolutePath()),
                List.of("chgrp", "staff", file.getAbsolutePath()),
                List.of("chown", "user", file.getAbsolutePath())),
            processExecutor.commands);
    }

    @Test
    public void testSetUserShareIgnoresFiles() throws Exception {
        fileSystem.setUserShare(temporaryFolder.newFile("file.txt"), permissions().shared(true).build());

        assertTrue(processExecutor.commands.isEmpty());
        assertTrue(processExecutor.privilegedCommands.isEmpty());
    }

    @Test
    public void testSetUserShareAddsNewShare() throws Exception {
        File folder = temporaryFolder.newFolder("Music");

        fileSystem.setUserShare(folder, sharePermissions("Music", true, false));

        assertEquals(List.of(List.of("sharing", "-a", folder.getAbsolutePath(),
                "-n", "Music", "-S", "Music", "-s", "001", "-g", "001", "-R", "1")),
            processExecutor.privilegedCommands);
    }

    @Test
    public void testSetUserShareKeepsShareWithSameSettings() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.output = command -> sharingOutput(command, folder);

        fileSystem.setUserShare(folder, sharePermissions("Music", true, true));

        assertTrue(processExecutor.privilegedCommands.isEmpty());
    }

    @Test
    public void testSetUserShareReplacesChangedShare() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.output = command -> sharingOutput(command, folder);

        fileSystem.setUserShare(folder, sharePermissions("Songs", false, true));

        assertEquals(List.of(
                List.of("sharing", "-r", "Music"),
                List.of("sharing", "-a", folder.getAbsolutePath(),
                    "-n", "Songs", "-S", "Songs", "-s", "001", "-g", "000", "-R", "0")),
            processExecutor.privilegedCommands);
    }

    @Test
    public void testSetUserShareRemovesShareWhenNotShared() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.output = command -> sharingOutput(command, folder);

        fileSystem.setUserShare(folder, permissions().shared(false).build());

        assertEquals(List.of(List.of("sharing", "-r", "Music")), processExecutor.privilegedCommands);
    }

    @Test
    public void testSetUserShareDoesNothingWhenNotSharedAndNoShare() throws Exception {
        fileSystem.setUserShare(temporaryFolder.newFolder("Music"), permissions().shared(false).build());

        assertTrue(processExecutor.privilegedCommands.isEmpty());
    }

    private static String sharingOutput(final List<String> command, final File folder) {
        return command.equals(List.of("sharing", "-l", "-f", "json")) ?
            SHARING_JSON_TEMPLATE.formatted(folder.getAbsolutePath()) :
            "";
    }

    private static FilePermissions.FilePermissionsBuilder permissions() {
        return FilePermissions.builder().textView("----------");
    }

    private static FilePermissions sharePermissions(final String name,
                                                    final boolean guestAccess,
                                                    final boolean allowOthersToEdit) {
        return permissions()
            .shared(true)
            .shareName(name)
            .guestAccess(guestAccess)
            .allowOthersToEdit(allowOthersToEdit)
            .build();
    }

    /**
     * {@link ProcessExecutor} recording the commands, and the ones run with privileges, instead of running them.
     */
    private static class RecordingProcessExecutor implements ProcessExecutor {

        private final List<List<String>> commands = new ArrayList<>();

        private final List<List<String>> privilegedCommands = new ArrayList<>();

        private Function<List<String>, String> output = command -> "";

        @Override
        public Optional<String> executeAndGetOutput(final String... command) {
            return Optional.of(output.apply(List.of(command)));
        }

        @Override
        public void execute(final boolean runInTerminal,
                            final File workingDir,
                            final boolean waitForProcess,
                            final String... command) {
            commands.add(List.of(command));
        }

        @Override
        public Optional<String> executeWithPrivilegesAndGetOutput(final String... command) {
            privilegedCommands.add(List.of(command));
            return Optional.of("");
        }

        @Override
        public void executeWithPrivileges(final String... command) {
            executeWithPrivilegesAndGetOutput(command);
        }

        @Override
        public Optional<String> executePipelineWithPrivilegesAndGetOutput(final String[]... commands) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void executeFile(final File file) {
            throw new UnsupportedOperationException();
        }
    }
}
