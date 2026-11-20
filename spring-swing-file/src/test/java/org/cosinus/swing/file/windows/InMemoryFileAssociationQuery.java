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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_COMMAND;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_EXECUTABLE;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_FRIENDLYAPPNAME;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_PROGID;

/**
 * {@link WindowsFileAssociationQuery} with the association strings in memory.
 */
public class InMemoryFileAssociationQuery implements WindowsFileAssociationQuery {

    private final Map<String, String> strings = new HashMap<>();

    /**
     * Forget all association strings.
     */
    public void reset() {
        strings.clear();
    }

    /**
     * Add the strings of an association; the null strings are not added.
     */
    public void putAssociation(final int flags,
                               final String association,
                               final String progId,
                               final String command,
                               final String executable,
                               final String name) {
        put(flags, ASSOCSTR_COMMAND, association, command);
        put(flags, ASSOCSTR_EXECUTABLE, association, executable);
        put(flags, ASSOCSTR_FRIENDLYAPPNAME, association, name);
        put(flags, ASSOCSTR_PROGID, association, progId);
    }

    /**
     * Add one string of an association, if not null.
     */
    public void put(final int flags, final int str, final String association, final String value) {
        if (value != null) {
            strings.put(key(flags, str, association), value);
        }
    }

    @Override
    public Optional<String> query(final int flags, final int str, final String association) {
        return Optional.ofNullable(strings.get(key(flags, str, association)));
    }

    private static String key(final int flags, final int str, final String association) {
        return flags + "|" + str + "|" + association;
    }
}
