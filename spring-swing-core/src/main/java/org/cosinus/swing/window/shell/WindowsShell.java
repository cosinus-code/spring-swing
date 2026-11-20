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
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import static com.sun.jna.win32.W32APIOptions.UNICODE_OPTIONS;

/**
 * The part of the Windows "shell32" library not mapped by JNA platform.
 * <p>
 * The library is loaded on first use, so this interface can be referenced on any platform.
 */
public interface WindowsShell extends StdCallLibrary {

    WindowsShell INSTANCE = Native.load("shell32", WindowsShell.class, UNICODE_OPTIONS);

    /**
     * Retrieve the file type description.
     */
    int SHGFI_TYPENAME = 0x000000400;

    /**
     * Use the given file attributes instead of accessing the file, so the file doesn't need to exist.
     */
    int SHGFI_USEFILEATTRIBUTES = 0x000000010;

    int FILE_ATTRIBUTE_DIRECTORY = 0x00000010;

    int FILE_ATTRIBUTE_NORMAL = 0x00000080;

    /**
     * A file type association has changed.
     */
    int SHCNE_ASSOCCHANGED = 0x08000000;

    /**
     * The items of the change notification are item identifier lists.
     */
    int SHCNF_IDLIST = 0x0000;

    /**
     * Retrieve information about a file, folder, directory or drive.
     *
     * @param path       the file path
     * @param attributes the file attributes, used with {@link #SHGFI_USEFILEATTRIBUTES}
     * @param fileInfo   the structure receiving the file information
     * @param size       the size of the file info structure
     * @param flags      the information to retrieve
     * @return non-zero if successful, zero otherwise
     */
    DWORD_PTR SHGetFileInfo(String path, int attributes, ShFileInfo fileInfo, int size, int flags);

    /**
     * Notify the system of an event that an application has performed.
     *
     * @param eventId the event, e.g. {@link #SHCNE_ASSOCCHANGED}
     * @param flags   the meaning of the items, e.g. {@link #SHCNF_IDLIST}
     * @param item1   the first event-dependent item, or null
     * @param item2   the second event-dependent item, or null
     */
    void SHChangeNotify(int eventId, int flags, Pointer item1, Pointer item2);

    /**
     * The file information returned by {@link #SHGetFileInfo}.
     */
    @Structure.FieldOrder({"hIcon", "iIcon", "dwAttributes", "szDisplayName", "szTypeName"})
    class ShFileInfo extends Structure {

        private static final int MAX_PATH = 260;

        private static final int MAX_TYPE_NAME = 80;

        public HICON hIcon;

        public int iIcon;

        public int dwAttributes;

        public char[] szDisplayName = new char[MAX_PATH];

        public char[] szTypeName = new char[MAX_TYPE_NAME];
    }
}
