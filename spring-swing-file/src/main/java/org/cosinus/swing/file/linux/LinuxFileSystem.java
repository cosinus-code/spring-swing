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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.DefaultFileSystemRoot;
import org.cosinus.swing.file.api.*;
import org.cosinus.swing.file.mac.BlockDevice;
import org.cosinus.swing.file.mac.BlockDevices;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.translate.Translator;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
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
import static org.cosinus.swing.file.linux.UserShare.ACL_EVERYONE_FULL;
import static org.cosinus.swing.file.linux.UserShare.ACL_EVERYONE_READ;
import static org.cosinus.swing.file.linux.UserShare.GUEST_OK_NO;
import static org.cosinus.swing.file.linux.UserShare.GUEST_OK_YES;
import static org.cosinus.swing.file.linux.UserShare.NET_COMMAND;
import static org.cosinus.swing.file.linux.UserShare.USERSHARE_ADD;
import static org.cosinus.swing.file.linux.UserShare.USERSHARE_COMMAND;
import static org.cosinus.swing.file.linux.UserShare.USERSHARE_DELETE;
import static org.cosinus.swing.file.linux.UserShare.USERSHARE_INFO;
import static org.cosinus.swing.icon.IconSize.X32;

/**
 * Implementation of {@link FileSystem} for Linux
 */
@Slf4j
public class LinuxFileSystem implements FileSystem {

    private final Translator translator;

    private static final Set<String> IGNORED_FILESYSTEMS = Set.of("swap", "vfat");

    private static final String XDG_MIME_COMMAND = "xdg-mime";
    private static final String XDG_MIME_QUERY = "query";
    private static final String XDG_MIME_DEFAULT = "default";

    /**
     * Splits the "net usershare info" output right before every "[name]" header line.
     */
    private static final Pattern USER_SHARE_SECTION_SPLITTER = Pattern.compile("(?m)(?=^\\h*\\[[^\\]\\n]+]\\h*$)");

    /**
     * Matches a "[name]" header line of the "net usershare info" output and captures the share name.
     */
    private static final Pattern USER_SHARE_HEADER = Pattern.compile("^\\s*\\[([^\\]\\n]+)]\\s*$");

    private static final String LINE_SEPARATOR_REGEX = "\\R";
    private static final String PROPERTY_SEPARATOR = "=";

