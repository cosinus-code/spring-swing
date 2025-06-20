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

package org.cosinus.swing.resource;

/**
 * Enum for application resource types.
 * <p>
 * Also implementation of {@link ResourceLocator}.
 *
 * <ul>
 * <li>CONF: configuration resources</li>
 * <li>I18N: translation resources</li>
 * <li>IMAGE: image resources</li>
 * </ul>
 */
public enum ResourceType implements ResourceLocator {
    CONF,
    I18N,
    IMAGE,
    UI;

    @Override
    public String getLocation() {
        return name().toLowerCase();
    }
}
