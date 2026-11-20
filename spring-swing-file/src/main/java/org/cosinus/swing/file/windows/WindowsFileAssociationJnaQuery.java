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

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.window.shell.WindowsShellWindowsApi;

import java.util.Optional;

import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.S_FALSE;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.S_OK;

/**
 * {@link WindowsFileAssociationQuery} implementation using "AssocQueryString" through JNA,
 * for the "open" verb.
 */
@Slf4j
public class WindowsFileAssociationJnaQuery implements WindowsFileAssociationQuery {

    /**
     * The verb of the association command used to open a file.
     */
    private static final String VERB_OPEN = "open";

    /**
     * Query a file association string, first getting the string length and then the string.
     */
    @Override
    public Optional<String> query(final int flags, final int str, final String association) {
        try {
            IntByReference length = new IntByReference();
            int result = WindowsShellWindowsApi.INSTANCE.AssocQueryString(flags, str, association, VERB_OPEN, null, length);
            if ((result != S_OK && result != S_FALSE) || length.getValue() <= 0) {
                return Optional.empty();
            }
            char[] buffer = new char[length.getValue()];
            result = WindowsShellWindowsApi.INSTANCE.AssocQueryString(flags, str, association, VERB_OPEN, buffer, length);
            return result == S_OK ?
                Optional.of(Native.toString(buffer)).filter(StringUtils::isNotBlank) :
                Optional.empty();
        } catch (RuntimeException | LinkageError ex) {
            log.debug("Cannot query the string {} of association {}", str, association, ex);
            return Optional.empty();
        }
    }
}
