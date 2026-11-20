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

import static java.util.Objects.requireNonNullElse;

/**
 * SMB share of a directory on Windows, as handled by the "SmbShare" PowerShell module.
 *
 * @param name              the share name
 * @param path              the shared directory path
 * @param comment           the share description
 * @param allowOthersToEdit true if everyone has full access to the share
 */
record WindowsShare(String name,
                    String path,
                    String comment,
                    boolean allowOthersToEdit) {

    WindowsShare {
        comment = requireNonNullElse(comment, "");
    }

    /**
     * Check if this share has the same settings as another one, ignoring the path.
     *
     * @param other the other share
     * @return true if name, comment and edit access are the same
     */
    boolean hasSameSettings(final WindowsShare other) {
        return name.equalsIgnoreCase(other.name) &&
            comment.equals(other.comment) &&
            allowOthersToEdit == other.allowOthersToEdit;
    }
}
