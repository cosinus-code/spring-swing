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

import com.sun.jna.platform.win32.Kernel32Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.file.api.Application;
import org.cosinus.swing.file.api.FileCompatibleApplications;
import org.cosinus.swing.window.registry.Registry;
import org.cosinus.swing.window.registry.RegistryRoot;
import org.cosinus.swing.window.shell.WindowsShell;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cosinus.swing.icon.IconSize.X32;
import static org.cosinus.swing.util.FileUtils.getExtension;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_APPLICATIONS;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_DEFAULT_VALUE;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_FILE_EXTS;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_MRU_LIST;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_OPEN_WITH_LIST;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_OPEN_WITH_PROGIDS;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_USER_CHOICE;
import static org.cosinus.swing.window.registry.Registry.REGISTRY_USER_CLASSES;
import static org.cosinus.swing.window.shell.WindowsShell.SHCNE_ASSOCCHANGED;
import static org.cosinus.swing.window.shell.WindowsShell.SHCNF_IDLIST;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.*;

/**
 * The file associations on Windows: the applications which can open a file,
 * like the "Open with" menu of Windows Explorer, and the default application of a file type.
 * <p>
 * The candidate applications of a file extension are, in this order:
 * <ul>
 *     <li>the default application, including the user choice</li>
 *     <li>the ProgIDs from the "OpenWithProgids" registry keys</li>
 *     <li>the executables from the "OpenWithList" registry keys</li>
 * </ul>
 * The applications without an "open" command (e.g. the Store apps) are skipped,
 * except for the default one, which is then opened through the Windows shell.
 * Several associations of the same executable are kept only once.
 * <p>
 * The default application is set in the per-user associations, without any dialog.
 * Windows may still keep its own default application, e.g. for the file types
 * protected by the "UserChoice Protection Driver" on Windows 11, in which case it is only logged.
 */
@Slf4j
public class WindowsFileAssociations {

    /**
     * The file placeholder of an application executable, see {@link Application}.
     */
    static final String FILE_OPTION = "%f";

    /**
     * Opens a file with its default application through the Windows shell.
     */
    static final String SHELL_OPEN_COMMAND = "rundll32 url.dll,FileProtocolHandler " + FILE_OPTION;

    private static final String EXTENSION_SEPARATOR = ".";

    /**
     * The file placeholders of a Windows command: "%1", "%L" and "%V", optionally quoted.
     */
    private static final Pattern FILE_PLACEHOLDER = Pattern.compile("\"?%[1LlVv]\"?");

    /**
     * The other placeholders of a Windows command, not supported: "%*", "%2".."%9", "%I", "%D", "%W", "%H".
     */
    private static final Pattern OTHER_PLACEHOLDER = Pattern.compile("\"?%[*2-9IiDdWwHh]\"?");

    private static final Pattern SPACES = Pattern.compile("\\s+");

    private final WindowsFileAssociationQuery windowsFileAssociationQuery;

    private final Registry registry;

    private final UnaryOperator<String> environmentExpander;

    private final Runnable associationsChangedNotifier;

    public WindowsFileAssociations(final WindowsFileAssociationQuery windowsFileAssociationQuery,
                                   final Registry registry) {
        this(windowsFileAssociationQuery,
            registry,
            Kernel32Util::expandEnvironmentStrings,
            WindowsFileAssociations::notifyAssociationsChanged);
    }

    public WindowsFileAssociations(final WindowsFileAssociationQuery windowsFileAssociationQuery,
                                   final Registry registry,
                                   final UnaryOperator<String> environmentExpander,
                                   final Runnable associationsChangedNotifier) {
        this.windowsFileAssociationQuery = windowsFileAssociationQuery;
        this.registry = registry;
        this.environmentExpander = environmentExpander;
        this.associationsChangedNotifier = associationsChangedNotifier;
    }

