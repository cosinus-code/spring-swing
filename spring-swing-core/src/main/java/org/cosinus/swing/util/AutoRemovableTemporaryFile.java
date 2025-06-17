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

import java.io.File;
import java.io.IOException;

import static org.cosinus.swing.util.FileUtils.fullExtension;

/**
 * Creator of temporary file which is auto removable
 */
public class AutoRemovableTemporaryFile implements AutoCloseable {

    private final File temporaryFile;

    public AutoRemovableTemporaryFile(File file) throws IOException {
        this(file.getName());
    }

    public AutoRemovableTemporaryFile(String name) throws IOException {
        temporaryFile = File.createTempFile(getClass().getPackageName() + System.currentTimeMillis(),
                                            name);
    }

    public File getFile() {
        return temporaryFile;
    }

    @Override
    public void close() {
        try {
            if (temporaryFile.exists()) {
                temporaryFile.delete();
            }
        } catch (Exception ex) {
            // this can be ignored as a temporary file is harmless
        }
    }

    public static AutoRemovableTemporaryFile autoRemovableTemporaryFileWithExtension
            (String fileExtension) throws IOException {
        return new AutoRemovableTemporaryFile(fullExtension(fileExtension));
    }
}
