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

import org.cosinus.swing.file.api.FilePermissions;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryFlag;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.UserPrincipal;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static java.nio.file.attribute.AclEntryPermission.EXECUTE;
import static java.nio.file.attribute.AclEntryPermission.READ_DATA;
import static java.nio.file.attribute.AclEntryPermission.WRITE_DATA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The Windows file system parts which don't need Windows to run:
 * the ACL evaluation, the share parsing and naming, and the executed icacls and PowerShell commands.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestWindowsFileApplication.class)
public class WindowsFileSystemTest {

    private static final String SHARE_JSON =
        "[{\"name\":\"Music\",\"path\":\"%s\",\"comment\":\"My music\",\"allowOthersToEdit\":false}]";

    private static final String NO_SHARE_JSON = "[]";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Autowired
    private RecordingProcessExecutor processExecutor;

    @Autowired
    private WindowsFileSystem fileSystem;

    @Before
    public void setUp() {
        processExecutor.reset();
    }

    @Test
    public void testQuote() {
        assertEquals("'text'", fileSystem.quote("text"));
        assertEquals("'it''s'", fileSystem.quote("it's"));
        assertEquals("'\u2018\u2018a\u2019\u2019'", fileSystem.quote("\u2018a\u2019"));
        assertEquals("''", fileSystem.quote(""));
    }

    @Test
    public void testToShareName() {
        assertEquals("My Music", fileSystem.toShareName("My Music"));
        assertEquals("a_b_c_d_e_f", fileSystem.toShareName("a\\b/c:d*e?f"));
        assertEquals("x_y_z_", fileSystem.toShareName("x[y]z\""));
        assertEquals("share", fileSystem.toShareName("   "));
        assertEquals(80, fileSystem.toShareName("n".repeat(100)).length());
    }

    @Test
    public void testGetPermissionsAllowedMinusDenied() {
        UserPrincipal user = new TestPrincipal("user");
        List<AclEntry> acl = List.of(
            entry(AclEntryType.ALLOW, user, READ_DATA, WRITE_DATA, EXECUTE),
            entry(AclEntryType.DENY, user, WRITE_DATA));

        assertEquals(EnumSet.of(READ_DATA, EXECUTE), fileSystem.getPermissions(acl, Optional.of(user)));
    }

    @Test
    public void testGetPermissionsIgnoresOtherPrincipalsAndInheritOnlyEntries() {
        UserPrincipal user = new TestPrincipal("user");
        AclEntry inheritOnly = AclEntry.newBuilder(entry(AclEntryType.ALLOW, user, EXECUTE))
            .setFlags(AclEntryFlag.INHERIT_ONLY)
            .build();
        List<AclEntry> acl = List.of(
            entry(AclEntryType.ALLOW, new TestPrincipal("other"), WRITE_DATA),
            entry(AclEntryType.ALLOW, user, READ_DATA),
            inheritOnly);

        assertEquals(EnumSet.of(READ_DATA), fileSystem.getPermissions(acl, Optional.of(user)));
    }

    @Test
    public void testGetPermissionsWithoutPrincipal() {
        List<AclEntry> acl = List.of(entry(AclEntryType.ALLOW, new TestPrincipal("user"), READ_DATA));

        assertTrue(fileSystem.getPermissions(acl, Optional.empty()).isEmpty());
    }

    @Test
    public void testParseShares() {
        WindowsShare[] shares = fileSystem.parseShares(
            "[{\"name\":\"Music\",\"path\":\"C:\\\\Music\",\"comment\":null,\"allowOthersToEdit\":true}]");

        assertArrayEquals(new WindowsShare[]{new WindowsShare("Music", "C:\\Music", "", true)}, shares);
        assertEquals(0, fileSystem.parseShares(NO_SHARE_JSON).length);
    }

