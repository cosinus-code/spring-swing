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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.icon.IconProvider.*;
import static org.cosinus.swing.image.icon.IconProvider.ICON_DATABASE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_COMPACT_DISK;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_MEDIA_FLASH;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_MEMORY_STICK;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_PHONE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_STORAGE_WATCH;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_DETAILS;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_GRID;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_ICON;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_LEFT_PANE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_VIEW_TREE;

public class KdeIconNameProvider implements IconNameProvider {

    private final Map<String, String> iconNamesMap;

    public KdeIconNameProvider() {
        this.iconNamesMap = new HashMap<>();
    }

    @Override
    public Optional<String> getIconName(String iconId) {
        return ofNullable(iconNamesMap.get(iconId));
    }

    @Override
    public void initialize() {
        iconNamesMap.put(ICON_STORAGE_INTERNAL, "drive-harddisk");
        iconNamesMap.put(ICON_STORAGE_EXTERNAL, "drive-storage-external");
        iconNamesMap.put(ICON_STORAGE_REMOVABLE, "drive-removable-media-usb");
        iconNamesMap.put(ICON_STORAGE_MEMORY_STICK, "drive-removable-media-usb");
        iconNamesMap.put(ICON_STORAGE_MEDIA_FLASH, "media-flash");
        iconNamesMap.put(ICON_STORAGE_PHONE, "phone");
        iconNamesMap.put(ICON_STORAGE_WATCH, "watch");
        iconNamesMap.put(ICON_STORAGE_COMPACT_DISK, "media-optical");
        iconNamesMap.put(ICON_DATABASE, "folder-database");
        iconNamesMap.put(ICON_NETWORK, "folder-network");
        iconNamesMap.put(ICON_GOOGLE, "im-google");
        iconNamesMap.put(ICON_GOOGLE_DRIVE, "folder-onedrive");

        iconNamesMap.put(ICON_VIEW_ICON, "view-grid-symbolic");
        iconNamesMap.put(ICON_VIEW_GRID, "format-justify-fill");
        iconNamesMap.put(ICON_VIEW_DETAILS, "view-list-symbolic");
        iconNamesMap.put(ICON_VIEW_TREE, "view-list-tree");

        iconNamesMap.put(ICON_VIEW_LEFT_PANE, "sidebar-show-symbolic");
        iconNamesMap.put(ICON_SETTINGS, "configure");
        iconNamesMap.put(ICON_PERMISSIONS, "unlock");
        iconNamesMap.put(ICON_HELP, "help-about");
    }
}
