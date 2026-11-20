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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Native;
import com.sun.jna.platform.FileUtils;
import com.sun.jna.platform.win32.BaseTSD.DWORD_PTR;
import com.sun.jna.platform.win32.WinNT.SECURITY_DESCRIPTOR_RELATIVE;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.DefaultFileSystemRoot;
import org.cosinus.swing.file.api.FileCompatibleApplications;
import org.cosinus.swing.file.api.FilePermissions;
import org.cosinus.swing.file.api.FileSystem;
import org.cosinus.swing.file.api.FileSystemRoot;
import org.cosinus.swing.window.shell.WindowsShell;
import org.cosinus.swing.window.shell.WindowsShell.ShFileInfo;
import org.springframework.cache.annotation.Cacheable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.sun.jna.platform.win32.Advapi32Util.getAccountBySid;
import static com.sun.jna.platform.win32.Advapi32Util.getFileSecurityDescriptor;
import static com.sun.jna.platform.win32.Advapi32Util.setFileSecurityDescriptor;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.file.attribute.AclEntryFlag.INHERIT_ONLY;
import static java.nio.file.attribute.AclEntryPermission.EXECUTE;
import static java.nio.file.attribute.AclEntryPermission.READ_DATA;
import static java.nio.file.attribute.AclEntryPermission.WRITE_DATA;
import static java.nio.file.attribute.AclEntryType.ALLOW;
import static java.nio.file.attribute.AclEntryType.DENY;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_ADD_SHARE;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_COMMAND;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_ENCODED_COMMAND;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_FIND_SHARES;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_FULL_ACCESS;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_NON_INTERACTIVE;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_NO_PROFILE;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_READ_ACCESS;
import static org.cosinus.swing.exec.WindowsProcessExecutor.POWERSHELL_REMOVE_SHARE;
import static org.cosinus.swing.window.shell.WindowsShell.FILE_ATTRIBUTE_DIRECTORY;
import static org.cosinus.swing.window.shell.WindowsShell.FILE_ATTRIBUTE_NORMAL;
import static org.cosinus.swing.window.shell.WindowsShell.SHGFI_TYPENAME;
import static org.cosinus.swing.window.shell.WindowsShell.SHGFI_USEFILEATTRIBUTES;

/**
 * Implementation of {@link FileSystem} for Windows.
 * <p>
 * Windows has no POSIX permissions, so they are mapped on the ACL of the file:
 * <ul>
 *     <li>owner - the entries of the file owner</li>
 *     <li>group - the entries of the built-in "Users" group</li>
 *     <li>others - the entries of "Everyone"</li>
 * </ul>
 * The set-user-id, set-group-id and sticky bits, the group of a file
 * and the guest access of a share don't exist on Windows and are ignored.
 */
@Slf4j
public class WindowsFileSystem implements FileSystem {

    private static final String SID_USERS = "S-1-5-32-545";
    private static final String SID_EVERYONE = "S-1-1-0";
    private static final String ICACLS_SID_PREFIX = "*";

    private static final String ICACLS_COMMAND = "icacls";
    private static final String ICACLS_GRANT_REPLACE = "/grant:r";
    private static final String ICACLS_REMOVE_GRANT = "/remove:g";

    private static final List<String> ICACLS_READ_RIGHTS = List.of("RD", "RA", "REA", "RC", "S");
    private static final List<String> ICACLS_WRITE_RIGHTS = List.of("WD", "AD", "WA", "WEA", "S");
    private static final List<String> ICACLS_EXECUTE_RIGHTS = List.of("X", "S");

    private static final String DEFAULT_SHARE_NAME = "share";
    private static final int MAX_SHARE_NAME_LENGTH = 80;

    private static final String DIRECTORY_TEXT_VIEW = "d---------";
    private static final String FILE_TEXT_VIEW = "----------";

    private final ProcessExecutor processExecutor;

    private final ObjectMapper objectMapper;

    private final WindowsFileAssociations fileAssociations;

    public WindowsFileSystem(final ProcessExecutor processExecutor,
                             final ObjectMapper objectMapper,
                             final WindowsFileAssociations fileAssociations) {
        this.processExecutor = processExecutor;
        this.objectMapper = objectMapper;
        this.fileAssociations = fileAssociations;
    }

    @Override
    public List<DefaultFileSystemRoot> getFileSystemRoots() {
        return getDefaultFileSystemRoot();
    }

    @Override
    public void mount(FileSystemRoot fileSystemRoot) {
    }

    /**
     * Find the applications which can open a file, like the "Open with" menu of Windows Explorer.
     *
     * @param file the file
     * @return the compatible applications, with the default application set if known
     */
    @Override
    public FileCompatibleApplications findCompatibleApplicationsToExecuteFile(final File file) {
        return fileAssociations.findCompatibleApplications(file);
    }

