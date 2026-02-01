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

import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.cosinus.swing.file.mac.MacFileInfoProvider.FOLDER_MIME_TYPE;
import static org.cosinus.swing.util.FileUtils.getExtension;

import com.sun.jna.platform.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.*;

/**
 * Implementation of {@link FileSystem} for Mac
 */
public class MacFileSystem implements FileSystem {

    private static final Logger LOG = LogManager.getLogger(MacFileSystem.class);

    private final ProcessExecutor processExecutor;

    private final MacFileInfoProvider fileTypeInfoProvider;

    public MacFileSystem(final ProcessExecutor processExecutor,
                         final FileInfoProvider fileTypeInfoProvider) {
        this.processExecutor = processExecutor;
        this.fileTypeInfoProvider = (MacFileInfoProvider) fileTypeInfoProvider;
    }

    @Override
    public List<? extends FileSystemRoot> getFileSystemRoots() {
        try {
            return getDefaultFileSystemRoot();
        } catch (Exception ex) {
            return getFileSystemRootsFromCommandLine();
        }
    }

    @Override
    public void mount(FileSystemRoot fileSystemRoot) {
    }

    /**
     * Get file system roots using the output of "diskutil" command
     *
     * @return A list of file system roots.
     */
    private List<MacFileSystemRoot> getFileSystemRootsFromCommandLine() {
        return processExecutor.executeAndGetOutput("diskutil", "list")
            .map(output -> output.split("\\n\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .flatMap(rawDiskData -> stream(rawDiskData.split("\\n"))
                .skip(2))
            .map(rawVolumeData -> stream(rawVolumeData.split("\\s+"))
                .reduce((first, second) -> second)
                .orElse(null))
            .filter(Objects::nonNull)
            .map(this::buildFileSystemRoot)
            .filter(root -> ((MacFileSystemRoot) root).isValid())
            .toList();
    }

    private MacFileSystemRoot buildFileSystemRoot(String volumeId) {
        return processExecutor.executeAndGetOutput("diskutil", "info", volumeId)
            .map(output -> output.split("\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .filter(not(String::isBlank))
            .map(line -> line.split(":"))
            .filter(line -> line.length > 1)
            .collect(toMap(
                property -> property[0].trim(),
                property -> property[1].trim(),
                (k1, k2) -> k1,
                MacFileSystemRoot::new));
    }

    @Override
    public FileCompatibleApplications findCompatibleApplicationsToExecuteFile(File file) {
        Optional<String> uniformTypeIdentifier = fileTypeInfoProvider.findUniformTypeIdentifier(file);
        FileCompatibleApplications compatibleApplications = uniformTypeIdentifier
            .map(fileTypeInfoProvider::getCompatibleApplications)
            .map(applications -> applications
                .stream()
                .collect(toMap(
                    Application::getId,
                    identity(),
                    (u, v) -> u,
                    FileCompatibleApplications::new)))
            .orElseGet(FileCompatibleApplications::new);

        uniformTypeIdentifier
            .map(fileTypeInfoProvider::getUniformType)
            .map(UniformType::getDefaultApplication)
            .or(() -> compatibleApplications
                .values()
                .stream()
                .findFirst())
            .ifPresent(compatibleApplications::setDefaultApplication);

        return compatibleApplications;
    }

    @Override
    public void setDefaultApplicationToExecuteFile(String applicationId, File file) {
        //TODO
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
    public Optional<String> getFileTypeDescription(final Path path, boolean isDirectory) {
        return isDirectory ?
            fileTypeInfoProvider.getFileTypeDescription(FOLDER_MIME_TYPE) :
            fileTypeInfoProvider.findUniformTypeIdentifier(path.toFile())
                .flatMap(fileTypeInfoProvider::getFileTypeDescription)
                .or(() -> fileTypeInfoProvider
                    .getFileTypeDescription("." + getExtension(path.toFile())));
    }

    @Override
    public FilePermissions getFilePermissions(File file) {
        return null;
    }

    @Override
    public void setOwnerForFile(File file, String ownerName, String groupName) {

    }

    @Override
    public void setPermissions(File file, FilePermissions permissions) {

    }
}
