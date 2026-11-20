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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.api.Application;
import org.cosinus.swing.file.api.FileCompatibleApplications;
import org.cosinus.swing.file.api.FileInfoProvider;
import org.cosinus.swing.file.api.FilePermissions;
import org.cosinus.swing.file.api.FileSystem;
import org.cosinus.swing.file.api.FileSystemRoot;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.String.format;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cosinus.swing.file.mac.MacFileInfoProvider.FOLDER_MIME_TYPE;
import static org.cosinus.swing.file.mac.MacShare.*;
import static org.cosinus.swing.util.FileUtils.getExtension;

/**
 * Implementation of {@link FileSystem} for Mac
 */
@Slf4j
public class MacFileSystem implements FileSystem {

    /**
     * The file systems supporting the POSIX permissions, as named by {@link java.nio.file.FileStore#type()}.
     */
    private static final Set<String> POSIX_COMPLIANT_FILESYSTEMS = Set.of("apfs", "hfs", "ufs", "zfs");

    private static final String UNIX_MODE_ATTRIBUTE = "unix:mode";

    private static final int MODE_FILE_TYPE = 61440;
    private static final int MODE_DIRECTORY = 16384;
    private static final int MODE_SYMBOLIC_LINK = 40960;
    private static final int MODE_SET_USER_ID = 2048;
    private static final int MODE_SET_GROUP_ID = 1024;
    private static final int MODE_STICKY = 512;

    /**
     * The permission bits of a mode, including the set-user-id, set-group-id and sticky bits.
     */
    private static final int MODE_PERMISSIONS = 4095;

    private static final String DIRECTORY_TEXT_VIEW = "d---------";
    private static final String SYMBOLIC_LINK_TEXT_VIEW = "l---------";
    private static final String FILE_TEXT_VIEW = "----------";

    private static final String DEFAULT_SHARE_NAME = "share";

    /**
     * The keys of the "sharing -l -f json" output, normalized by {@link #normalizeKey(String)}.
     */
    private static final String JSON_NAME = "name";
    private static final String JSON_PATH = "path";
    private static final String JSON_SMB = "smb";
    private static final String JSON_SHARED = "shared";
    private static final String JSON_GUEST_ACCESS = "guestaccess";
    private static final String JSON_READ_ONLY = "readonly";
    private static final Set<String> JSON_TRUE_TEXTS = Set.of("1", "true", "yes");

    private final ProcessExecutor processExecutor;

    private final MacFileInfoProvider fileTypeInfoProvider;

    private final ObjectMapper objectMapper;

    private final MacApplicationHandler defaultApplicationHandler;

    public MacFileSystem(final ProcessExecutor processExecutor,
                         final FileInfoProvider fileTypeInfoProvider,
                         final ObjectMapper objectMapper) {
        this(processExecutor, fileTypeInfoProvider, objectMapper, new MacLaunchServicesApplicationHandler());
    }

    MacFileSystem(final ProcessExecutor processExecutor,
                  final FileInfoProvider fileTypeInfoProvider,
                  final ObjectMapper objectMapper,
                  final MacApplicationHandler defaultApplicationHandler) {
        this.processExecutor = processExecutor;
        this.fileTypeInfoProvider = (MacFileInfoProvider) fileTypeInfoProvider;
        this.objectMapper = objectMapper;
        this.defaultApplicationHandler = defaultApplicationHandler;
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
    @SuppressWarnings("SpellCheckingInspection")
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
            .filter(MacFileSystemRoot::isValid)
            .toList();
    }

    @SuppressWarnings("SpellCheckingInspection")
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

