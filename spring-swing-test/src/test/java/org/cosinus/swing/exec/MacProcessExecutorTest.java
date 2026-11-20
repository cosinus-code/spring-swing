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

import org.junit.Test;

import static org.cosinus.swing.exec.MacProcessExecutor.appleScriptString;
import static org.cosinus.swing.exec.MacProcessExecutor.shellCommand;
import static org.junit.Assert.assertEquals;

/**
 * The quoting of the commands run with administrator privileges through AppleScript.
 */
public class MacProcessExecutorTest {

    @Test
    public void testShellCommandQuotesEveryArgument() {
        assertEquals("'sharing' '-a' '/Users/user/My Music'", shellCommand("sharing", "-a", "/Users/user/My Music"));
    }

    @Test
    public void testShellCommandEscapesSingleQuotes() {
        assertEquals("'sharing' '-n' 'it'\\''s'", shellCommand("sharing", "-n", "it's"));
    }

    @Test
    public void testShellCommandKeepsShellCharactersAsText() {
        assertEquals("'echo' '$HOME; rm -rf /'", shellCommand("echo", "$HOME; rm -rf /"));
    }

    @Test
    public void testAppleScriptStringEscapesBackslashesAndDoubleQuotes() {
        assertEquals("\"'a\\\\b' '\\\"c\\\"'\"", appleScriptString("'a\\b' '\"c\"'"));
    }
}
