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
package org.cosinus.swing.file.linux;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.*;
import org.cosinus.swing.file.mac.BlockDevice;
import org.cosinus.swing.file.mac.BlockDevices;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.translate.Translator;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.probeContentType;
import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.*;
import static org.cosinus.swing.error.ProcessExecutionException.PERMISSION_DENIED;
import static org.cosinus.swing.exec.Command.commands;
import static org.cosinus.swing.exec.Command.of;
import static org.cosinus.swing.file.linux.MtpFileSystemRoot.MTP_PROTOCOL;
import static org.cosinus.swing.file.linux.MtpFileSystemRoot.MTP_PROTOCOL_MARK;
import static org.cosinus.swing.icon.IconSize.X32;

/**
 * Implementation of {@link FileSystem} for Linux
 */
public class LinuxFileSystem implements FileSystem {

    private final Translator translator;

    Logger LOG = LogManager.getLogger(LinuxFileSystem.class);

    private static final Set<String> IGNORED_FILESYSTEMS = Set.of("swap", "vfat");

    private static final Set<String> POSIX_COMPLIANT_FILESYSTEMS = Set.of(
        "ext4", "ext3", "ext2", "xfs", "btrfs", "jfs", "reiserfs", "zfs", "f2fs", "ufs", "ffs");

    private final ProcessExecutor processExecutor;

    private final MimeTypeResolver mimeTypeResolver;

    private final FileInfoProvider fileInfoProvider;

    private final ObjectMapper objectMapper;

    private final ErrorHandler errorHandler;

    public LinuxFileSystem(final ProcessExecutor processExecutor,
                           final ObjectMapper objectMapper,
                           final ErrorHandler errorHandler,
                           final Translator translator,
                           final MimeTypeResolver mimeTypeResolver,
                           final FileInfoProvider fileInfoProvider) {
        this.processExecutor = processExecutor;
        this.objectMapper = objectMapper;
        this.errorHandler = errorHandler;
        this.translator = translator;
        this.mimeTypeResolver = mimeTypeResolver;
        this.fileInfoProvider = fileInfoProvider;
    }

    @Override
    public List<? extends FileSystemRoot> getFileSystemRoots() {
        Map<String, ? extends FileSystemRoot> rootsMap = concat(
            getDefaultFileSystemRoot()
                .stream()
                .filter(root -> !root.getMountPoint().startsWith("/tmp/")),
            concat(
                listPartitions().stream()
                    .filter(root -> root.getType() != null &&
                        !IGNORED_FILESYSTEMS.contains(root.getType())),
                getMtpFilesystemRoots().stream()))
            .collect(toMap(
                FileSystemRoot::getId,
                identity(),
                this::fileSystemRootMerger,
                LinkedHashMap::new));

        return new ArrayList<>(rootsMap.values());
    }

    private FileSystemRoot fileSystemRootMerger(FileSystemRoot u, FileSystemRoot v) {
        if (u instanceof LinuxFileSystemRoot linuxFileSystemRoot &&
            v instanceof DefaultFileSystemRoot defaultFileSystemRoot) {
            return mergeFileSystemRoots(defaultFileSystemRoot, linuxFileSystemRoot);
        }
        if (v instanceof LinuxFileSystemRoot linuxFileSystemRoot &&
            u instanceof DefaultFileSystemRoot defaultFileSystemRoot) {
            return mergeFileSystemRoots(defaultFileSystemRoot, linuxFileSystemRoot);
        }
        return u instanceof LinuxFileSystemRoot ? v : u;
    }

    private FileSystemRoot mergeFileSystemRoots(
        final DefaultFileSystemRoot defaultFileSystemRoot,
        final LinuxFileSystemRoot linuxFileSystemRoot) {
        ofNullable(linuxFileSystemRoot.getDevice())
            .ifPresent(defaultFileSystemRoot::setFileSystemDevice);
        ofNullable(linuxFileSystemRoot.getType())
            .ifPresent(defaultFileSystemRoot::setType);
        return defaultFileSystemRoot;
    }

