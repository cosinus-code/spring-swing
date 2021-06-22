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
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;
import org.cosinus.swing.util.WindowsUtils;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS_XP;
import static org.cosinus.swing.image.icon.IconSize.X16;
import static org.cosinus.swing.util.AutoRemovableTemporaryFile.autoRemovableTemporaryFileWithExtension;
import static org.cosinus.swing.util.FileUtils.getExtension;
import static org.cosinus.swing.util.WindowsUtils.getRegistryValue;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link IconProvider} for Windows
 */
public class WindowsIconProvider implements IconProvider {

    private static final Logger LOG = LogManager.getLogger(WindowsIconProvider.class);

    private static final String REGISTRY_HKCR = "HKCR\\";

    private static final String REGISTRY_HKCU_EXPLORER = "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\";

    private static final String REGISTRY_FILE_EXTENSIONS = REGISTRY_HKCU_EXPLORER + "FileExts\\.";

    private static final String REGISTRY_HKCR_SYSTEM_FILE_ASSOCIATIONS = REGISTRY_HKCR + "SystemFileAssociations\\";

    private static final String ICON_COMPUTER_PATH = REGISTRY_HKCU_EXPLORER + "CLSID\\{20D04FE0-3AEA-1069-A2D8-08002B30309D}\\DefaultIcon";

    private static final String WINDOWS_SHELL_PATH = "%SystemRoot%\\System32\\shell32.dll";

    private static final String EXTENSION_EXE = "exe";

    private final Map<String, String> iconNameToRegistryPathMap;

    private final Map<String, Integer> extensionsToIconIndexMap;

    private final ImageHandler imageHandler;

    public WindowsIconProvider(ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
        this.iconNameToRegistryPathMap = new HashMap<>();
        this.extensionsToIconIndexMap = new HashMap<>();
    }

