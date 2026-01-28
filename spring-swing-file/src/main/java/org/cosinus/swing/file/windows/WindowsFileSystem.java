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

import com.sun.jna.platform.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.cosinus.swing.file.*;
import org.springframework.cache.annotation.Cacheable;

/**
 * Implementation of {@link FileSystem} for Windows
 */

public class WindowsFileSystem implements FileSystem {

    @Override
    public List<DefaultFileSystemRoot> getFileSystemRoots() {
        return getDefaultFileSystemRoot();
    }

    @Override
    public void mount(FileSystemRoot fileSystemRoot) {
    }

    @Override
    public FileCompatibleApplications findCompatibleApplicationsToExecuteFile(File file) {
        return new FileCompatibleApplications();
    }

    @Override
    public void setDefaultApplicationToExecuteFile(String applicationId, File file) {

    }

    @Override
    public boolean moveToTrash(File file) {
        try {
            FileUtils.getInstance().moveToTrash(file);
            return true;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void copyPermissions(File fileSource, File fileTarget) {

    }

    @Override
    @Cacheable("spring.swing.file.type.description")
    public Optional<String> getFileTypeDescription(Path path, boolean isDirectory) {
        return Optional.empty();
    }

    @Override
    public Permissions getFilePermissions(File file) {
        return null;
    }

    @Override
    public void setGroupOwnerForFile(File file, String groupName) {

    }

    @Override
    public void setPermissions(File file, Permissions permissions) {

    }
}
