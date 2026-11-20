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

import java.util.Map;

import static java.util.Arrays.stream;

/**
 * Samba user share of a directory, as handled by "net usershare".
 *
 * @param name              the share name
 * @param path              the shared directory path
 * @param comment           the share comment
 * @param allowOthersToEdit true if everyone has full access to the share
 * @param guestAccess       true if guest access is allowed
 */
record UserShare(String name,
                 String path,
                 String comment,
                 boolean allowOthersToEdit,
                 boolean guestAccess) {

    static final String NET_COMMAND = "net";
    static final String USERSHARE_COMMAND = "usershare";
    static final String USERSHARE_INFO = "info";
    static final String USERSHARE_ADD = "add";
    static final String USERSHARE_DELETE = "delete";

    static final String PROPERTY_PATH = "path";
    static final String PROPERTY_COMMENT = "comment";
    static final String PROPERTY_ACL = "usershare_acl";

    static final String ACL_SEPARATOR = ",";
    static final String ACL_EVERYONE_FULL = "Everyone:F";
    static final String ACL_EVERYONE_READ = "Everyone:R";

    static final String GUEST_OK = "guest_ok";

    static final String YES = "y";
    static final String NO = "n";

    static final String GUEST_OK_YES = GUEST_OK + "=" + YES;
    static final String GUEST_OK_NO = GUEST_OK + "=" + NO;

    /**
     * Create a user share from the properties of a "net usershare info" section.
     *
     * @param name       the share name
     * @param properties the share properties (path, comment, usershare_acl, guest_ok)
     * @return the user share
     */
    static UserShare of(final String name, final Map<String, String> properties) {
        return new UserShare(
            name,
            properties.get(PROPERTY_PATH),
            properties.getOrDefault(PROPERTY_COMMENT, ""),
            stream(properties.getOrDefault(PROPERTY_ACL, "").split(ACL_SEPARATOR))
                .anyMatch(acl -> acl.trim().equalsIgnoreCase(ACL_EVERYONE_FULL)),
            YES.equalsIgnoreCase(properties.get(GUEST_OK)));
    }

    /**
     * Check if this share has the same settings as another one, ignoring the path.
     *
     * @param other the other share
     * @return true if name, comment, edit and guest access are the same
     */
    boolean hasSameSettings(final UserShare other) {
        return name.equals(other.name) &&
            comment.equals(other.comment) &&
            allowOthersToEdit == other.allowOthersToEdit &&
            guestAccess == other.guestAccess;
    }
}
