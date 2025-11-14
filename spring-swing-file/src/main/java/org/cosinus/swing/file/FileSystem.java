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
package org.cosinus.swing.file;

import java.nio.file.Path;
import java.util.Optional;
import oshi.SystemInfo;

import java.io.File;
import java.util.List;

/**
 * Custom interface for filesystem related functionality
 */
public interface FileSystem {

    /**
     * Get file system roots on this machine.
     *
     * @return A list of file system roots.
     */
    List<? extends FileSystemRoot> getFileSystemRoots();

    /**
     * Get default file system roots on this machine.
     *
     * @return A list of file system roots.
     */
    default List<DefaultFileSystemRoot> getDefaultFileSystemRoot() {
        return new SystemInfo().getOperatingSystem().getFileSystem().getFileStores()
            .stream()
            .map(DefaultFileSystemRoot::new)
            .toList();
    }

    void mount(FileSystemRoot fileSystemRoot);

    FileCompatibleApplications findCompatibleApplicationsToExecuteFile(File file);

    void setDefaultApplicationToExecuteFile(String applicationId, File file);

    boolean moveToTrash(File file);

    void copyPermissions(File fileSource, File fileTarget);

    Optional<String> getFileTypeDescription(final Path path, boolean isDirectory);
}
