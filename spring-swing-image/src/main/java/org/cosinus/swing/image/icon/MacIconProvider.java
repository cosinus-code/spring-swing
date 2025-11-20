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

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.icon.SpecialFileIcon.APPLICATIONS;
import static org.cosinus.swing.image.icon.SpecialFileIcon.DESKTOP;
import static org.cosinus.swing.image.icon.SpecialFileIcon.DOCUMENTS;
import static org.cosinus.swing.image.icon.SpecialFileIcon.DOWNLOADS;
import static org.cosinus.swing.image.icon.SpecialFileIcon.HOME;
import static org.cosinus.swing.image.icon.SpecialFileIcon.MUSIC;
import static org.cosinus.swing.image.icon.SpecialFileIcon.PICTURES;
import static org.cosinus.swing.image.icon.SpecialFileIcon.PUBLIC;
import static org.cosinus.swing.image.icon.SpecialFileIcon.VIDEOS;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.file.Application;
import org.cosinus.swing.file.FileCompatibleApplications;
import org.cosinus.swing.file.FileSystem;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.image.ImageHandler;

/**
 * Implementation of {@link IconProvider} for Mac
 */
public class MacIconProvider implements IconProvider {

    private static final Logger LOG = LogManager.getLogger(MacIconProvider.class);

    private static final String ICON_STORAGE_PATH = "/System/Library/Extensions/IOStorageFamily" +
        ".kext/Contents/Resources/";
    private static final String ICON_SCSI_PATH = "/System/Library/Extensions" +
        "/IOSCSIArchitectureModelFamily.kext/Contents/Resources";
    private static final String ICON_CORE_PATH = "/System/Library/CoreServices/CoreTypes" +
        ".bundle/Contents/Resources/";
    private static final String ICON_DOCK_PATH = "/System/Library/CoreServices/Dock" +
        ".app/Contents/Resources/";

    private final FileSystem fileSystem;

    private final Map<String, String> iconNameToFilePathMap;

    private final IcnsImageParser icnsImageParser;

    private final ImageHandler imageHandler;

    public MacIconProvider(final FileSystem fileSystem,
                           final IcnsImageParser icnsImageParser,
                           final ImageHandler imageHandler) {
        this.fileSystem = fileSystem;
        this.icnsImageParser = icnsImageParser;
        this.imageHandler = imageHandler;
        this.iconNameToFilePathMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        initIconNameToFilePathMap();
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return file.isDirectory() ?
            findIconByName(getFolderIconName(file), size)
                .or(() -> findIconByName(ICON_FOLDER, size)) :
            ofNullable(fileSystem.findCompatibleApplicationsToExecuteFile(file))
                .map(FileCompatibleApplications::getDefaultApplication)
                .map(Application::getIconName)
                .map(File::new)
                .flatMap(icnsFile -> readFromIcnsFile(icnsFile, size))
                .<Icon>map(ImageIcon::new)
                .or(() -> findIconByName(ICON_FILE, size));
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return ofNullable(getIconNameToFilePathMap().get(name))
            .map(File::new)
            .filter(File::exists)
            .or(() -> ofNullable(name)
                .map(File::new)
                .filter(File::exists))
            .flatMap(imageFile -> readImage(imageFile, size))
            .map(ImageIcon::new);
    }

    private Optional<BufferedImage> readFromIcnsFile(File imageFile, IconSize size) {
        try {
            return icnsImageParser.getAllBufferedImages(imageFile)
                .stream()
                .filter(image ->
                    image.getWidth() >= size.getSize() &&
                        image.getHeight() >= size.getSize())
                .findFirst()
                .map(image ->
                    image.getWidth() == size.getSize() &&
                        image.getHeight() == size.getSize() ?
                        image :
                        imageHandler.scaleImage(image, size.getSize()));
        } catch (IOException e) {
            LOG.warn("Cannot read image of size {} from icns file {}",
                size.getSize(), imageFile.getPath());
            return Optional.empty();
        }
    }

    private Optional<BufferedImage> readFromImageFile(File imageFile, IconSize size) {
        try {
            return Optional.of(ImageIO.read(imageFile))
                .map(image -> imageHandler.scaleImage(image, size.getSize()));
        } catch (IOException e) {
            LOG.warn(String.format("Cannot read image of size %s from file %s",
                size.getSize(), imageFile.getPath()));
            return Optional.empty();
        }
    }