    /**
     * Find the applications which can open a file.
     *
     * @param file the file
     * @return the compatible applications mapped by id, with the default application set if known
     */
    public FileCompatibleApplications findCompatibleApplications(final File file) {
        FileCompatibleApplications applications = new FileCompatibleApplications();
        String extension = getExtension(file);
        if (isBlank(extension)) {
            return applications;
        }
        String dotExtension = EXTENSION_SEPARATOR + extension.toLowerCase(Locale.ROOT);
        Set<String> executables = new HashSet<>();

        Optional<Application> defaultApplication = findDefaultApplication(dotExtension);
        defaultApplication.ifPresent(application -> add(applications, executables, application));

        findProgIds(dotExtension).stream()
            .map(progId -> findApplication(progId, ASSOCF_NONE))
            .flatMap(Optional::stream)
            .forEach(application -> add(applications, executables, application));

        findExecutableNames(dotExtension).stream()
            .map(executableName -> findApplication(executableName, ASSOCF_OPEN_BYEXENAME))
            .flatMap(Optional::stream)
            .forEach(application -> add(applications, executables, application));

        defaultApplication.ifPresent(applications::setDefaultApplication);
        return applications;
    }

    /**
     * Set the default application of the file type of a file, for the current user.
     * <p>
     * The ProgID of the application becomes the per-user association of the extension
     * and the "UserChoice" of the extension is removed, so that the per-user association is used.
     * An executable name application (e.g. "notepad.exe") is used through its "Applications" ProgID.
     *
     * @param file          the file
     * @param applicationId the id of the application, as found by {@link #findCompatibleApplications(File)}
     */
    public void setDefaultApplication(final File file, final String applicationId) {
        String extension = getExtension(file);
        if (isBlank(extension) || isBlank(applicationId)) {
            return;
        }
        String dotExtension = EXTENSION_SEPARATOR + extension.toLowerCase(Locale.ROOT);
        Optional<String> progId = toProgId(applicationId);
        if (progId.isEmpty()) {
            log.warn("Cannot set the default application of {}: unknown application {}", dotExtension, applicationId);
            return;
        }
        if (isDefaultProgId(dotExtension, progId.get())) {
            return;
        }

        String classesKey = REGISTRY_USER_CLASSES + dotExtension;
        registry.setStringValue(RegistryRoot.CURRENT_USER, classesKey, REGISTRY_DEFAULT_VALUE, progId.get());
        registry.setStringValue(RegistryRoot.CURRENT_USER, classesKey + REGISTRY_OPEN_WITH_PROGIDS,
            progId.get(), REGISTRY_DEFAULT_VALUE);
        boolean userChoiceRemoved = registry.deleteKey(RegistryRoot.CURRENT_USER,
            REGISTRY_FILE_EXTS + dotExtension + REGISTRY_USER_CHOICE);
        associationsChangedNotifier.run();

        if (!userChoiceRemoved || !isDefaultProgId(dotExtension, progId.get())) {
            log.warn("Windows kept its own default application for {} instead of {}", dotExtension, progId.get());
        }
    }

    /**
     * Get the ProgID of an application id: either the id itself,
     * or the "Applications" ProgID of an executable name.
     */
    private Optional<String> toProgId(final String applicationId) {
        if (!applicationId.startsWith(EXTENSION_SEPARATOR) &&
            registry.keyExists(RegistryRoot.CLASSES_ROOT, applicationId)) {
            return Optional.of(applicationId);
        }
        String applicationProgId = REGISTRY_APPLICATIONS + applicationId;
        return registry.keyExists(RegistryRoot.CLASSES_ROOT, applicationProgId) ?
            Optional.of(applicationProgId) :
            Optional.empty();
    }

    private boolean isDefaultProgId(final String dotExtension, final String progId) {
        return windowsFileAssociationQuery.query(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_PROGID, dotExtension)
            .filter(progId::equalsIgnoreCase)
            .isPresent();
    }

    /**
     * Add an application, unless one with the same id or the same executable was already added.
     */
    private void add(final FileCompatibleApplications applications,
                     final Set<String> executables,
                     final Application application) {
        String executable = getExecutable(application.getExecutable());
        if (!applications.containsKey(application.getId()) &&
            (SHELL_OPEN_COMMAND.equals(application.getExecutable()) || executables.add(executable))) {
            applications.put(application.getId(), application);
        }
    }

    /**
     * Find the default application of an extension.
     * If it has no "open" command, the file is opened through the Windows shell.
     */
    private Optional<Application> findDefaultApplication(final String dotExtension) {
        return findApplication(dotExtension, ASSOCF_INIT_IGNOREUNKNOWN)
            .or(() -> windowsFileAssociationQuery.query(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_FRIENDLYAPPNAME, dotExtension)
                .map(name -> createApplication(
                    windowsFileAssociationQuery.query(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_PROGID, dotExtension)
                        .orElse(dotExtension),
                    name,
                    SHELL_OPEN_COMMAND,
                    null)));
    }