    @Override
    public void initialize() {
        initIconNameToFilePathMap();
        initExtensionsToIconIndexMap();
    }

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return ofNullable(file)
            .map(f -> size == X16 ?
                getSmallSystemIcon(file) :
                getBigSystemIcon(file, size));
    }

    private Icon getSmallSystemIcon(File file) {
        return file.exists() || file.isDirectory() ?
            getSystemIcon(file) :
            getSystemIcon(getExtension(file));
    }

    private Icon getBigSystemIcon(File file, IconSize size) {
        return ofNullable(getSmallSystemIcon(file))
            .filter(icon -> ImageIcon.class.isAssignableFrom(icon.getClass()))
            .map(ImageIcon.class::cast)
            .map(ImageIcon::getImage)
            .map(image -> imageHandler.scaleImage(image, size.getSize()))
            .map(ImageIcon::new)
            .orElse(null);
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return ofNullable(getIconNameToRegistryPathMap().get(name))
            .flatMap(WindowsUtils::getRegistryValue)
            .flatMap(iconFilePath -> createIconFromSystemFile(iconFilePath, size.getSize()))
            .map(bytes -> createIconFromBytes(bytes, size.getSize()));
    }

    private Optional<int[]> createIconFromSystemFile(String systemFilePath, int size) {
        String[] pieces = systemFilePath.split(",");
        if (pieces.length > 1) {
            String source = pieces[0].startsWith("\"") && pieces[0].endsWith("\"") ?
                pieces[0].substring(1, pieces[0].length() - 1) :
                pieces[0];
            int index = Integer.parseInt(pieces[1]);
            return ofNullable(getNativeSystemIcon(source, index, size));
        }

        return Optional.empty();
    }

    private Icon getSystemIcon(String fileExtension) {
        try (AutoRemovableTemporaryFile tmpFile = autoRemovableTemporaryFileWithExtension(fileExtension)) {
            return getSystemIcon(tmpFile.getFile());
        } catch (IOException ex) {
            LOG.error("Cannot create temporary file", ex);
            return null;
        }
    }

    private Icon getSystemIcon(File file) {
        return file.exists() ?
            getSystemIconForExistingFile(file) :
            getSystemIconForExistingFile(new File(System.getProperty("java.io.tmpdir")));
    }

    private Icon getSystemIconForExistingFile(File file) {
        return FileSystemView.getFileSystemView().getSystemIcon(file);
    }

    private Map<String, String> getIconNameToRegistryPathMap() {
        return iconNameToRegistryPathMap;
    }

    private void initIconNameToFilePathMap() {
        iconNameToRegistryPathMap.put(ICON_COMPUTER, ICON_COMPUTER_PATH);
    }

    /**
     * Get the system big icon in Windows corresponding to a given file
     *
     * @param file the file
     * @param size the size of the icon to retrieve
     * @return the corresponding icon
     */
    public Icon getNativeSystemIcon(File file, int size) {
        int[] bytes = null;

        // if this is a directory, I know where is the icon
        if (file.isDirectory()) {
            bytes = getNativeSystemIcon(WINDOWS_SHELL_PATH, 3, size);
        }

        String ext = getExtension(file).toLowerCase();

        // try first directly
        if (ext.equals("exe")) {
            bytes = getNativeSystemIcon(file.getPath(), 0, size);
        }

        if (bytes == null) {
            if (ext.equals("exe")) {
                bytes = getNativeSystemIcon(WINDOWS_SHELL_PATH, 2, size);
            } else {
                bytes = getRegistryValue(REGISTRY_FILE_EXTENSIONS + ext, "ProgID")
                    .map(value -> getRegistryValue(REGISTRY_HKCR + value + "\\DefaultIcon"))
                    .orElseGet(() -> getRegistryValue(REGISTRY_HKCR + "." + ext)
                        .map(value -> getRegistryValue(REGISTRY_HKCR + value + "\\DefaultIcon"))
                        .orElseGet(() -> getRegistryValue(REGISTRY_HKCR + "." + ext, "PerceivedType")
                            .flatMap(value -> getRegistryValue(REGISTRY_HKCR_SYSTEM_FILE_ASSOCIATIONS + value + "\\DefaultIcon"))
                        ))
                    .flatMap(systemFilePath -> createIconFromSystemFile(systemFilePath, size))
                    .orElse(null);
            }
        }

        //if after all these, nothing is found, let's try something manually
        if (bytes == null) {
            bytes = getIconIndexByExtension(ext)
                .map(index -> getNativeSystemIcon(WINDOWS_SHELL_PATH, index, size))
                .orElse(null);
        }

        return bytes != null ? createIconFromBytes(bytes, size) : null;
    }

    private Optional<Integer> getIconIndexByExtension(String extension) {
        return ofNullable(getExtensionsToIconIndexMap().get(extension));
    }

    private Map<String, Integer> getExtensionsToIconIndexMap() {
        return extensionsToIconIndexMap;
    }

    private void initExtensionsToIconIndexMap() {
        extensionsToIconIndexMap.put("doc", 1);
        extensionsToIconIndexMap.put("exe", 2);
        extensionsToIconIndexMap.put("com", 2);
        extensionsToIconIndexMap.put("hlp", IS_OS_WINDOWS_XP ? 29 : 23);
        extensionsToIconIndexMap.put("ini", 69);
        extensionsToIconIndexMap.put("inf", 69);
        extensionsToIconIndexMap.put("txt", 70);
        extensionsToIconIndexMap.put("bat", 71);
        extensionsToIconIndexMap.put("dll", 72);
        extensionsToIconIndexMap.put("sys", 72);
        extensionsToIconIndexMap.put("vbx", 72);
        extensionsToIconIndexMap.put("ocx", 72);
        extensionsToIconIndexMap.put("vxd", 72);
        extensionsToIconIndexMap.put("fon", 73);
        extensionsToIconIndexMap.put("ttf", 74);
        extensionsToIconIndexMap.put("fot", 75);
    }

    /**
     * Create icon from bytes array
     *
     * @param bytes the bytes
     * @param size  the icon size to create
     * @return the created icon
     */
    public static Icon createIconFromBytes(int[] bytes, int size) {
        int len = 4096;
        int[] bits1 = new int[len];
        int[] bits2 = new int[len];
        System.arraycopy(bytes, 0, bits1, 0, len);
        System.arraycopy(bytes, len, bits2, 0, len);

        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        bi.setRGB(0, 0, size, size, bits1, 0, size);
        boolean totalTransparent = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if ((bi.getRGB(i, j) | 0xff0000) < 0xff0000) totalTransparent = false;
            }
        }
        if (totalTransparent) bi.setRGB(0, 0, size, size, bits2, 0, size);
        return new ImageIcon(bi);
    }

    public int[] getNativeSystemIcon(String source, int index, int size) {
        //TODO:
        return null;
    }
}
