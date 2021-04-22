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

import org.cosinus.swing.image.ImageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link IconProvider} for Mac
 */
public class MacIconProvider implements IconProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MacIconProvider.class);

    private static final String ICON_STORAGE_PATH = "/System/Library/Extensions/IOStorageFamily.kext/Contents/Resources/";
    private static final String ICON_SCSI_PATH = "/System/Library/Extensions/IOSCSIArchitectureModelFamily.kext/Contents/Resources";
    private static final String ICON_CORE_PATH = "/System/Library/CoreServices/CoreTypes.bundle/Contents/Resources/";

    private Map<String, String> iconNameToFilePathMap;

    private final ImageHandler imageHandler;

    public MacIconProvider(ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
        this.iconNameToFilePathMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        initIconNameToFilePathMap();
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return Optional.empty();
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return Optional.ofNullable(getIconNameToFilePathMap().get(name))
            .map(File::new)
            .filter(File::exists)
            .flatMap(this::readImage)
            .map(image -> imageHandler.scaleImage(image, size.getSize()))
            .map(ImageIcon::new);
    }

    private Optional<BufferedImage> readImage(File file) {
        try {
            return Optional.of(ImageIO.read(file));
        } catch (IOException e) {
            LOG.error("Cannot read file: " + file);
            return Optional.empty();
        }
    }

    private Map<String, String> getIconNameToFilePathMap() {
        return iconNameToFilePathMap;
    }

    private void initIconNameToFilePathMap() {
        iconNameToFilePathMap.put(ICON_FOLDER, ICON_CORE_PATH + "GenericFolderIcon.icns");

        iconNameToFilePathMap.put(ICON_STORAGE_INTERNAL, ICON_STORAGE_PATH + "Internal.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_EXTERNAL, ICON_STORAGE_PATH + "External.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_REMOVABLE, ICON_STORAGE_PATH + "Removable.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_USB_HD, ICON_SCSI_PATH + "USBHD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_SAS_HD, ICON_SCSI_PATH + "SASHD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MEMORY_STICK, ICON_SCSI_PATH + "MemoryStick.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MEMORY_STICK_PRO_DUO, ICON_SCSI_PATH + "MemoryStickProDuo.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_COMPACT_FLASH, ICON_SCSI_PATH + "CompactFlash.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_XD, ICON_SCSI_PATH + "XD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_SD, ICON_SCSI_PATH + "SD.icns");
        iconNameToFilePathMap.put(ICON_STORAGE_MINI_SD, ICON_SCSI_PATH + "MiniSD.icns");

        iconNameToFilePathMap.put(ICON_COMPUTER, ICON_CORE_PATH + "com.apple.macbookpro-15.icns");
        iconNameToFilePathMap.put(ICON_FILE_SERVER, ICON_CORE_PATH + "GenericFileServerIcon.icns");
        iconNameToFilePathMap.put(ICON_NETWORK, ICON_CORE_PATH + "GenericNetworkIcon.icns");
    }
}
