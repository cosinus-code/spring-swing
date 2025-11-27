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

package org.cosinus.swing.image.icon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.listener.UIThemeProvider;
import org.springframework.util.MimeType;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static javax.imageio.ImageIO.read;
import static org.cosinus.swing.icon.IconSize.*;

/**
 * Implementation of {@link IconProvider} for Linux
 */
public class LinuxIconProvider implements IconProvider {

    private static final Logger LOG = LogManager.getLogger(LinuxIconProvider.class);

    private final ApplicationProperties applicationProperties;

    private final ApplicationUIHandler uiHandler;

    private final UIThemeProvider uiThemeProvider;

    private final MimeTypeResolver mimeTypeResolver;

    private IconThemeIndex iconThemeIndex;

    private final Map<String, String> iconNamesMap;

    public LinuxIconProvider(final ApplicationProperties applicationProperties,
                             final ApplicationUIHandler uiHandler,
                             final UIThemeProvider uiThemeProvider,
                             final MimeTypeResolver mimeTypeResolver) {
        this.applicationProperties = applicationProperties;
        this.uiHandler = uiHandler;
        this.uiThemeProvider = uiThemeProvider;
        this.mimeTypeResolver = mimeTypeResolver;

        this.iconThemeIndex = new IconThemeIndex();
        this.iconNamesMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        initPathsToIcons();
        initIconNamesMap();
        ofNullable(System.getProperty("java.io.tmpdir"))
            .map(File::new)
            .flatMap(folder -> findIconByFile(folder, X16))
            .ifPresent(uiHandler::setDefaultFolderIcon);
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return file.isDirectory() ?
            findIconByName(getFolderIconName(file), size)
                .or(() -> findIconByName(ICON_FOLDER, size)) :
            findIconFileByMimeType(file, size, false)
                .or(() -> findIconFileByMimeType(file, X48, false))
                .or(() -> findIconFileByMimeType(file, X256, false))
                .or(() -> findIconFileByMimeType(file, null, false))
                .or(() -> findIconFileByMimeType(file, size, true))
                .or(() -> findIconFileByMimeType(file, null, true))
                .or(() -> getSpecialIconNameByFile(file)
                    .flatMap(iconName -> findIconByName(iconName, size)))
                //TODO: to re-enable magic
                //.or(() -> findIconFileByMagicMimeType(file, size))
                .or(() -> findIconByName(ICON_UNKNOWN, size));
    }

