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

import org.cosinus.swing.icon.IconSize;

import javax.swing.*;
import java.io.File;
import java.util.Optional;

/**
 * Interface for an icon provider
 */
public interface IconProvider {

    String ICON_FOLDER = "folder";
    String ICON_FILE = "file";
    String ICON_EXECUTABLE = "executable";
    String ICON_GRID = "grid";
    String ICON_BACK = "go-previous";
    String ICON_NEXT = "go-next";
    String ICON_UP = "go-up";
    String ICON_REFRESH = "refresh";
    String ICON_STOP = "stop";
    String ICON_HOME = "home";
    String ICON_NEW_FOLDER = "folder-new";
    String ICON_DELETE = "edit-delete";
    String ICON_FIND = "find";
    String ICON_HIDDEN = "hidden";
    String ICON_PREVIEW = "preview";
    String ICON_VIEW = "view";
    String ICON_NEW_FILE = "new-file";
    String ICON_OPEN = "open";
    String ICON_SAVE = "save";
    String ICON_SAVE_AS = "save-as";
    String ICON_PRINT = "print";
    String ICON_PRINT_PREVIEW = "print-preview";
    String ICON_UNDO = "undo";
    String ICON_REDO = "redo";
    String ICON_CUT = "cut";
    String ICON_COPY = "copy";
    String ICON_PASTE = "paste";
    String ICON_FIND_TEXT = "find-text";
    String ICON_REPLACE = "replace";
    String ICON_SIDEBAR = "sidebar";
    String ICON_PACKAGE = "package";
    String ICON_SHELL_SCRIPT = "shellscript";
    String ICON_TEXT = "text";
    String ICON_UNKNOWN = "unknown";

    String ICON_FONT = "font";
    String ICON_WRAP = "wrap";
    String ICON_EXECUTE = "system-run";
    String ICON_ARCHIVE = "archive";
    String ICON_CONSOLE = "console";
    String ICON_CONNECT = "connect";
    String ICON_DISCONNECT = "disconnect";
    String ICON_CONVERSION = "conversion";
    String ICON_PROPERTIES = "properties";
    String ICON_CLOSE = "close";
    String ICON_EXIT = "exit";

    String ICON_FILE_SERVER = "file-server";
    String ICON_COMPUTER = "computer";
    String ICON_NETWORK = "network";
    String ICON_STORAGE_INTERNAL = "storage-internal";
    String ICON_STORAGE_INTERNAL_SSD = "storage-internal-ssd";
    String ICON_STORAGE_EXTERNAL = "storage-external";
    String ICON_STORAGE_EXTERNAL_SEAGATE = "storage-external-seagate";
    String ICON_STORAGE_EXTERNAL_WESTERN_DIGITAL = "storage-external-wd";
    String ICON_STORAGE_REMOVABLE = "storage-removable";
    String ICON_STORAGE_PHONE = "storage-phone";
    String ICON_STORAGE_WATCH = "storage-watch";
    String ICON_STORAGE_WATCH_GARMIN = "storage-watch-garmin";
    String ICON_STORAGE_COMPACT_DISK = "storage-cdrom";
    String ICON_STORAGE_USB_HD = "storage-usb-hd";
    String ICON_STORAGE_SAS_HD = "storage-sas-hd";
    String ICON_STORAGE_MEMORY_STICK = "storage-memory-stick";
    String ICON_STORAGE_MEMORY_STICK_PRO_DUO = "storage-memory-stick-pro-duo";
    String ICON_STORAGE_MEDIA_FLASH = "storage-compact-flash";
    String ICON_STORAGE_XD = "storage-xd";
    String ICON_STORAGE_SD = "storage-sd";
    String ICON_STORAGE_MINI_SD = "storage-mini-sd";
    String ICON_DATABASE = "database";

    String ICON_VIEW_ICON = "view-icon";
    String ICON_VIEW_GRID = "view-grid";
    String ICON_VIEW_LIST = "view-list";
    String ICON_VIEW_DETAILS = "view-details";
    String ICON_VIEW_TREE = "view-tree";

    String ICON_VIEW_HIDDEN = "view-hidden";
    String ICON_VIEW_LEFT_PANE = "view-left-pane";
    String ICON_SETTINGS = "settings";
    String ICON_HELP = "help";

    String ICON_FORWARD = "insert-link";
    String ICON_MOVE_TO_TRASH = "user-trash";

    /**
     * Initialize this provider.
     * This allows time-consuming operations to be performed with less impact on user experience.
     */
    default void initialize() {
    }

    /**
     * Find the icon corresponding to a file
     *
     * @param file the file
     * @param size the icon size
     * @return the icon
     */
    Optional<Icon> findIconByFile(File file, IconSize size);

    /**
     * Find the icon corresponding to a name
     *
     * @param name the name of the icon
     * @param size the icon size
     * @return the icon
     */
    Optional<Icon> findIconByName(String name, IconSize size);

    default String getFolderIconName(File file) {
        return SpecialFileIcon.byFile(file)
            .map(SpecialFileIcon::getName)
            .orElse(ICON_FOLDER);
    }
}