    private Optional<BufferedImage> readImage(File imageFile, IconSize size) {
        return ofNullable(imageFile)
            .filter(file -> file.getName().endsWith("icns"))
            .flatMap(file -> readFromIcnsFile(file, size))
            .or(() -> readFromImageFile(imageFile, size));
    }

    private Map<String, String> getIconNameToFilePathMap() {
        return iconNameToFilePathMap;
    }

    private void initIconNameToFilePathMap() {
        iconNameToFilePathMap.put(ICON_FOLDER, ICON_CORE_PATH + "GenericFolderIcon.icns");
        iconNameToFilePathMap.put(ICON_FILE, ICON_CORE_PATH + "GenericDocumentIcon.icns");
        iconNameToFilePathMap.put(ICON_EXECUTABLE, ICON_CORE_PATH + "ExecutableBinaryIcon.icns");
        iconNameToFilePathMap.put(ICON_GRID, ICON_CORE_PATH + "GridIcon.icns");

        iconNameToFilePathMap.put(HOME.getName(), ICON_CORE_PATH + "HomeFolderIcon.icns");
        iconNameToFilePathMap.put(APPLICATIONS.getName(), ICON_CORE_PATH +
            "ApplicationsFolderIcon.icns");
        iconNameToFilePathMap.put(DESKTOP.getName(), ICON_CORE_PATH + "DesktopFolderIcon.icns");
        iconNameToFilePathMap.put(DOCUMENTS.getName(), ICON_CORE_PATH + "DocumentsFolderIcon.icns");
        iconNameToFilePathMap.put(DOWNLOADS.getName(), ICON_CORE_PATH + "DownloadsFolderIcon.icns");
        iconNameToFilePathMap.put(MUSIC.getName(), ICON_CORE_PATH + "MusicFolderIcon.icns");
        iconNameToFilePathMap.put(PUBLIC.getName(), ICON_CORE_PATH + "PublicFolderIcon.icns");
        iconNameToFilePathMap.put(PICTURES.getName(), ICON_CORE_PATH + "PicturesFolderIcon.icns");
        iconNameToFilePathMap.put(VIDEOS.getName(), ICON_CORE_PATH + "MovieFolderIcon.icns");

        iconNameToFilePathMap.put(ICON_STORAGE_INTERNAL, ICON_STORAGE_PATH + "Internal.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_EXTERNAL, ICON_STORAGE_PATH + "External.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_REMOVABLE, ICON_STORAGE_PATH + "Removable.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_USB_HD, ICON_SCSI_PATH + "USBHD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_SAS_HD, ICON_SCSI_PATH + "SASHD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MEMORY_STICK, ICON_SCSI_PATH + "MemoryStick.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MEMORY_STICK_PRO_DUO, ICON_SCSI_PATH +
            "MemoryStickProDuo.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MEDIA_FLASH, ICON_SCSI_PATH + "CompactFlash.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_XD, ICON_SCSI_PATH + "XD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_SD, ICON_SCSI_PATH + "SD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MINI_SD, ICON_SCSI_PATH + "MiniSD.icns");

        iconNameToFilePathMap.put(ICON_COMPUTER, ICON_CORE_PATH + "com.apple.macbookpro-15.icns");
        iconNameToFilePathMap.put(ICON_DATABASE, ICON_CORE_PATH + "GenericFileServerIcon.icns");
        iconNameToFilePathMap.put(ICON_NETWORK, ICON_CORE_PATH + "GenericNetworkIcon.icns");

        iconNameToFilePathMap.put(ICON_NEW_FOLDER, ICON_CORE_PATH + "UtilitiesFolder.icns");
        iconNameToFilePathMap.put(ICON_EXECUTE, ICON_CORE_PATH + "ToolbarAdvanced.icns");
        iconNameToFilePathMap.put(ICON_REFRESH, ICON_CORE_PATH + "Sync.icns");
        iconNameToFilePathMap.put(ICON_FORWARD, ICON_CORE_PATH + "ForwardArrowIcon.icns");
        iconNameToFilePathMap.put(ICON_DELETE, ICON_CORE_PATH + "ToolbarDeleteIcon.icns");
        iconNameToFilePathMap.put(ICON_MOVE_TO_TRASH, ICON_CORE_PATH + "FullTrashIcon.icns");
    }
}
