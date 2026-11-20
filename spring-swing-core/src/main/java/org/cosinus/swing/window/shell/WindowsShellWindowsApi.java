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
package org.cosinus.swing.window.shell;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import static com.sun.jna.win32.W32APIOptions.UNICODE_OPTIONS;

/**
 * The part of the Windows "shlwapi" library not mapped by JNA platform.
 * <p>
 * The library is loaded on first use, so this interface can be referenced on any platform.
 */
public interface WindowsShellWindowsApi extends StdCallLibrary {

    WindowsShellWindowsApi INSTANCE = Native.load("shlwapi", WindowsShellWindowsApi.class, UNICODE_OPTIONS);

    int S_OK = 0;

    int S_FALSE = 1;

    int ASSOCF_NONE = 0x00000000;

    /**
     * The association is an executable name (e.g. "notepad.exe"), registered under "Applications".
     */
    int ASSOCF_OPEN_BYEXENAME = 0x00000002;

    /**
     * Don't return an association for unknown file types ("Open with..." dialog).
     */
    int ASSOCF_INIT_IGNOREUNKNOWN = 0x00000400;

    int ASSOCSTR_COMMAND = 1;

    int ASSOCSTR_EXECUTABLE = 2;

    int ASSOCSTR_FRIENDLYAPPNAME = 4;

    int ASSOCSTR_PROGID = 20;

    /**
     * Search for and retrieve a file or protocol association-related string from the registry.
     *
     * @param flags     the ASSOCF flags
     * @param str       the ASSOCSTR string to retrieve
     * @param assoc     the association: an extension (".txt"), a ProgID or an executable name
     * @param extra     the verb (e.g. "open"), or null
     * @param out       the buffer receiving the string, or null to get the needed size
     * @param outLength the size of the buffer, in characters, receiving the size of the string
     * @return S_OK if successful, S_FALSE if out is null, an error code otherwise
     */
    int AssocQueryString(int flags, int str, String assoc, String extra, char[] out, IntByReference outLength);
}
