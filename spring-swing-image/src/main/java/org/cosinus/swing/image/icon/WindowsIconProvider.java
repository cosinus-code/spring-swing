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
import static org.cosinus.swing.image.icon.IconSize.X32;
import static org.cosinus.swing.util.AutoRemovableTemporaryFile.autoRemovableTemporaryFileWithExtension;
import static org.cosinus.swing.util.FileUtils.getExtension;
import static org.cosinus.swing.util.WindowsUtils.getRegistryValue;

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

    private Map<String, String> iconNameToFilePathMap;

    private Map<String, Integer> extensionsToIconIndexMap;

    @Override
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return Optional.ofNullable(file)
                .map(f -> {
                    if (size == X16) {
                        return file.exists() || file.isDirectory() ?
                                getSystemIcon(file) :
                                getSystemIcon(getExtension(file));
                    } else if (size == X32) {
                        return getNativeSystemIcon(file, X32.getSize());
                    } else {
                        return null;
                    }
                });
    }

    @Override
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return Optional.ofNullable(getIconNameToFilePathMap().get(name))
                .flatMap(WindowsUtils::getRegistryValue)
                .flatMap(iconFilePath -> createIconFromSystemFile(iconFilePath, size.getSize()))
                .map(bytes -> createIconFromBytes(bytes, size.getSize()));
    }

    private Optional<int[]> createIconFromSystemFile(String systemFilePath, int size) {
        String[] pieces = systemFilePath.split(",");
        String source = pieces[0].startsWith("\"") && pieces[0].endsWith("\"") ?
                pieces[0].substring(1, pieces[0].length() - 1) :
                pieces[0];
        int index = Integer.parseInt(pieces[1]);
        return Optional.ofNullable(getNativeSystemIcon(source, index, size));
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

    private Map<String, String> getIconNameToFilePathMap() {
        if (iconNameToFilePathMap == null) {
            iconNameToFilePathMap = new HashMap<>();
            initIconNameToFilePathMap();
        }
        return iconNameToFilePathMap;
    }

    private void initIconNameToFilePathMap() {
        iconNameToFilePathMap.put(ICON_COMPUTER, ICON_COMPUTER_PATH);
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

        // if nothing found, give up :(
        if (bytes == null) {
            return null;
        }

        return createIconFromBytes(bytes, size);
    }

    private Optional<Integer> getIconIndexByExtension(String extension) {
        return Optional.ofNullable(getExtensionsToIconIndexMap().get(extension));
    }

    private Map<String, Integer> getExtensionsToIconIndexMap() {
        if (extensionsToIconIndexMap == null) {
            extensionsToIconIndexMap = new HashMap<>();
            initExtensionsToIconIndexMap();
        }
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

//    static {
//        try {
//            System.loadLibrary("dll/bigicons");
//        } catch (Error error) {
//            LOG.error("Failed to load native library: bigicons", error);
//        }
//    }
//
//    public static native int[] getNativeSystemIcon(String source, int index, int size);

    public int[] getNativeSystemIcon(String source, int index, int size) {
        return null;
    }
}