    /**
     * Set the default application of the uniform type identifier of a file, for all its roles,
     * through the macOS Launch Services.
     * <p>
     * The known default application of the uniform type is updated too,
     * so {@link #findCompatibleApplicationsToExecuteFile(File)} finds it without reloading the registered types.
     * A failure is only logged.
     *
     * @param applicationId the bundle id of the application, as found by
     *                      {@link #findCompatibleApplicationsToExecuteFile(File)}
     * @param file          the file
     */
    @Override
    public void setDefaultApplicationToExecuteFile(final String applicationId, final File file) {
        if (!isNotBlank(applicationId)) {
            return;
        }
        fileTypeInfoProvider.findUniformTypeIdentifier(file)
            .filter(StringUtils::isNotBlank)
            .ifPresentOrElse(
                uniformTypeIdentifier -> {
                    if (defaultApplicationHandler.setDefaultApplication(uniformTypeIdentifier, applicationId)) {
                        ofNullable(fileTypeInfoProvider.getUniformType(uniformTypeIdentifier))
                            .ifPresent(uniformType -> fileTypeInfoProvider.findApplication(applicationId)
                                .ifPresent(uniformType::setDefaultApplication));
                    }
                },
                () -> log.warn("Cannot set the default application of {}: unknown uniform type", file));
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

    /**
     * Copy the permissions of a file to another one, like "chmod --reference" on Linux,
     * which the macOS chmod doesn't support.
     * <p>
     * The permission bits, including the set-user-id, set-group-id and sticky bits, are copied;
     * the owner, the group and the ACL are not.
     * As on Linux, a failure to read the permissions of the source doesn't fail the caller, it is only logged.
     *
     * @param fileSource the file to copy the permissions from
     * @param fileTarget the file to copy the permissions to
     */
    @Override
    public void copyPermissions(final File fileSource, final File fileTarget) {
        try {
            int mode = (Integer) Files.getAttribute(fileSource.toPath(), UNIX_MODE_ATTRIBUTE);
            processExecutor.execute("chmod", Integer.toOctalString(mode & MODE_PERMISSIONS),
                fileTarget.getAbsolutePath());
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException ex) {
            log.warn("Cannot copy the permissions from {} to {}", fileSource, fileTarget, ex);
        }
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

    /**
     * Get the POSIX permissions of a file, with its owner and group,
     * and the share point of the file, if it is a shared directory.
     *
     * @param file the file
     * @return the file permissions, or null if they cannot be read
     */
    @Override
    public FilePermissions getFilePermissions(final File file) {
        try {
            Path path = file.toPath();
            PosixFileAttributes attributes = Files.readAttributes(path, PosixFileAttributes.class, NOFOLLOW_LINKS);
            int mode = (Integer) Files.getAttribute(path, UNIX_MODE_ATTRIBUTE, NOFOLLOW_LINKS);
            String ownerName = attributes.owner().getName();
            Optional<MacShare> share = findShare(file);

            return FilePermissions.builder()
                .textView(textView(mode))
                .ownerRead((mode & 256) != 0)
                .ownerWrite((mode & 128) != 0)
                .ownerExecute((mode & 64) != 0)
                .groupRead((mode & 32) != 0)
                .groupWrite((mode & 16) != 0)
                .groupExecute((mode & 8) != 0)
                .othersRead((mode & 4) != 0)
                .othersWrite((mode & 2) != 0)
                .othersExecute((mode & 1) != 0)
                .setUserId((mode & MODE_SET_USER_ID) != 0)
                .setGroupId((mode & MODE_SET_GROUP_ID) != 0)
                .sticky((mode & MODE_STICKY) != 0)
                .ownerName(ownerName)
                .groupName(attributes.group().getName())
                .availableGroupNames(getAvailableGroupNames(ownerName))
                .editable(isPosixCompliant(path))
                .shared(share.filter(MacShare::smbShared).isPresent())
                .shareName(share.map(MacShare::name).orElse(file.getName()))
                .shareComment(null)
                .allowOthersToEdit(share.map(MacShare::readOnly).map(readOnly -> !readOnly).orElse(false))
                .guestAccess(share.map(MacShare::guestAccess).orElse(false))
                .build()
                .updateViews();
        } catch (IOException | UnsupportedOperationException | IllegalArgumentException ex) {
            log.debug("Cannot read the permissions of file {}", file, ex);
            return null;
        }
    }

    private String textView(final int mode) {
        return switch (mode & MODE_FILE_TYPE) {
            case MODE_DIRECTORY -> DIRECTORY_TEXT_VIEW;
            case MODE_SYMBOLIC_LINK -> SYMBOLIC_LINK_TEXT_VIEW;
            default -> FILE_TEXT_VIEW;
        };
    }

    private boolean isPosixCompliant(final Path path) {
        try {
            return POSIX_COMPLIANT_FILESYSTEMS.contains(Files.getFileStore(path).type());
        } catch (IOException ex) {
            return false;
        }
    }

    private String[] getAvailableGroupNames(final String ownerName) {
        try {
            return ofNullable(ownerName)
                .flatMap(user -> processExecutor.executeAndGetOutput("id", "-Gn", user))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .map(output -> output.split("\\s+"))
                .orElse(new String[0]);
        } catch (ProcessExecutionException ex) {
            log.debug("Cannot read the groups of user {}", ownerName, ex);
            return new String[0];
        }
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

    @Override
    public void setPermissions(final File file, final FilePermissions permissions) {
        processExecutor.execute("chmod", permissions.getNumberView(), file.getAbsolutePath());
    }

    /**
     * Create, update or delete the share point of the given directory, shared over SMB,
     * according to the sharing info in the given permissions.
     * <p>
     * Changing the share points requires administrator rights, so the user is asked for the password.
     * The macOS share points have no comment, so the share comment is ignored;
     * "allow others to edit" is the opposite of the SMB read-only setting.
     *
     * @param file        the directory to share
     * @param permissions the permissions containing the sharing info
     */
    @Override
    public void setUserShare(final File file, final FilePermissions permissions) {
        if (!file.isDirectory()) {
            return;
        }
        Optional<MacShare> currentShare = findShare(file);
        if (!permissions.isShared()) {
            currentShare.ifPresent(this::removeShare);
            return;
        }

        String shareName = ofNullable(permissions.getShareName())
            .filter(StringUtils::isNotBlank)
            .map(this::toShareName)
            .or(() -> currentShare.map(MacShare::name))
            .orElseGet(() -> toShareName(file.getName()));

        MacShare newShare = new MacShare(
            shareName,
            file.getAbsolutePath(),
            true,
            permissions.isGuestAccess(),
            !permissions.isAllowOthersToEdit());

        if (currentShare.filter(newShare::hasSameSettings).isPresent()) {
            return;
        }

        currentShare.ifPresent(this::removeShare);
        processExecutor.executeWithPrivileges(SHARING_COMMAND,
            SHARING_ADD, newShare.path(),
            SHARING_NAME, newShare.name(),
            SHARING_SMB_NAME, newShare.name(),
            SHARING_FLAGS, SHARING_SMB_ENABLED,
            SHARING_GUEST_FLAGS, newShare.guestAccess() ? SHARING_SMB_ENABLED : SHARING_SMB_DISABLED,
            SHARING_READ_ONLY, newShare.readOnly() ? SHARING_TRUE : SHARING_FALSE);
    }

    private void removeShare(final MacShare share) {
        processExecutor.executeWithPrivileges(SHARING_COMMAND, SHARING_REMOVE, share.name());
    }

    /**
     * Find the share point of the given directory.
     *
     * @param file the file to check
     * @return the share point of the given directory, or {@link Optional#empty()} if it is not shared
     */
    private Optional<MacShare> findShare(final File file) {
        if (!file.isDirectory()) {
            return Optional.empty();
        }
        try {
            Path filePath = file.toPath().toAbsolutePath().normalize();
            return processExecutor
                .executeAndGetOutput(SHARING_COMMAND, SHARING_LIST, SHARING_FORMAT, SHARING_FORMAT_JSON)
                .stream()
                .flatMap(output -> parseShares(output).stream())
                .filter(share -> share.path() != null)
                .filter(share -> Path.of(share.path()).toAbsolutePath().normalize().equals(filePath))
                .findFirst();
        } catch (ProcessExecutionException | JsonConvertException ex) {
            log.debug("Cannot read the share points", ex);
            return Optional.empty();
        }
    }

    /**
     * Parse the output of "sharing -l -f json" into share points.
     * <p>
     * Every JSON object with a "path" is a share point record, wherever it is:
     * in an array, or in an object keyed by the share point name.
     * The SMB settings ("shared", "guest access", "read only") are read from the "smb" object of the record,
     * or from the record itself with an "smb" prefix or without it.
     * The keys are compared ignoring the case, the spaces and the separators,
     * and the true values are 1, true or the texts "1", "true", "yes".
     *
     * @param output the "sharing -l -f json" output
     * @return the share points
     */
    List<MacShare> parseShares(final String output) {
        if (output == null || output.isBlank()) {
            return List.of();
        }
        try {
            return findShares(objectMapper.readTree(output), null).toList();
        } catch (IOException ex) {
            throw new JsonConvertException(format("Failed to map the share points output: %s", output), ex);
        }
    }

    private Stream<MacShare> findShares(final JsonNode node, final String key) {
        if (node.isArray()) {
            return StreamSupport.stream(node.spliterator(), false).flatMap(element -> findShares(element, null));
        }
        if (!node.isObject()) {
            return Stream.empty();
        }

        return findField(node, JSON_PATH)
            .filter(JsonNode::isTextual)
            .map(JsonNode::asText)
            .map(s -> findField(node, JSON_NAME)
                .filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .or(() -> ofNullable(key))
                .filter(StringUtils::isNotBlank)
                .map(name -> toShare(node, name, s))
                .stream())
            .orElseGet(() -> node.properties()
                .stream()
                .flatMap(property -> findShares(property.getValue(), property.getKey())));
    }

    private MacShare toShare(final JsonNode record, final String name, final String path) {
        JsonNode smb = findField(record, JSON_SMB)
            .filter(JsonNode::isObject)
            .orElse(null);
        return new MacShare(name, path,
            isSmbSettingTrue(record, smb, JSON_SHARED),
            isSmbSettingTrue(record, smb, JSON_GUEST_ACCESS),
            isSmbSettingTrue(record, smb, JSON_READ_ONLY));
    }

    private boolean isSmbSettingTrue(final JsonNode record, final JsonNode smb, final String setting) {
        return ofNullable(smb)
            .flatMap(smbNode -> findField(smbNode, setting))
            .or(() -> findField(record, JSON_SMB + setting))
            .or(() -> findField(record, setting))
            .map(this::isTrue)
            .orElse(false);
    }

    private Optional<JsonNode> findField(final JsonNode node, final String normalizedKey) {
        return node.properties()
            .stream()
            .filter(property -> normalizeKey(property.getKey()).equals(normalizedKey))
            .map(Map.Entry::getValue)
            .findFirst();
    }

    private boolean isTrue(final JsonNode value) {
        if (value.isBoolean()) {
            return value.asBoolean();
        }
        if (value.isNumber()) {
            return value.asInt() == 1;
        }
        return value.isTextual() && JSON_TRUE_TEXTS.contains(value.asText().trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Normalize a JSON key: lower-cased, without spaces and separators, e.g. "Guest Access" to "guestaccess".
     */
    private static String normalizeKey(final String key) {
        return key
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]", "");
    }

    /**
     * Convert a folder name to a share point name, replacing the characters not allowed in SMB share names.
     */
    String toShareName(final String folderName) {
        String shareName = folderName
            .replaceAll("[\\p{Cntrl}\"/\\\\\\[\\]:|<>+=;,?*]", "_")
            .trim();
        return isNotBlank(shareName) ? shareName : DEFAULT_SHARE_NAME;
    }
}