    @Override
    public void mount(FileSystemRoot fileSystemRoot) {
        String userName = System.getProperty("user.name");
        if (fileSystemRoot.getVolume() == null || userName == null) {
            return;
        }

        String mountPoint = "/media/" + userName + "/" + fileSystemRoot.getUuid();
        String[] mkdirCommand = of("mkdir", mountPoint);
        String[] mountCommand = of("mount", fileSystemRoot.getVolume(), mountPoint);
        try {
            processExecutor.executePipelineWithPrivileges(new File(mountPoint).exists() ?
                commands(mountCommand) :
                commands(mkdirCommand, mountCommand));
            fileSystemRoot.setMountPoint(mountPoint);
        } catch (ProcessExecutionException ex) {
            if (ex.getProcessExitCode() != PERMISSION_DENIED) {
                errorHandler.handleError(ex);
            }
        }
    }

    private List<? extends FileSystemRoot> listPartitions() {
        return processExecutor.executeAndGetOutput("lsblk", "-J", "-b", "-o",
                "UUID,NAME,LABEL,PATH,TYPE,SIZE,FSTYPE,RM,ROTA,HOTPLUG,VENDOR,MOUNTPOINT")
            .map(this::getBlockDevices)
            .map(BlockDevices::getBlockDevices)
            .stream()
            .flatMap(Collection::stream)
            .flatMap(this::getChildrenDevices)
            .map(LinuxFileSystemRoot::new)
            .toList();
    }

    private Stream<BlockDevice> getChildrenDevices(BlockDevice parentDevice) {
        parentDevice.getChildren()
            .forEach(childDevice -> childDevice.setVendor(ofNullable(parentDevice.getVendor())
                .map(String::trim)
                .filter(not(String::isEmpty))
                .orElse(null)));
        return parentDevice.getChildren().stream();
    }

    private BlockDevices getBlockDevices(String input) {
        try {
            return objectMapper.readValue(input.getBytes(UTF_8), BlockDevices.class);
        } catch (IOException e) {
            throw new JsonConvertException(format("Failed to map the lsblk output: %s", input), e);
        }
    }

    private List<MtpFileSystemRoot> getMtpFilesystemRoots() {
        return getMtpMountFolder()
            .map(mtpMountFolder -> getMtpMountedDevices()
                .entrySet()
                .stream()
                .map(entry -> new MtpFileSystemRoot(
                    entry.getKey().endsWith("/") ? substringBefore(entry.getKey(), "/") :
                        entry.getKey(),
                    entry.getValue(),
                    mtpMountFolder))
                .filter(MtpFileSystemRoot::isValid)
                .toList())
            .orElseGet(Collections::emptyList);
    }

