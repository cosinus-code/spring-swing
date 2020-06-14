/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.image.icon;

import org.apache.log4j.Logger;
import org.cosinus.swing.boot.ApplicationProperties;
import org.cosinus.swing.util.FileUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

/**
 * Implementation of {@link IconProvider} for Linux
 */
public class LinuxIconProvider implements IconProvider {

    private static final Logger LOG = Logger.getLogger(LinuxIconProvider.class);

    private static final String DEFAULT_THEME = "Default";

    private static final String GNOME_THEME_NAME_PROPERTY = "gnome.Net/IconThemeName";

    private static final String LINUX_ICONS_FILE = "linux-icons";

    private static final String DESKTOP_EXTENSION = ".desktop";

    private static final String EXECUTABLE_MIMETYPE = "application/x-executable";

    private static final String EXE = "exe";

    private String pathToIcons;

    private Map<String, Map<String, String[]>> iconsMap;

    private Map<String, String> extensionToMimeTypeMap;

    private Map<String, String> extensionToIconNameMap;

    private final IconsMapProvider iconsMapProvider;

    private final ApplicationProperties applicationProperties;

    public LinuxIconProvider(ApplicationProperties applicationProperties,
                             IconsMapProvider iconsMapProvider) {
        this.applicationProperties = applicationProperties;
        this.iconsMapProvider = iconsMapProvider;
    }

    @Override
    public void initialize() {
        initPathToIcons();
        initIconsMap();
        initExtensionsMaps();
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return Optional.ofNullable(FileUtils.getExtension(file))
                .filter(not(String::isEmpty))
                .flatMap(extension -> Optional.ofNullable(getExtensionToIconNameMap().get(extension))
                        .or(() -> file.canExecute() ? getExecutableIconName() : Optional.empty()))
                .flatMap(iconName -> findIconByName(iconName, size))
                .or(() -> findIconByName(file.isDirectory() ? ICON_FOLDER : ICON_FILE, size));
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return pathToIcons()
                .flatMap(iconsFolder -> getIconFromPath(iconsFolder, name, size));
    }

    public Optional<Icon> getIconFromPath(String path, String name, IconSize size) {
        return Optional.ofNullable(getIconsMap().get(name))
                .map(Map::entrySet)
                .stream()
                .flatMap(Collection::stream)
                .flatMap(entry -> Arrays.stream(entry.getValue())
                        .flatMap(key -> Stream.of("", "gnome-", "gtk-", "stock-", "gnome-fs-", "gnome-mime-", "gtk-")
                                .flatMap(prefix -> getIconFromPath(path, size, entry.getKey(), prefix + key))))
                .filter(File::exists)
                .findFirst()
                .map(File::getAbsolutePath)
                .map(ImageIcon::new);
    }

    private Stream<File> getIconFromPath(String path, IconSize size, String category, String name) {
        return Stream.of(".png", ".jpg", ".gif")
                .map(extension -> Path.of(path, size.toString(), category, name + extension))
                .map(Path::toFile);
    }

    private Optional<String> pathToIcons() {
        if (pathToIcons == null) {
            initPathToIcons();
        }
        return Optional.ofNullable(pathToIcons);
    }

    private void initPathToIcons() {
        pathToIcons = Stream.of(
                applicationProperties.getIconsPath(),
                System.getProperty("user.home") + "/.icons",
                "/usr/share/icons", // Redhat/Debian/Solaris
                "/opt/gnome2/share/icons", // SUSE
                System.getProperty("swing.gtkthomedir") + "/icons")
                .flatMap(iconsFolderName -> Stream.of(
                        Optional.ofNullable(Toolkit.getDefaultToolkit().getDesktopProperty(GNOME_THEME_NAME_PROPERTY))
                                .map(Object::toString)
                                .orElse(DEFAULT_THEME),
                        "Bluecurve",
                        "crystalsvg")
                        .map(themeName -> Paths.get(iconsFolderName, themeName))

                )
                .map(Path::toFile)
                .filter(folder -> folder.exists() && folder.canRead())
                .findFirst()
                .map(File::getAbsolutePath)
                .orElse(null);
    }

    private Map<String, Map<String, String[]>> getIconsMap() {
        if (iconsMap == null) {
            initIconsMap();
        }
        return iconsMap;
    }

