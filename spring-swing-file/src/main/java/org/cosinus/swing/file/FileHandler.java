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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.mimetype.MimeTypeResolver;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * Proxy for handling files.
 */
public class FileHandler {

    private static final Logger LOG = LogManager.getLogger(FileHandler.class);

    private final MimeTypeResolver mimeTypeResolver;

    private final FileSystem fileSystem;

    private List<? extends FileSystemRoot> fileSystemRoots;

    private final ProcessExecutor processExecutor;

    private final Desktop desktop;

    public FileHandler(final MimeTypeResolver mimeTypeResolver,
                       final FileSystem fileSystem,
                       final ProcessExecutor processExecutor) {
        this.mimeTypeResolver = mimeTypeResolver;
        this.fileSystem = fileSystem;
        this.processExecutor = processExecutor;
        this.desktop = initDesktop();
    }

    private Desktop initDesktop() {
        try {
            return Desktop.getDesktop();
        } catch (UnsupportedOperationException e) {
            LOG.warn(e.getMessage());
            return null;
        }
    }

    public Stream<Path> walk(Path path) {
        try {
            return Files.walk(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Stream<Path> list(Path path) {
        try {
            return Files.list(path);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean isSameFile(Path path1, Path path2) {
        try {
            if (path1 == null && path2 == null) {
                return true;
            }
            if (path1 == null || path2 == null) {
                return false;
            }
            return path1.toFile().exists() &&
                path2.toFile().exists() &&
                Files.isSameFile(path1, path2);
        } catch (IOException ex) {
            LOG.error("Failed to compare files: {} and {}", path1, path2, ex);
            return false;
        }
    }

    public synchronized List<? extends FileSystemRoot> getFileSystemRoots() {
        if (fileSystemRoots == null) {
            fileSystemRoots = fileSystem.getFileSystemRoots();
        }
        return fileSystemRoots;
    }

    public boolean isTextCompatible(Path path) {
        return mimeTypeResolver.isTextCompatible(path) ||
            mimeTypeResolver.hasUnknownMimeType(path);
    }

    public boolean isImage(Path path) {
        return mimeTypeResolver.isImageCompatible(path);
    }

    public void reset() {
        fileSystemRoots = null;
    }

    public void mount(final FileSystemRoot fileSystemRoot) {
        fileSystem.mount(fileSystemRoot);
        reset();
    }

    public boolean delete(final File file, boolean moveToTrash) {
        return moveToTrash && desktop != null ?
            moveToTrash(file) :
            file.delete();
    }

    private boolean moveToTrash(final File file) {
        try {
            return desktop.moveToTrash(file);
        } catch (UnsupportedOperationException ex) {
            //fallback to particular os implementation
            return fileSystem.moveToTrash(file);
        }
    }

    public void open(final File file) {
        processExecutor.executeFile(file);
    }

    public void copyPermissions(File fileSource, File fileTarget) {
        fileSystem.copyPermissions(fileSource, fileTarget);
    }

    public Optional<String> getTypeDescription(final File file) {
        return mimeTypeResolver.getMimeTypeDescription(file.toPath(), file.isDirectory());
    }

    public Optional<String> getTypeDescription(final Path path, boolean isDrectory) {
        return mimeTypeResolver.getMimeTypeDescription(path, isDrectory);
    }

    public boolean isLink(final File file) {
        return ofNullable(file)
            .map(File::toPath)
            .map(Files::isSymbolicLink)
            .orElse(false);
    }

    public Path getLinkedPath(final File file) {
        return ofNullable(file)
            .map(File::toPath)
            .map(path -> {
                try {
                    return path.toRealPath();
                } catch (IOException e) {
                    LOG.warn("Failed to read symbolic link: {}", path, e);
                    return null;
                }
            })
            .orElse(null);
    }
}