    /**
     * Find the application of an association, if it has an "open" command.
     *
     * @param association the extension, ProgID or executable name
     * @param flags       the ASSOCF flags of the association
     * @return the application, or {@link Optional#empty()} if it has no "open" command
     */
    private Optional<Application> findApplication(final String association, final int flags) {
        return windowsFileAssociationQuery.query(flags, ASSOCSTR_COMMAND, association)
            .map(command -> {
                Optional<String> executablePath = windowsFileAssociationQuery.query(flags, ASSOCSTR_EXECUTABLE, association)
                    .map(environmentExpander);
                String name = windowsFileAssociationQuery.query(flags, ASSOCSTR_FRIENDLYAPPNAME, association)
                    .or(() -> executablePath.map(FilenameUtils::getName))
                    .orElse(association);
                String id = flags == ASSOCF_OPEN_BYEXENAME ?
                    association :
                    windowsFileAssociationQuery.query(flags, ASSOCSTR_PROGID, association).orElse(association);
                return createApplication(id, name, toExecutable(command), executablePath.orElse(null));
            });
    }

    private Application createApplication(final String id,
                                          final String name,
                                          final String executable,
                                          final String iconName) {
        return new Application(id, name, null, null, null, executable, X32, iconName, false);
    }

    /**
     * Convert a Windows command (e.g. {@code "C:\App\app.exe" "%1" %*}) to an application executable,
     * with the file placeholder {@value #FILE_OPTION} and the environment variables expanded.
     * The file placeholder is appended if the command has none.
     *
     * @param command the Windows command
     * @return the application executable
     */
    String toExecutable(final String command) {
        String executable = environmentExpander.apply(command);
        executable = FILE_PLACEHOLDER.matcher(executable).replaceAll(FILE_OPTION);
        executable = OTHER_PLACEHOLDER.matcher(executable).replaceAll("");
        executable = SPACES.matcher(executable.trim()).replaceAll(" ");
        return SPACES.splitAsStream(executable).anyMatch(FILE_OPTION::equals) ?
            executable :
            executable + " " + FILE_OPTION;
    }

    private Set<String> findProgIds(final String dotExtension) {
        Set<String> progIds = new LinkedHashSet<>();
        progIds.addAll(registry.getValues(RegistryRoot.CURRENT_USER,
            REGISTRY_FILE_EXTS + dotExtension + REGISTRY_OPEN_WITH_PROGIDS).keySet());
        progIds.addAll(registry.getValues(RegistryRoot.CLASSES_ROOT,
            dotExtension + REGISTRY_OPEN_WITH_PROGIDS).keySet());
        progIds.removeIf(StringUtils::isBlank);
        return progIds;
    }

    private Set<String> findExecutableNames(final String dotExtension) {
        Set<String> executableNames = new LinkedHashSet<>();
        registry.getValues(RegistryRoot.CURRENT_USER, REGISTRY_FILE_EXTS + dotExtension + REGISTRY_OPEN_WITH_LIST)
            .entrySet()
            .stream()
            .filter(entry -> !REGISTRY_MRU_LIST.equalsIgnoreCase(entry.getKey()))
            .map(Map.Entry::getValue)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .forEach(executableNames::add);
        executableNames.addAll(registry.getKeys(RegistryRoot.CLASSES_ROOT, dotExtension + REGISTRY_OPEN_WITH_LIST));
        executableNames.removeIf(StringUtils::isBlank);
        return executableNames;
    }

    /**
     * Notify Windows Explorer that the file associations have changed.
     */
    private static void notifyAssociationsChanged() {
        try {
            WindowsShell.INSTANCE.SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, null, null);
        } catch (RuntimeException | LinkageError ex) {
            log.debug("Cannot notify the associations change", ex);
        }
    }

    private String getExecutable(final String commandLine) {
        String trimmed = commandLine.trim();
        String executable = trimmed.startsWith("\"") && trimmed.indexOf('"', 1) > 0 ?
            trimmed.substring(1, trimmed.indexOf('"', 1)) :
            SPACES.split(trimmed, 2)[0];
        return executable.toLowerCase(Locale.ROOT);
    }
}