    private void initIconsMap() {
        iconsMap = iconsMapProvider
                .convert(LINUX_ICONS_FILE)
                .orElse(null);
    }

    private Map<String, String> getExtensionToMimeTypeMap() {
        if (extensionToMimeTypeMap == null) {
            initExtensionsMaps();
        }
        return extensionToMimeTypeMap;
    }

    private Map<String, String> getExtensionToIconNameMap() {
        if (extensionToIconNameMap == null) {
            initExtensionsMaps();
        }
        return extensionToIconNameMap;
    }

    private void initExtensionsMaps() {
        extensionToMimeTypeMap = new HashMap<>();
        extensionToIconNameMap = new HashMap<>();
        fillExtensionToMimeTypeMap();
        extensionToMimeTypeMap.forEach((extension, mimeType) -> findIconNameByMimeType(extension, mimeType)
                .ifPresent(iconName -> extensionToIconNameMap.put(extension, iconName)));
    }

    private void fillExtensionToMimeTypeMap() {
        Stream.of("/usr/share/mime-info/gnome-vfs.mime", //fedora
                  "/opt/gnome/share/mime-info/gnome-vfs.mime", //suse
                  "/usr/share/mime/globs", //fedora
                  "/opt/kde3/share/mime/globs" //suse
        )
                .map(File::new)
                .filter(File::exists)
                .forEach(this::parseMimeTypeFile);
        extensionToMimeTypeMap.put(EXE, EXECUTABLE_MIMETYPE);
    }

    private void parseMimeTypeFile(File mimeTypeFile) {
        AtomicReference<String> mimeType = new AtomicReference<>();
        try (Stream<String> stream = Files.lines(mimeTypeFile.toPath())) {
            stream.map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .forEach(line -> {
                        if (line.startsWith("ext")) {
                            int index = line.indexOf(':');
                            if (index > 2) {
                                Arrays.stream(line.substring(index + 1).split(" "))
                                        .forEach(ext -> extensionToMimeTypeMap.put(ext, mimeType.get()));
                            }
                        } else if (!line.startsWith("regex")) {
                            int index = line.indexOf(":*.");
                            if (index > 0) {
                                extensionToMimeTypeMap.put(line.substring(0, index), line.substring(index + 1));
                            } else {
                                mimeType.set(line);
                            }
                        }
                    });
        } catch (IOException e) {
            LOG.error("Cannot parse mime type file: " + mimeTypeFile.getAbsolutePath(), e);
        }
    }

    private Optional<String> findIconNameByMimeType(String extension, String mimeType) {
        return Stream.of("/usr/share/mimelnk/",
                         "/opt/kde3/share/mimelnk/")
                .flatMap(basePath -> getIconNamePathsForMimeType(basePath, extension, mimeType))
                .map(Path::toFile)
                .map(this::parseMimeLnkFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public Stream<Path> getIconNamePathsForMimeType(String basePath, String extension, String mimeType) {
        int index = mimeType.indexOf('/');
        if (index > 0) {
            String desk = mimeType.substring(index + 1);
            String dir = mimeType.substring(0, index);

            return Stream.of(
                    Paths.get(basePath, mimeType + DESKTOP_EXTENSION),
                    Paths.get(basePath, dir, desk + DESKTOP_EXTENSION),
                    Paths.get(basePath, dir, xFileName(desk + DESKTOP_EXTENSION)),
                    Paths.get(basePath, dir, extension + DESKTOP_EXTENSION),
                    Paths.get(basePath, dir, xFileName(extension + DESKTOP_EXTENSION)));
        }

        return Stream.of(Paths.get(basePath, mimeType + DESKTOP_EXTENSION));
    }

    private String xFileName(String fileName) {
        return fileName.startsWith("x-") ? fileName.substring(2) : "x-" + fileName;
    }

    private Optional<String> parseMimeLnkFile(File mimeLnkFile) {
        try (Stream<String> stream = Files.lines(mimeLnkFile.toPath())) {
            return stream.filter(line -> line.startsWith("Icon="))
                    .findFirst()
                    .map(line -> line.substring(5));
        } catch (IOException e) {
            LOG.error("Cannot parse mime link file: " + mimeLnkFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    private Optional<String> getExecutableIconName() {
        if (extensionToIconNameMap == null) {
            initExtensionsMaps();
        }
        return Optional.ofNullable(extensionToIconNameMap.get(EXE));
    }
}
