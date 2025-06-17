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

package org.cosinus.swing.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR;
import static org.apache.commons.io.FilenameUtils.removeExtension;

/**
 * File related utils
 */
public final class FileUtils {

    /**
     * @param file the file
     * @return the extension of the file
     */
    public static String getExtension(File file) {
        return FilenameUtils.getExtension(file.getName());
    }

    public static String setExtension(String filename,
                                      String extension) {
        return ofNullable(extension)
            .map(FileUtils::fullExtension)
            .filter(ext -> !filename.endsWith(ext))
            .map(fullExtension -> removeExtension(filename) + fullExtension)
            .orElse(filename);
    }

    public static String fullExtension(String extension) {
        return EXTENSION_SEPARATOR + extension;
    }

    public static String setHomeFolder(String path) {
        return path.replaceFirst("^~", System.getProperty("user.home"));
    }

    private FileUtils() {
    }
}
