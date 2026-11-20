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

/**
 * Share point of a directory on macOS, as handled by the "sharing" command.
 * <p>
 * The macOS share points have no comment, and their guest access and read-only setting are for SMB.
 *
 * @param name        the share point name
 * @param path        the shared directory path
 * @param smbShared   true if the share point is shared over SMB
 * @param guestAccess true if guest access is allowed over SMB
 * @param readOnly    true if the share point is read-only over SMB
 */
record MacShare(String name,
                String path,
                boolean smbShared,
                boolean guestAccess,
                boolean readOnly) {

    static final String SHARING_COMMAND = "sharing";
    static final String SHARING_LIST = "-l";
    static final String SHARING_FORMAT = "-f";
    static final String SHARING_FORMAT_JSON = "json";
    static final String SHARING_ADD = "-a";
    static final String SHARING_REMOVE = "-r";
    static final String SHARING_NAME = "-n";
    static final String SHARING_SMB_NAME = "-S";
    static final String SHARING_FLAGS = "-s";
    static final String SHARING_GUEST_FLAGS = "-g";
    static final String SHARING_READ_ONLY = "-R";

    /**
     * Share over SMB only.
     */
    static final String SHARING_SMB_ENABLED = "001";
    static final String SHARING_SMB_DISABLED = "000";
    static final String SHARING_TRUE = "1";
    static final String SHARING_FALSE = "0";

    /**
     * Check if this share has the same settings as another one, ignoring the path.
     *
     * @param other the other share
     * @return true if name, SMB sharing, guest access and read-only are the same
     */
    boolean hasSameSettings(final MacShare other) {
        return name.equals(other.name) &&
            smbShared == other.smbShared &&
            guestAccess == other.guestAccess &&
            readOnly == other.readOnly;
    }
}
