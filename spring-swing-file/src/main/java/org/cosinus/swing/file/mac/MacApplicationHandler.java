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
 * Setter of the default application of a uniform type identifier on macOS.
 */
@FunctionalInterface
interface MacApplicationHandler {

    /**
     * Set the default application, for all its roles, of a uniform type identifier.
     *
     * @param uniformTypeIdentifier the uniform type identifier, e.g. "public.plain-text"
     * @param bundleId              the bundle id of the application, e.g. "com.apple.TextEdit"
     * @return true if the default application was set
     */
    boolean setDefaultApplication(String uniformTypeIdentifier, String bundleId);
}
