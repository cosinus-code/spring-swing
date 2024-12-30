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
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    private final ProcessExecutor processExecutor;

    private IconThemeIndex iconThemeIndex;

    private final Map<String, String> iconNamesMap;

    public LinuxIconProvider(ApplicationProperties applicationProperties,
                             ApplicationUIHandler uiHandler,
                             ProcessExecutor processExecutor) {
        this.applicationProperties = applicationProperties;
        this.uiHandler = uiHandler;
        this.processExecutor = processExecutor;

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
            getFileMimeType(file)
                .stream()
                .flatMap(this::mimeTypeToIconNames)
                .map(iconName -> findIconByName(iconName, size))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .or(() -> findIconByName(ICON_FILE, size));
    }

    protected String getFolderIconName(File file) {
        return SpecialFileIcon.byFile(file)
            .map(SpecialFileIcon::getName)
            .orElse(ICON_FOLDER);
    }

    protected Optional<String> getFileMimeType(File file) {
        return processExecutor.executeAndGetOutput("file", "--mime-type", file.getAbsolutePath())
            .map(output -> output.substring(output.lastIndexOf(" ") + 1))
            .or(() -> processExecutor.executeAndGetOutput("xdg-mime", "query", "filetype", file.getAbsolutePath()))
            .map(output -> output.replaceAll("\\n", ""));
    }

    protected Stream<String> mimeTypeToIconNames(String mimeType) {
        String iconName = mimeType.replace("/", "-");
        int index = mimeType.indexOf("/");
        return index > 0 ?
            Stream.of(iconName, mimeType.substring(0, index)) :
            Stream.of(iconName);
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        String iconName = ofNullable(iconNamesMap.get(name))
            .orElse(name);
        return iconThemeIndex()
            .getIconPaths()
            .stream()
            .map(iconsFolder -> getIconFromPath(iconsFolder, iconName, size))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    protected Optional<Icon> getIconFromPath(Path path, String name, IconSize size) {
        return iconThemeIndex()
            .getIconInternalPath(size)
            .map(path::resolve)
            .flatMap(filePath -> Stream.of("", "gnome-", "gnome-mime-", "gtk-", "stock-")
                .flatMap(prefix -> getIconFileName(filePath, prefix + name)))
            .filter(File::exists)
            .findFirst()
            .flatMap(this::createIcon);
    }

    private Stream<File> getIconFileName(Path path, String name) {
        return Stream.of(".svg", ".png", ".jpg")
            .map(extension -> path.resolve(name + extension))
            .map(Path::toFile);
    }

    protected Optional<Icon> createIcon(File file) {
        try {
            LOG.debug("Create icon from file: " + file.getAbsolutePath());
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
        iconNamesMap.put(ICON_STORAGE_EXTERNAL, "drive-removable-media");
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
