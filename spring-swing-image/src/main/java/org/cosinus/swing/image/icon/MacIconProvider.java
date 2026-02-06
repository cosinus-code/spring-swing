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

    private final FileSystem fileSystem;

    private final IcnsImageParser icnsImageParser;

    private final ImageHandler imageHandler;

    private final IconNameProvider iconNameProvider;

    public MacIconProvider(final FileSystem fileSystem,
                           final IcnsImageParser icnsImageParser,
                           final ImageHandler imageHandler,
                           final IconNameProvider iconNameProvider) {
        this.fileSystem = fileSystem;
        this.icnsImageParser = icnsImageParser;
        this.imageHandler = imageHandler;
        this.iconNameProvider = iconNameProvider;
    }

    @Override
    public void initialize() {
        iconNameProvider.initialize();
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
        return iconNameProvider.getIconName(name)
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
}