    @SuppressWarnings("SpellCheckingInspection")
    private static final Set<String> POSIX_COMPLIANT_FILESYSTEMS = Set.of(
        "ext4", "ext3", "ext2", "xfs", "btrfs", "jfs", "reiserfs", "zfs", "f2fs", "ufs", "ffs", "fuseblk");

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
                this::getDistinctionKey,
                identity(),
                this::fileSystemRootMerger,
                LinkedHashMap::new));

        return new ArrayList<>(rootsMap.values());
    }

    /**
     * Get the key used to distinguish file system roots: the mount path.
     * Roots that are not mounted (no mount path) are kept distinct by their id,
     * so that they don't collapse into a single entry.
     *
     * @param root the file system root
     * @return the distinction key
     */
    private String getDistinctionKey(final FileSystemRoot root) {
        return isBlank(root.getMountPoint()) ?
            root.getId() :
            root.getMountPoint();
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

        String userId = processExecutor.executeAndGetOutput("id", "-u")
            .map(String::trim)
            .filter(uid -> !uid.equals("0"))
            .orElse(null);

        String mountPoint = "/media/" + userName + "/" + fileSystemRoot.getUuid();
        String[] mkdirCommand = of("mkdir", mountPoint);
        String[] mountCommand = userId != null ?
            of("mount", "-o", "uid=%s,gid=%s,umask=022".formatted(userId, userId),
                fileSystemRoot.getVolume(), mountPoint) :
            of("mount", fileSystemRoot.getVolume(), mountPoint);
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
        //noinspection SpellCheckingInspection
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
            //noinspection SpellCheckingInspection
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
            //noinspection SpellCheckingInspection
            return processExecutor.executePipelineAndGetOutput(
                    of("df", "-a", "-t", "fuse.gvfsd-fuse"),
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
        String mimeType = mimeTypeResolver.mimeType(file.toPath()).orElse(null);
        if (mimeType == null) {
            return null;
        }

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

        processExecutor.executeAndGetOutput(XDG_MIME_COMMAND, XDG_MIME_QUERY, XDG_MIME_DEFAULT, mimeType)
            .flatMap(applicationId -> stream(applicationId.split("\\n")).findFirst())
            .map(compatibleApplications::get)
            .ifPresent(compatibleApplications::setDefaultApplication);

        return compatibleApplications;
    }

    /**
     * Set the default application of the mime type of a file, for the current user, with "xdg-mime default".
     * <p>
     * Nothing is done for a blank application id; an unknown mime type or a failure of "xdg-mime" is only logged.
     *
     * @param applicationId the desktop file name of the application, e.g. "org.gnome.TextEditor.desktop",
     *                      as found by {@link #findCompatibleApplicationsToExecuteFile(File)}
     * @param file          the file
     */
    @Override
    public void setDefaultApplicationToExecuteFile(final String applicationId, final File file) {
        if (isBlank(applicationId)) {
            return;
        }
        mimeTypeResolver.mimeType(file.toPath())
            .filter(StringUtils::isNotBlank)
            .ifPresentOrElse(
                mimeType -> {
                    try {
                        processExecutor.executeAndGetOutput(XDG_MIME_COMMAND, XDG_MIME_DEFAULT, applicationId, mimeType);
                    } catch (ProcessExecutionException ex) {
                        log.warn("Cannot set {} as default application of {}", applicationId, mimeType, ex);
                    }
                },
                () -> log.warn("Cannot set the default application of {}: unknown mime type", file));
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
        Optional<UserShare> userShare = findUserShare(file);
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
                .shared(userShare.isPresent())
                .shareName(userShare.map(UserShare::name).orElse(file.getName()))
                .shareComment(userShare.map(UserShare::comment).orElse(null))
                .allowOthersToEdit(userShare.map(UserShare::allowOthersToEdit).orElse(false))
                .guestAccess(userShare.map(UserShare::guestAccess).orElse(false))
                .build()
                .updateNumberViews())
            .orElse(null);
    }

    /**
     * Find the Samba user share (as created by "net usershare add") for the given directory.
     *
     * @param file the file to check
     * @return the user share of the given directory, or {@link Optional#empty()} if it is not shared
     */
    private Optional<UserShare> findUserShare(final File file) {
        if (!file.isDirectory()) {
            return Optional.empty();
        }
        try {
            Path filePath = file.toPath().toAbsolutePath().normalize();
            return processExecutor.executeAndGetOutput(NET_COMMAND, USERSHARE_COMMAND, USERSHARE_INFO)
                .stream()
                .flatMap(output -> parseUserShares(output).stream())
                .filter(share -> share.path() != null)
                .filter(share -> Path.of(share.path()).toAbsolutePath().normalize().equals(filePath))
                .findFirst();
        } catch (ProcessExecutionException ex) {
            log.debug("Cannot read user shares", ex);
            return Optional.empty();
        }
    }

    /**
     * Parse the output of "net usershare info" into user shares.
     * <p>
     * The output is split into sections, each one starting with a "[name]" header line,
     * and every section is translated into a {@link UserShare}.
     *
     * @param output the "net usershare info" output
     * @return the user shares
     */
    static List<UserShare> parseUserShares(final String output) {
        return USER_SHARE_SECTION_SPLITTER.splitAsStream(output)
            .map(LinuxFileSystem::parseUserShare)
            .flatMap(Optional::stream)
            .toList();
    }

    /**
     * Parse a section of the "net usershare info" output into a user share.
     *
     * @param section the section text, starting with the "[name]" header line
     *                followed by the "key = value" property lines
     * @return the user share, or {@link Optional#empty()} if the section doesn't start with a header line
     */
    private static Optional<UserShare> parseUserShare(final String section) {
        String[] lines = section.split(LINE_SEPARATOR_REGEX);
        if (lines.length == 0) {
            return Optional.empty();
        }
        Matcher header = USER_SHARE_HEADER.matcher(lines[0]);
        if (!header.matches()) {
            return Optional.empty();
        }

        String name = header.group(1).trim();
        Map<String, String> properties = stream(lines)
            .skip(1)
            .map(String::trim)
            .filter(line -> line.contains(PROPERTY_SEPARATOR))
            .collect(toMap(
                line -> substringBefore(line, PROPERTY_SEPARATOR).trim(),
                line -> substringAfter(line, PROPERTY_SEPARATOR).trim(),
                (first, second) -> second));

        return Optional.of(UserShare.of(name, properties));
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

    /**
     * Create, update or delete the Samba user share of the given directory
     * according to the sharing info in the given permissions.
     *
     * @param file        the directory to share
     * @param permissions the permissions containing the sharing info
     */
    @Override
    public void setUserShare(final File file, final FilePermissions permissions) {
        if (!file.isDirectory()) {
            return;
        }
        Optional<UserShare> currentShare = findUserShare(file);
        if (!permissions.isShared()) {
            currentShare.ifPresent(share ->
                processExecutor.execute(NET_COMMAND, USERSHARE_COMMAND, USERSHARE_DELETE, share.name()));
            return;
        }

        String shareName = ofNullable(permissions.getShareName())
            .filter(StringUtils::isNotBlank)
            .map(this::toShareName)
            .or(() -> currentShare.map(UserShare::name))
            .orElseGet(() -> toShareName(file.getName()));

        UserShare newShare = new UserShare(
            shareName,
            file.getAbsolutePath(),
            ofNullable(permissions.getShareComment())
                .orElse(EMPTY),
            permissions.isAllowOthersToEdit(),
            permissions.isGuestAccess());

        if (currentShare.filter(newShare::hasSameSettings).isPresent()) {
            return;
        }

        currentShare
            .filter(share -> !shareName.equals(share.name()))
            .ifPresent(share -> processExecutor.execute(NET_COMMAND, USERSHARE_COMMAND, USERSHARE_DELETE, share.name()));

        processExecutor.execute(NET_COMMAND, USERSHARE_COMMAND, USERSHARE_ADD,
            newShare.name(),
            newShare.path(),
            newShare.comment(),
            newShare.allowOthersToEdit() ? ACL_EVERYONE_FULL : ACL_EVERYONE_READ,
            newShare.guestAccess() ? GUEST_OK_YES : GUEST_OK_NO);
    }

    /**
     * Convert a folder name to a valid Samba share name,
     * replacing the characters not allowed by "net usershare".
     */
    private String toShareName(final String folderName) {
        String shareName = folderName.replaceAll("[%<>*?|/\\\\+=;:\",]", "_").trim();
        return isNotBlank(shareName) ? shareName : "share";
    }

    @Override
    @SuppressWarnings("SpellCheckingInspection")
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
            log.error("Failed to read desktop file: {}", desktopFile.getAbsolutePath(), ex);
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
