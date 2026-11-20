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

import java.util.Optional;

/**
 * Query a string of a file association, as "AssocQueryString" does.
 */
@FunctionalInterface
public interface WindowsFileAssociationQuery {

    /**
     * @param flags       the ASSOCF flags
     * @param str         the ASSOCSTR string to retrieve
     * @param association the extension, ProgID or executable name
     * @return the string, or {@link Optional#empty()} if not found
     */
    Optional<String> query(int flags, int str, String association);
}
