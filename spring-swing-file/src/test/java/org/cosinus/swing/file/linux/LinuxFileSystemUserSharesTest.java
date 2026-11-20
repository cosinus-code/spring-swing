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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Parsing the output of "net usershare info" into user shares.
 */
public class LinuxFileSystemUserSharesTest {

    @Test
    public void testParseMultipleShares() {
        String output = """
            [music]
            path=/home/user/Music
            comment=My music
            usershare_acl=Everyone:F,
            guest_ok=y

            [documents]
            path=/home/user/Documents
            comment=
            usershare_acl=Everyone:R,
            guest_ok=n
            """;

        List<UserShare> shares = LinuxFileSystem.parseUserShares(output);

        assertEquals(List.of(
                new UserShare("music", "/home/user/Music", "My music", true, true),
                new UserShare("documents", "/home/user/Documents", "", false, false)),
            shares);
    }

    @Test
    public void testParseEmptyOutput() {
        assertTrue(LinuxFileSystem.parseUserShares("").isEmpty());
        assertTrue(LinuxFileSystem.parseUserShares("\n\n").isEmpty());
    }

    @Test
    public void testIgnoreTextBeforeFirstHeader() {
        String output = """
            some warning
            path=/ignored

            [share]
            path=/home/user/share
            """;

        assertEquals(
            List.of(new UserShare("share", "/home/user/share", "", false, false)),
            LinuxFileSystem.parseUserShares(output));
    }

    @Test
    public void testParseShareNameWithSpacesAndSurroundingWhitespace() {
        String output = "  [my share]  \n  path = /home/user/my share  \n  comment = a comment  \n";

        assertEquals(
            List.of(new UserShare("my share", "/home/user/my share", "a comment", false, false)),
            LinuxFileSystem.parseUserShares(output));
    }

    @Test
    public void testKeepEqualsSignInPropertyValue() {
        String output = """
            [share]
            path=/home/user/share
            comment=a=b
            """;

        assertEquals("a=b", LinuxFileSystem.parseUserShares(output).getFirst().comment());
    }

    @Test
    public void testLastDuplicatedPropertyWins() {
        String output = """
            [share]
            path=/first
            path=/second
            """;

        assertEquals("/second", LinuxFileSystem.parseUserShares(output).getFirst().path());
    }

    @Test
    public void testParseAllowOthersToEditFromAclList() {
        String output = """
            [share]
            path=/home/user/share
            usershare_acl=S-1-1-0:R, Everyone:F,
            """;

        assertTrue(LinuxFileSystem.parseUserShares(output).getFirst().allowOthersToEdit());
    }

    @Test
    public void testParseShareWithoutProperties() {
        assertEquals(
            List.of(new UserShare("empty", null, "", false, false)),
            LinuxFileSystem.parseUserShares("[empty]\n"));
    }
}