    protected Optional<Icon> findIconFileByMimeType(File file, IconSize size, boolean genericMimeType) {
        return iconThemeIndex.getPathsToIcons(size)
            .flatMap(iconsFolder -> ofNullable(file)
                .map(mimeTypeResolver::getMimeTypes)
                .map(mimeTypes -> genericMimeType ?
                    genericMimeTypesToIconNames(mimeTypes) :
                    mimeTypesToIconNames(mimeTypes))
                .orElseGet(Stream::empty)
                .flatMap(iconName -> getIconFromPath(iconsFolder, iconName)))
            .map(this::createIcon)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    protected Optional<Icon> findIconFileByMagicMimeType(File file, IconSize size) {
        return mimeTypeResolver.getMagicMimeType(file)
            .map(this::mimeTypeToIconNames)
            .orElseGet(Stream::empty)
            .map(iconName -> findIconByName(iconName, size))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    protected Optional<String> getSpecialIconNameByFile(File file) {
        if (mimeTypeResolver.isArchive(file)) {
            return Optional.of(ICON_PACKAGE);
        }

        if (mimeTypeResolver.isShellScript(file)) {
            return Optional.of(ICON_SHELL_SCRIPT);
        }

        if (mimeTypeResolver.isTextCompatible(file.toPath())) {
            return Optional.of(ICON_TEXT);
        }
        return Optional.empty();
    }

    protected Stream<String> mimeTypeToIconNames(MimeType mimeType) {
        return mimeTypesToIconNames(singletonList(mimeType));
    }

    protected Stream<String> mimeTypesToIconNames(List<MimeType> mimeTypes) {
        return mimeTypes
            .stream()
            .filter(mimeType -> !mimeType.getSubtype().isEmpty())
            .map(mimeType -> mimeType.getType() + "-" + mimeType.getSubtype());
    }

    protected Stream<String> genericMimeTypesToIconNames(List<MimeType> mimeTypes) {
        return mimeTypes
            .stream()
            .map(MimeType::getType)
            .filter(not("application"::equals));
//            .flatMap(mimeType -> Stream.of(
//                mimeType.getType(),
//                mimeType.getType() + "-x-generic"
//            ));
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return findIconByNameInternal(name, size)
            .or(() -> findIconByNameInternal(name, X48))
            .or(() -> findIconByNameInternal(name, X256))
            .or(() -> findIconByNameInternal(name));
    }

    public Optional<Icon> findIconByNameInternal(String name) {
        return findIconByNameInternal(name, null);
    }

    public Optional<Icon> findIconByNameInternal(String name, IconSize size) {
        String iconName = ofNullable(iconNamesMap.get(name))
            .orElse(name);
        return iconThemeIndex.getPathsToIcons(size)
            .flatMap(iconsFolder -> getIconFromPath(iconsFolder, iconName))
            .map(this::createIcon)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    protected Stream<File> getIconFromPath(Path path, String name) {
        return Stream.of(
                name,
                name + "-symbolic",
                "gnome-" + name,
                "gnome-mime-" + name,
                "gtk-" + name,
                "stock-" + name)
            .flatMap(fileName -> getIconFile(path, fileName))
            .filter(File::exists);
    }

    private Stream<File> getIconFile(Path path, String name) {
        return Stream.of(".svg", ".png", ".jpg")
            .map(extension -> path.resolve(name + extension))
            .map(Path::toFile);
    }

    protected Optional<Icon> createIcon(File file) {
        try {
            LOG.debug("Create icon from file: {}", file.getAbsolutePath());
            return ofNullable(read(file))
                .map(ImageIcon::new);
        } catch (IOException e) {
            LOG.error("Failed to create icon from file: {}", file.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    protected void initPathsToIcons() {
        this.iconThemeIndex = new IconThemeIndex();
        getIconsThemeFolder()
            .ifPresent(iconThemeIndex::load);
    }

    protected void initIconNamesMap() {
        iconNamesMap.put(ICON_STORAGE_INTERNAL, "drive-harddisk");
        iconNamesMap.put(ICON_STORAGE_EXTERNAL, "drive-storage-external");
        iconNamesMap.put(ICON_STORAGE_REMOVABLE, "drive-removable-media-usb");
        iconNamesMap.put(ICON_STORAGE_MEMORY_STICK, "drive-removable-media-usb");
        iconNamesMap.put(ICON_STORAGE_MEDIA_FLASH, "media-flash");
        iconNamesMap.put(ICON_STORAGE_PHONE, "phone");
        iconNamesMap.put(ICON_STORAGE_WATCH, "watch");
        iconNamesMap.put(ICON_STORAGE_COMPACT_DISK, "media-optical");
        iconNamesMap.put(ICON_NETWORK, "network-server");
        iconNamesMap.put(ICON_DATABASE, "sqlitebrowser");

        iconNamesMap.put(ICON_VIEW_ICON, "view-grid-symbolic");
        iconNamesMap.put(ICON_VIEW_GRID, "format-justify-fill");
        iconNamesMap.put(ICON_VIEW_DETAILS, "view-list-symbolic");
        iconNamesMap.put(ICON_VIEW_TREE, "view-list-tree");

        iconNamesMap.put(ICON_VIEW_LEFT_PANE, "sidebar-show-symbolic");
    }

    private Optional<File> getIconsThemeFolder() {
        Optional<File> iconsThemeFolder = ofNullable(applicationProperties.getIconsPath())
            .map(File::new)
            .filter(File::exists)
            .or(() -> Stream.of(
                    System.getProperty("user.home") + "/.icons",
                    "/usr/share/icons",
                    "/opt/gnome2/share/icons", // SUSE
                    ofNullable(System.getProperty("swing.gtkthomedir"))
                        .map(path -> path.concat("/icons"))
                        .orElse(null))
                .filter(Objects::nonNull)
                .map(Paths::get)
                .filter(path -> path.toFile().exists() && path.toFile().canRead())
                .map(iconsFolderName -> getIconsTheme()
                    .map(iconsFolderName::resolve))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Path::toFile)
                .filter(folder -> folder.exists() && folder.canRead())
                .findFirst());

        iconsThemeFolder
            .map(File::getAbsolutePath)
            .ifPresentOrElse(
                path -> LOG.info("Path to icons resolved: {}", path),
                () -> LOG.warn("Path to icons unresolved"));

        return iconsThemeFolder;
    }

    private Optional<String> getIconsTheme() {
        return uiThemeProvider.getIconTheme();
    }

}
