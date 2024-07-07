/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cosinus.swing.resource;

/**
 * Generic interface for a resource locator within the resource source
 */
public interface ResourceLocator {

    /**
     * Get the resource location within the resource source.
     *
     * @return the resource location
     */
    String getLocation();

    /**
     * create a resource locator based on a folder name
     *
     * @param resourceFolderName the folder name
     * @return the resource locator
     */
    static ResourceLocator resourceLocator(String resourceFolderName) {
        return () -> resourceFolderName;
    }
}
