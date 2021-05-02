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
 * Enum for identifying the resource sources.
 * <lu>
 * <li>FILESYSTEM: search in filesystem looking in the locations dedicated to the this application</li>
 * <li>CLASSPATH: search in the application classpath</li>
 * <li>FILESYSTEM_BEFORE_CLASSPATH: search first in filesystem and next, if not found, in the classpath</li>
 * </lu>
 */
public enum ResourceSource {

    FILESYSTEM,
    CLASSPATH,
    FILESYSTEM_BEFORE_CLASSPATH
}