    /**
     * Set the default application of the file type of a file, for the current user, without any dialog.
     * <p>
     * Windows may keep its own default application for the protected file types (e.g. ".pdf" on Windows 11),
     * in which case it is only logged.
     *
     * @param applicationId the id of the application, as found by {@link #findCompatibleApplicationsToExecuteFile}
     * @param file          the file
     */
    @Override
    public void setDefaultApplicationToExecuteFile(final String applicationId, final File file) {
        fileAssociations.setDefaultApplication(file, applicationId);
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
     * Copy the permissions of a file to another one, like "chmod --reference" on Linux.
     * <p>
     * The explicit ACL entries and the inheritance protection of the source are copied,
     * while the inherited entries of the target are computed again from its own parent.
     * For regular files, the read-only attribute is copied too. The owner is not copied.
     * <p>
     * As on Linux, a failure to copy the permissions doesn't fail the caller, it is only logged.
     *
     * @param fileSource the file to copy the permissions from
     * @param fileTarget the file to copy the permissions to
     */
    @Override
    public void copyPermissions(final File fileSource, final File fileTarget) {
        try {
            SECURITY_DESCRIPTOR_RELATIVE securityDescriptor = getFileSecurityDescriptor(fileSource, false);
            setFileSecurityDescriptor(fileTarget, securityDescriptor,
                false, false, true, false, true, false);
        } catch (RuntimeException ex) {
            log.warn("Cannot copy the ACL from {} to {}", fileSource, fileTarget, ex);
        }

        if (fileSource.isFile() && fileTarget.isFile()) {
            copyReadOnlyAttribute(fileSource.toPath(), fileTarget.toPath());
        }
    }

    private void copyReadOnlyAttribute(final Path source, final Path target) {
        DosFileAttributeView targetView = Files.getFileAttributeView(target, DosFileAttributeView.class);
        if (targetView == null) {
            return;
        }
        try {
            targetView.setReadOnly(Files.readAttributes(source, DosFileAttributes.class).isReadOnly());
        } catch (IOException | UnsupportedOperationException ex) {
            log.warn("Cannot copy the read-only attribute from {} to {}", source, target, ex);
        }
    }

    /**
     * Get the file type description as shown by Windows Explorer, e.g. "Text Document" or "File folder".
     * <p>
     * The description is resolved by the file extension, so the file doesn't need to exist.
     *
     * @param path        the file path
     * @param isDirectory true if the path is a directory
     * @return the file type description, or {@link Optional#empty()} if not available
     */
    @Override
    @Cacheable("spring.swing.file.type.description")
    public Optional<String> getFileTypeDescription(final Path path, final boolean isDirectory) {
        try {
            ShFileInfo fileInfo = new ShFileInfo();
            DWORD_PTR result = WindowsShell.INSTANCE.SHGetFileInfo(
                path.toString(),
                isDirectory ? FILE_ATTRIBUTE_DIRECTORY : FILE_ATTRIBUTE_NORMAL,
                fileInfo,
                fileInfo.size(),
                SHGFI_TYPENAME | SHGFI_USEFILEATTRIBUTES);
            return result.longValue() != 0 ?
                Optional.of(Native.toString(fileInfo.szTypeName)).filter(StringUtils::isNotBlank) :
                Optional.empty();
        } catch (RuntimeException | LinkageError ex) {
            log.debug("Cannot get the file type description of {}", path, ex);
            return Optional.empty();
        }
    }

    /**
     * Get the permissions of a file, mapped from the file ACL,
     * and the SMB share of the file, if it is a shared directory.
     *
     * @param file the file
     * @return the file permissions, or null if the file system doesn't support ACLs
     */
    @Override
    public FilePermissions getFilePermissions(final File file) {
        AclFileAttributeView aclView = Files.getFileAttributeView(file.toPath(), AclFileAttributeView.class);
        if (aclView == null) {
            return null;
        }
        try {
            UserPrincipal owner = aclView.getOwner();
            List<AclEntry> acl = aclView.getAcl();
            Optional<UserPrincipal> users = findPrincipalBySid(SID_USERS);
            Set<AclEntryPermission> ownerPermissions = getPermissions(acl, Optional.of(owner));
            Set<AclEntryPermission> groupPermissions = getPermissions(acl, users);
            Set<AclEntryPermission> othersPermissions = getPermissions(acl, findPrincipalBySid(SID_EVERYONE));
            Optional<WindowsShare> share = findShare(file);

            return FilePermissions.builder()
                .textView(file.isDirectory() ? DIRECTORY_TEXT_VIEW : FILE_TEXT_VIEW)
                .ownerRead(ownerPermissions.contains(READ_DATA))
                .ownerWrite(ownerPermissions.contains(WRITE_DATA))
                .ownerExecute(ownerPermissions.contains(EXECUTE))
                .groupRead(groupPermissions.contains(READ_DATA))
                .groupWrite(groupPermissions.contains(WRITE_DATA))
                .groupExecute(groupPermissions.contains(EXECUTE))
                .othersRead(othersPermissions.contains(READ_DATA))
                .othersWrite(othersPermissions.contains(WRITE_DATA))
                .othersExecute(othersPermissions.contains(EXECUTE))
                .ownerName(owner.getName())
                .groupName(users.map(UserPrincipal::getName).orElse(null))
                .availableGroupNames(new String[0])
                .editable(true)
                .shared(share.isPresent())
                .shareName(share.map(WindowsShare::name).orElse(file.getName()))
                .shareComment(share.map(WindowsShare::comment).orElse(null))
                .allowOthersToEdit(share.map(WindowsShare::allowOthersToEdit).orElse(false))
                .guestAccess(false)
                .build()
                .updateViews();
        } catch (IOException ex) {
            log.debug("Cannot read the permissions of file {}", file, ex);
            return null;
        }
    }

    /**
     * Get the permissions granted by the ACL to a principal, as allowed minus denied permissions.
     * <p>
     * Only the entries of the principal itself are considered, not the ones of the groups it belongs to.
     *
     * @param acl       the ACL
     * @param principal the principal
     * @return the permissions of the principal
     */
    Set<AclEntryPermission> getPermissions(final List<AclEntry> acl, final Optional<UserPrincipal> principal) {
        Set<AclEntryPermission> allowed = EnumSet.noneOf(AclEntryPermission.class);
        Set<AclEntryPermission> denied = EnumSet.noneOf(AclEntryPermission.class);
        principal.ifPresent(user -> acl.stream()
            .filter(entry -> user.equals(entry.principal()))
            .filter(entry -> !entry.flags().contains(INHERIT_ONLY))
            .forEach(entry -> {
                if (entry.type() == ALLOW) {
                    allowed.addAll(entry.permissions());
                } else if (entry.type() == DENY) {
                    denied.addAll(entry.permissions());
                }
            }));
        allowed.removeAll(denied);
        return allowed;
    }

    /**
     * Find a principal by its SID, using the (localized) account name of the SID.
     *
     * @param sid the SID
     * @return the principal, or {@link Optional#empty()} if not found
     */
    private Optional<UserPrincipal> findPrincipalBySid(final String sid) {
        try {
            return Optional.of(FileSystems.getDefault()
                .getUserPrincipalLookupService()
                .lookupPrincipalByName(getAccountBySid(sid).name));
        } catch (IOException | RuntimeException ex) {
            log.debug("Cannot find the account of SID {}", sid, ex);
            return Optional.empty();
        }
    }

    /**
     * Set the owner of a file.
     * <p>
     * Windows files have no group, so the group name is ignored.
     *
     * @param file      the file
     * @param ownerName the new owner name, or null to keep the current owner
     * @param groupName ignored
     */
    @Override
    public void setOwnerForFile(final File file, final String ownerName, final String groupName) {
        if (ownerName == null) {
            return;
        }
        try {
            Path path = file.toPath();
            UserPrincipal owner = path.getFileSystem()
                .getUserPrincipalLookupService()
                .lookupPrincipalByName(ownerName);
            Files.setOwner(path, owner);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Set the permissions of a file by replacing the explicit ACL entries
     * of the owner, the "Users" group and "Everyone".
     * <p>
     * The inherited ACL entries are kept, so they can still grant more permissions.
     *
     * @param file        the file
     * @param permissions the permissions to set
     */
    @Override
    public void setPermissions(final File file, final FilePermissions permissions) {
        try {
            String owner = Files.getOwner(file.toPath()).getName();
            List<String> grants = new ArrayList<>();
            List<String> removes = new ArrayList<>();
            addPermissions(grants, removes, owner,
                permissions.isOwnerRead(), permissions.isOwnerWrite(), permissions.isOwnerExecute());
            addPermissions(grants, removes, ICACLS_SID_PREFIX + SID_USERS,
                permissions.isGroupRead(), permissions.isGroupWrite(), permissions.isGroupExecute());
            addPermissions(grants, removes, ICACLS_SID_PREFIX + SID_EVERYONE,
                permissions.isOthersRead(), permissions.isOthersWrite(), permissions.isOthersExecute());

            if (!grants.isEmpty()) {
                executeIcacls(file, ICACLS_GRANT_REPLACE, grants);
            }
            if (!removes.isEmpty()) {
                executeIcacls(file, ICACLS_REMOVE_GRANT, removes);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void addPermissions(final List<String> grants,
                                final List<String> removes,
                                final String principal,
                                final boolean read,
                                final boolean write,
                                final boolean execute) {
        Set<String> rights = new LinkedHashSet<>();
        if (read) {
            rights.addAll(ICACLS_READ_RIGHTS);
        }
        if (write) {
            rights.addAll(ICACLS_WRITE_RIGHTS);
        }
        if (execute) {
            rights.addAll(ICACLS_EXECUTE_RIGHTS);
        }
        if (rights.isEmpty()) {
            removes.add(principal);
        } else {
            grants.add(principal + ":(" + join(",", rights) + ")");
        }
    }

    private void executeIcacls(final File file, final String option, final List<String> arguments) {
        List<String> command = new ArrayList<>(List.of(ICACLS_COMMAND, file.getAbsolutePath(), option));
        command.addAll(arguments);
        processExecutor.executeAndGetOutput(command.toArray(String[]::new));
    }

    /**
     * Create, update or delete the SMB share of the given directory
     * according to the sharing info in the given permissions.
     * <p>
     * Changing shares requires administrator rights.
     * The guest access is not configurable per share on Windows, so it is ignored.
     *
     * @param file        the directory to share
     * @param permissions the permissions containing the sharing info
     */
    @Override
    public void setUserShare(final File file, final FilePermissions permissions) {
        if (!file.isDirectory()) {
            return;
        }
        Optional<WindowsShare> currentShare = findShare(file);
        if (!permissions.isShared()) {
            currentShare.ifPresent(this::removeShare);
            return;
        }

        String shareName = ofNullable(permissions.getShareName())
            .filter(StringUtils::isNotBlank)
            .map(this::toShareName)
            .or(() -> currentShare.map(WindowsShare::name))
            .orElseGet(() -> toShareName(file.getName()));

        WindowsShare newShare = new WindowsShare(
            shareName,
            file.getAbsolutePath(),
            ofNullable(permissions.getShareComment())
                .orElse(EMPTY),
            permissions.isAllowOthersToEdit());

        if (currentShare.filter(newShare::hasSameSettings).isPresent()) {
            return;
        }

        currentShare.ifPresent(this::removeShare);
        executePowerShell(
            format(POWERSHELL_ADD_SHARE,
                quote(newShare.name()),
                quote(newShare.path()),
                quote(newShare.comment()),
                newShare.allowOthersToEdit() ? POWERSHELL_FULL_ACCESS : POWERSHELL_READ_ACCESS));
    }

    /**
     * Find the SMB share of the given directory.
     *
     * @param file the file to check
     * @return the share of the given directory, or {@link Optional#empty()} if it is not shared
     */
    private Optional<WindowsShare> findShare(final File file) {
        if (!file.isDirectory()) {
            return Optional.empty();
        }
        try {
            return executePowerShell(
                format(POWERSHELL_FIND_SHARES, quote(file.getAbsolutePath())))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .stream()
                .flatMap(output -> stream(parseShares(output)))
                .findFirst();
        } catch (ProcessExecutionException ex) {
            log.debug("Cannot read the shares", ex);
            return Optional.empty();
        }
    }

    WindowsShare[] parseShares(final String output) {
        try {
            return objectMapper.readValue(output, WindowsShare[].class);
        } catch (IOException ex) {
            throw new JsonConvertException(format("Failed to map the shares output: %s", output), ex);
        }
    }

    private void removeShare(final WindowsShare share) {
        executePowerShell(format(POWERSHELL_REMOVE_SHARE, quote(share.name())));
    }

    /**
     * Execute a PowerShell script, passed as encoded command to avoid any quoting issue.
     *
     * @param script the script to execute
     * @return the script output
     */
    private Optional<String> executePowerShell(final String script) {
        return processExecutor.executeAndGetOutput(
            POWERSHELL_COMMAND,
            POWERSHELL_NO_PROFILE,
            POWERSHELL_NON_INTERACTIVE,
            POWERSHELL_ENCODED_COMMAND,
            Base64.getEncoder().encodeToString(script.getBytes(UTF_16LE)));
    }

    /**
     * Quote a text as a PowerShell single-quoted string,
     * doubling the single quotes (including the typographic ones PowerShell accepts).
     *
     * @param text the text to quote
     * @return the quoted text
     */
    String quote(final String text) {
        return "'" + text.replaceAll("(['\u2018\u2019\u201A\u201B])", "$1$1") + "'";
    }

    /**
     * Convert a folder name to a valid Windows share name,
     * replacing the characters not allowed in share names.
     */
    String toShareName(final String folderName) {
        String shareName = folderName.replaceAll("[\\p{Cntrl}\"/\\\\\\[\\]:|<>+=;,?*]", "_").trim();
        if (shareName.length() > MAX_SHARE_NAME_LENGTH) {
            shareName = shareName.substring(0, MAX_SHARE_NAME_LENGTH);
        }
        return isNotBlank(shareName) ? shareName : DEFAULT_SHARE_NAME;
    }
}
