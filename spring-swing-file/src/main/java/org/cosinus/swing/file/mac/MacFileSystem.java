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
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.sun.jna.platform.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.error.ProcessExecutionException;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.file.Application;
import org.cosinus.swing.file.FileCompatibleApplications;
import org.cosinus.swing.file.FileSystem;
import org.cosinus.swing.file.FileSystemRoot;

/**
 * Implementation of {@link FileSystem} for Mac
 */
public class MacFileSystem implements FileSystem {

    private static final Logger LOG = LogManager.getLogger(MacFileSystem.class);

    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String EXECUTABLE = "executable";
    private static final String ICONS = "icons";
    private static final String LOCALIZED_DESCRIPTION = "localizedDescription";
    private static final String BUNDLE_ID = "bundle id";
    private static final String CLAIMED_UTI = "claimed utis";
    private static final String FLAGS = "flags";
    private static final String BUNDLE = "bundle";
    private static final String BINDINGS = "bindings";
    private static final String APPLE_DEFAULT_FLAG = "apple-default";

    private final ProcessExecutor processExecutor;

    private final Map<String, Set<Application>> compatibleApplicationsByContentTypeMap;

    private Map<String, Application> defaultApplicationsByContentTypeMap;

    public MacFileSystem(final ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
        this.compatibleApplicationsByContentTypeMap = buildApplicationsMap();
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
        Optional<String> contentType = findFileContentType(file);
        FileCompatibleApplications compatibleApplications = contentType
            .map(compatibleApplicationsByContentTypeMap::get)
            .map(applications -> applications
                .stream()
                .collect(toMap(
                    Application::getId,
                    identity(),
                    (u, v) -> u,
                    FileCompatibleApplications::new)))
            .orElseGet(FileCompatibleApplications::new);

        contentType
            .map(defaultApplicationsByContentTypeMap::get)
            .or(() -> compatibleApplications
                .values()
                .stream()
                .findFirst())
            .ifPresent(compatibleApplications::setDefaultApplication);

        return compatibleApplications;
    }

    protected Optional<String> findFileContentType(final File file) {
        try {
            return processExecutor.executeAndGetOutput(
                    "mdls", "-name", "kMDItemContentType", "-raw", file.getAbsolutePath())
                .map(uti -> uti.endsWith("%") ? uti.substring(0, uti.length() - 1) : uti);
        } catch (ProcessExecutionException executionException) {
            LOG.warn("Failed to detect the content type of the file '%s'"
                .formatted(file.getAbsolutePath()));
            return Optional.empty();
        }
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

    private Map<String, Set<Application>> buildApplicationsMap() {
        List<Map<String, String>> entries = processExecutor.executeAndGetOutput(
                "/System/Library/Frameworks/CoreServices.framework/Frameworks/" +
                    "LaunchServices.framework/Support/lsregister", "-dump")
            .map(output -> output.split("---+\\n"))
            .stream()
            .flatMap(Arrays::stream)
            .map(this::toKeyValuesMap)
            .toList();

        Map<String, Application> applicationsByBundleMap = entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(PATH))
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE_ID))
            .collect(toMap(
                keyValuesMap -> keyValuesMap.get(BUNDLE_ID),
                this::builApplication,
                (u, v) -> u,
                LinkedHashMap::new));

        defaultApplicationsByContentTypeMap = entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE))
            .filter(keyValueMap -> keyValueMap.containsKey(BINDINGS))
            .filter(keyValueMap -> keyValueMap.containsKey(FLAGS))
            .filter(keyValueMap ->
                keyValueMap.get(FLAGS).contains(APPLE_DEFAULT_FLAG))
            .flatMap(keyValueMap -> stream(keyValueMap.get(BINDINGS).split(",\\s*"))
                .map(binding -> new ImmutablePair<>(
                    binding,
                    applicationsByBundleMap.get(keyValueMap.get(BUNDLE))
                )))
            .filter(pair -> Objects.nonNull(pair.getValue()))
            .collect(toMap(
                Pair::getKey,
                Pair::getValue,
                (u, v) -> v
            ));

        return entries
            .stream()
            .filter(keyValueMap -> keyValueMap.containsKey(BUNDLE_ID))
            .filter(keyValueMap -> keyValueMap.containsKey(CLAIMED_UTI))
            .flatMap(keyValueMap ->
                stream(keyValueMap.get(CLAIMED_UTI).split(",\\s*"))
                    .map(uri -> new ImmutablePair<>(
                        uri,
                        applicationsByBundleMap.get(keyValueMap.get(BUNDLE_ID)))))
            .filter(pair -> Objects.nonNull(pair.getValue()))
            .collect(groupingBy(
                Pair::getKey,
                mapping(Pair::getValue, toSet())));
    }

    private Map<String, String> toKeyValuesMap(String rawApplication) {
        return stream(rawApplication.split("\\n"))
            .filter(line -> !line.startsWith(" "))
            .map(line -> line.split(":\\s+"))
            .filter(linePieces -> linePieces.length > 1)
            .collect(toMap(
                linePieces -> linePieces[0].trim().toLowerCase(),
                linePieces -> linePieces[1].trim(),
                (u, v) -> u));
    }

    private Application builApplication(Map<String, String> keyValueMap) {
        String applicationPath = keyValueMap.get(PATH).substring(
            0, keyValueMap.get(PATH).lastIndexOf(" ("));

        return new Application(
            keyValueMap.get(BUNDLE_ID),
            keyValueMap.get(NAME),
            "\"" + applicationPath + "/" + keyValueMap.get(EXECUTABLE) + "\" %f",
            keyValueMap.get(LOCALIZED_DESCRIPTION),
            keyValueMap.get(NAME),
            keyValueMap.get(LOCALIZED_DESCRIPTION),
            applicationPath + "/" + keyValueMap.get(ICONS),
            false);
    }
}
