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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.io.MimeTypeResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
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
import static javax.imageio.ImageIO.read;
import static org.cosinus.swing.image.icon.IconSize.X16;

/**
 * Implementation of {@link IconProvider} for Linux
 */
public class LinuxIconProvider implements IconProvider {

    private static final Logger LOG = LogManager.getLogger(LinuxIconProvider.class);

    private final ApplicationProperties applicationProperties;

    private final ApplicationUIHandler uiHandler;

    private final MimeTypeResolver mimeTypeResolver;

    private IconThemeIndex iconThemeIndex;

    private final Map<String, String> iconNamesMap;

    public LinuxIconProvider(final ApplicationProperties applicationProperties,
                             final ApplicationUIHandler uiHandler,
                             final MimeTypeResolver mimeTypeResolver) {
        this.applicationProperties = applicationProperties;
        this.uiHandler = uiHandler;
        this.mimeTypeResolver = mimeTypeResolver;

        this.iconThemeIndex = new IconThemeIndex();
        this.iconNamesMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        initPathsToIcons();
        initIconNamesMap();
        ofNullable(System.getProperty("user.home"))
            .map(File::new)
            .flatMap(folder -> findIconByFile(folder, X16))
            .ifPresent(uiHandler::setDefaultFolderIcon);
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return file.isDirectory() ?
            findIconByName(getFolderIconName(file), size)
                .or(() -> findIconByName(ICON_FOLDER, size)) :
            findIconFileByMimeType(file, size)
                .or(() -> getSpecialIconNameByFile(file)
                    .flatMap(iconName -> findIconByName(iconName, size)))
                //TODO: to re-enable magic
                //.or(() -> findIconFileByMagicMimeType(file, size))
                .or(() -> findIconByName(ICON_UNKNOWN, size));
    }

    protected Optional<Icon> findIconFileByMimeType(File file, IconSize size) {
        return ofNullable(file)
            .map(File::toPath)
            .map(mimeTypeResolver::getMimeTypes)
            .map(this::mimeTypesToIconNames)
            .orElseGet(Stream::empty)
            .map(iconName -> findIconByName(iconName, size))
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
        Set<String> iconNames = new LinkedHashSet<>();
        mimeTypes
            .stream()
            .filter(mimeType -> !mimeType.getSubtype().isEmpty())
            .map(mimeType -> mimeType.getType() + "-" + mimeType.getSubtype())
            .forEach(iconNames::add);
        mimeTypes
            .forEach(mimeType -> {
                iconNames.add(mimeType.getType());
                iconNames.add(mimeType.getType() + "-x-generic");
            });
        return iconNames.stream();
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        String iconName = ofNullable(iconNamesMap.get(name))
            .orElse(name);
        return iconThemeIndex()
            .getIconPaths()
            .stream()
            .flatMap(path -> iconThemeIndex()
                .getIconInternalPath(size)
                .map(path::resolve))
            .map(iconsFolder -> getIconFromPath(iconsFolder, iconName, size))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    protected Optional<Icon> getIconFromPath(Path path, String name, IconSize size) {
        return Stream.of("", "gnome-", "gnome-mime-", "gtk-", "stock-")
            .flatMap(prefix -> getIconFile(path, prefix + name))
            .filter(File::exists)
            .findFirst()
            .flatMap(this::createIcon);
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

    protected IconThemeIndex iconThemeIndex() {
        return iconThemeIndex;
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
        iconNamesMap.put(ICON_VIEW_GRID, "view-list-symbolic");
        iconNamesMap.put(ICON_VIEW_LIST, "view-list-details");
        iconNamesMap.put(ICON_VIEW_TREE, "view-list-tree");
    }

    private Optional<File> getIconsThemeFolder() {
        Optional<File> iconsThemeFolder = ofNullable(applicationProperties.getIconsPath())
            .map(File::new)
            .filter(File::exists)
            .or(() -> Stream.of(
                    System.getProperty("user.home") + "/.icons",
                    "/usr/share/icons", // Redhat/Debian/Solaris
                    "/opt/gnome2/share/icons", // SUSE
                    System.getProperty("swing.gtkthomedir") + "/icons")
                .map(Paths::get)
                .map(iconsFolderName -> iconsFolderName.resolve(getIconsTheme()))
                .map(Path::toFile)
                .filter(folder -> folder.exists() && folder.canRead())
                .findFirst());

        if (iconsThemeFolder.isPresent()) {
            LOG.info("Path to icons resolved: " + iconsThemeFolder.get().getAbsolutePath());
        } else {
            LOG.warn("Path to icons unresolved");
        }

        return iconsThemeFolder;
    }

    private String getIconsTheme() {
        return uiHandler.getGnomeIconTheme();
    }

}