    @Test
    public void testSetPermissionsGrantsAndRemoves() throws Exception {
        File file = temporaryFolder.newFile("file.txt");
        String owner = Files.getOwner(file.toPath()).getName();

        fileSystem.setPermissions(file, permissions()
            .ownerRead(true)
            .ownerWrite(true)
            .othersExecute(true)
            .build());

        assertEquals(List.of(
                List.of("icacls", file.getAbsolutePath(), "/grant:r",
                    owner + ":(RD,RA,REA,RC,S,WD,AD,WA,WEA)",
                    "*S-1-1-0:(X,S)"),
                List.of("icacls", file.getAbsolutePath(), "/remove:g", "*S-1-5-32-545")),
            processExecutor.getCommands());
    }

    @Test
    public void testSetUserShareIgnoresFiles() throws Exception {
        fileSystem.setUserShare(temporaryFolder.newFile("file.txt"), permissions().shared(true).build());

        assertTrue(processExecutor.getCommands().isEmpty());
    }

    @Test
    public void testSetUserShareAddsNewShare() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.setPowerShellOutput(script -> NO_SHARE_JSON);

        fileSystem.setUserShare(folder, sharePermissions("Music", "My music", true));

        List<String> scripts = processExecutor.getPowerShellScripts();
        assertEquals(2, scripts.size());
        assertTrue(scripts.get(0).contains("Get-SmbShare"));
        assertTrue(scripts.get(0).contains(fileSystem.quote(folder.getAbsolutePath())));
        assertTrue(scripts.get(1).contains(
            "New-SmbShare -Name 'Music' -Path " + fileSystem.quote(folder.getAbsolutePath()) +
                " -Description 'My music' -FullAccess $everyone"));
    }

    @Test
    public void testSetUserShareKeepsShareWithSameSettings() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.setPowerShellOutput(script -> shareJson(folder));

        fileSystem.setUserShare(folder, sharePermissions("Music", "My music", false));

        assertEquals(1, processExecutor.getPowerShellScripts().size());
    }

    @Test
    public void testSetUserShareReplacesChangedShare() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.setPowerShellOutput(script -> shareJson(folder));

        fileSystem.setUserShare(folder, sharePermissions("Songs", "My music", false));

        List<String> scripts = processExecutor.getPowerShellScripts();
        assertEquals(3, scripts.size());
        assertTrue(scripts.get(1).contains("Remove-SmbShare -Name 'Music' -Force"));
        assertTrue(scripts.get(2).contains("New-SmbShare -Name 'Songs'"));
        assertTrue(scripts.get(2).contains("-ReadAccess $everyone"));
    }

    @Test
    public void testSetUserShareRemovesShareWhenNotShared() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.setPowerShellOutput(script -> shareJson(folder));

        fileSystem.setUserShare(folder, permissions().shared(false).build());

        List<String> scripts = processExecutor.getPowerShellScripts();
        assertEquals(2, scripts.size());
        assertTrue(scripts.get(1).contains("Remove-SmbShare -Name 'Music' -Force"));
    }

    @Test
    public void testSetUserShareDoesNothingWhenNotSharedAndNoShare() throws Exception {
        File folder = temporaryFolder.newFolder("Music");
        processExecutor.setPowerShellOutput(script -> NO_SHARE_JSON);

        fileSystem.setUserShare(folder, permissions().shared(false).build());

        assertEquals(1, processExecutor.getPowerShellScripts().size());
    }

    private static String shareJson(final File folder) {
        return SHARE_JSON.formatted(folder.getAbsolutePath().replace("\\", "\\\\"));
    }

    private static FilePermissions.FilePermissionsBuilder permissions() {
        return FilePermissions.builder().textView("----------");
    }

    private static FilePermissions sharePermissions(final String name,
                                                    final String comment,
                                                    final boolean allowOthersToEdit) {
        return permissions()
            .shared(true)
            .shareName(name)
            .shareComment(comment)
            .allowOthersToEdit(allowOthersToEdit)
            .build();
    }

    private static AclEntry entry(final AclEntryType type,
                                  final UserPrincipal principal,
                                  final AclEntryPermission... permissions) {
        return AclEntry.newBuilder()
            .setType(type)
            .setPrincipal(principal)
            .setPermissions(permissions)
            .build();
    }

    private record TestPrincipal(String getName) implements UserPrincipal {
    }
}