    private Map<String, String> getMtpMountedDevices() {
        return getGioMountOutput()
            .map(output -> output.split("\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .filter(Objects::nonNull)
            .map(output -> ImmutablePair.of(
                substringAfter(output, MTP_PROTOCOL_MARK),
                substringBetween(output, ":", MTP_PROTOCOL_MARK)))
            .filter(pair -> pair.getKey() != null && pair.getValue() != null)
            .collect(toMap(pair -> pair.getKey().trim(),
                pair -> pair.getValue().trim(),
                (key1, key2) -> key1));
    }

    private Optional<String> getGioMountOutput() {
        try {
            return processExecutor.executePipelineAndGetOutput(
                of("gio", "mount", "-l"),
                of("grep", MTP_PROTOCOL));
        } catch (ProcessExecutionException ex) {
            return ofNullable(ex.getOutput());
        }
    }

    private Optional<String> getMtpMountFolder() {
        try {
            return processExecutor.executePipelineAndGetOutput(
                    of("df", "-a"),
                    of("grep", "gvfsd-fuse"))
                .map(output -> output.split("\\s+"))
                .stream()
                .flatMap(Arrays::stream)
                .reduce((first, second) -> second);
        } catch (ProcessExecutionException ex) {
            return ofNullable(ex.getOutput());
        }
    }

    @Override
    public FileCompatibleApplications findCompatibleApplicationsToExecuteFile(File file) {
        try {
            String mimeType = probeContentType(file.toPath());
            FileCompatibleApplications compatibleApplications =
                Stream.of("/usr/share/applications",
                        System.getProperty("user.home") + "/.local/share/applications")
                    .map(File::new)
                    .filter(File::exists)
                    .filter(File::isDirectory)
                    .map(applicationFolder -> applicationFolder
                        .listFiles((d, name) -> name.endsWith(".desktop")))
                    .filter(Objects::nonNull)
                    .flatMap(Arrays::stream)
                    .map(desktopFile -> getApplicationForDesktopFile(desktopFile, mimeType))
                    .filter(Objects::nonNull)
                    .collect(toMap(
                        Application::getId,
                        identity(),
                        (u, v) -> u,
                        FileCompatibleApplications::new));

            processExecutor.executeAndGetOutput("xdg-mime", "query", "default", mimeType)
                .flatMap(applicationId -> stream(applicationId.split("\\n")).findFirst())
                .map(compatibleApplications::get)
                .ifPresent(compatibleApplications::setDefaultApplication);

            return compatibleApplications;
        } catch (IOException e) {
            throw new UncheckedIOException(format("Failed to find compatible applications for file: %s",
                file), e);
        }
    }

    @Override
    public void setDefaultApplicationToExecuteFile(String applicationId, File file) {
        try {
            String mimeType = probeContentType(file.toPath());
            processExecutor.execute("xdg-mime", "default", applicationId, mimeType);
        } catch (IOException e) {
            throw new UncheckedIOException(
                format("Failed to set the application %s as default to execute file: %s",
                    applicationId, file), e);
        }
    }

    @Override
    public boolean moveToTrash(File file) {
        processExecutor.execute("gio", "trash", file.getAbsolutePath());
        return true;
    }

    @Override
    public void copyPermissions(File fileSource, File fileTarget) {
        processExecutor.execute(
            "chmod", "--reference=" + fileSource.getAbsolutePath(), fileTarget.getAbsolutePath());
    }

    @Override
    public FilePermissions getFilePermissions(final File file) {
        return processExecutor.executeAndGetOutput("ls", "-ld", file.getAbsolutePath())
            .map(ls -> ls.split(" "))
            .filter(parts -> parts.length > 0)
            .map(parts -> FilePermissions.builder()
                .textView(parts[0])
                .ownerRead(parts[0].charAt(1) == 'r')
                .ownerWrite(parts[0].charAt(2) == 'w')
                .ownerExecute(parts[0].charAt(3) == 'x' || parts[0].charAt(3) == 's')
                .groupRead(parts[0].charAt(4) == 'r')
                .groupWrite(parts[0].charAt(5) == 'w')
                .groupExecute(parts[0].charAt(6) == 'x' || parts[0].charAt(6) == 's')
                .othersRead(parts[0].charAt(7) == 'r')
                .othersWrite(parts[0].charAt(8) == 'w')
                .othersExecute(parts[0].charAt(9) == 'x' || parts[0].charAt(9) == 't')
                .setUserId(parts[0].charAt(3) == 's' || parts[0].charAt(3) == 'S')
                .setGroupId(parts[0].charAt(6) == 's' || parts[0].charAt(6) == 'S')
                .sticky(parts[0].charAt(9) == 't' || parts[0].charAt(9) == 'T')
                .ownerName(parts.length > 2 ? parts[2] : null)
                .groupName(parts.length > 3 ? parts[3] : null)
                .availableGroupNames(getAvailableGroupNames(parts.length > 2 ? parts[2] : null))
                .editable(getFileSystemTypeForFile(file)
                    .map(POSIX_COMPLIANT_FILESYSTEMS::contains)
                    .orElse(false))
                .build()
                .updateNumberViews())
            .orElse(null);
    }

    private Optional<String> getFileSystemTypeForFile(final File file) {
        return processExecutor.executeAndGetOutput("df", "-T", file.getAbsolutePath())
            .map(output -> output.split("\\n"))
            .filter(lines -> lines.length > 1)
            .map(lines -> lines[1].split("\\s+"))
            .filter(parts -> parts.length > 1)
            .map(parts -> parts[1]);
    }

    @Override
    public void setPermissions(final File file, final FilePermissions permissions) {
        processExecutor.execute("chmod", permissions.getNumberView(), file.getAbsolutePath());
    }

    @Override
    public void setOwnerForFile(final File file, final String ownerName, final String groupName) {
        if (groupName != null) {
            if (ownerName != null) {
                processExecutor.execute("chown", ownerName + ":" + groupName, file.getAbsolutePath());
            } else {
                processExecutor.execute("chgrp", groupName, file.getAbsolutePath());
            }
        } else if (ownerName != null) {
            processExecutor.execute("chown", ownerName, file.getAbsolutePath());
        }
    }

//    private String[] getAvailableUserNames() {
//        return processExecutor.executeAndGetOutput("cut", "-d:", "-f1", "/etc/passwd")
//            .map(output -> output.split("\\n"))
//            .orElse(new String[0]);
//    }

    private String[] getAvailableGroupNames(final String ownerName) {
        return ofNullable(ownerName)
            .flatMap(user -> processExecutor.executeAndGetOutput("groups", user))
            .map(output -> output.split(":"))
            .filter(parts -> parts.length > 1)
            .map(parts -> parts[1].trim().split("\\s+"))
            .orElse(new String[0]);
    }

    @Override
    public Optional<String> getFileTypeDescription(final Path path, boolean isDirectory) {
        return mimeTypeResolver.getMimeTypes(path, isDirectory)
            .stream()
            .map(this::getMimeTypeDescription)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    public Optional<String> getMimeTypeDescription(final MimeType mimeType) {
        return fileInfoProvider.getFileTypeDescription(mimeType.toString());
    }

    private Application getApplicationForDesktopFile(File desktopFile, String mimeType) {
        try {
            List<String> lines = readAllLines(desktopFile.toPath());
            boolean supportsMime = mimeType != null && lines
                .stream()
                .anyMatch(line -> line.startsWith("MimeType=") && line.contains(mimeType));

            if (!supportsMime) {
                return null;
            }

            String name = findValue(lines, "Name");
            String translatedName = translator.getLocale()
                .map(locale -> ofNullable(findValue(lines, "Name[%s]".formatted(locale.toString())))
                    .orElseGet(() -> findValue(lines, "Name[%s]".formatted(locale.getCountry()))))
                .orElse(null);
            String comment = findValue(lines, "Comment");
            String translatedComment = translator.getLocale()
                .map(locale -> ofNullable(findValue(lines,
                    "Comment[%s]".formatted(locale.toString())))
                    .orElseGet(() -> findValue(lines,
                        "Comment[%s]".formatted(locale.getCountry()))))
                .orElse(null);
            boolean runInterminal = ofNullable(findValue(lines, "Terminal"))
                .map(Boolean::parseBoolean)
                .orElse(false);
            String iconName = findValue(lines, "Icon");
            String executable = findValue(lines, "Exec");
            if (name == null || executable == null) {
                return null;
            }

            String id = desktopFile.getName();
            return new Application(id, name, translatedName, comment, translatedComment,
                executable, X32, iconName, runInterminal);

        } catch (IOException ex) {
            LOG.error("Failed to read desktop file: {}", desktopFile.getAbsolutePath(), ex);
            return null;
        }
    }

    private String findValue(List<String> lines, String name) {
        String prefix = name + "=";
        return lines.stream()
            .filter(line -> line.startsWith(prefix))
            .findFirst()
            .map(line -> line.substring(prefix.length()))
            .map(String::trim)
            .orElse(null);
    }
}
