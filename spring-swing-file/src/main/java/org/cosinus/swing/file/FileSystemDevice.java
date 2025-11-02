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

package org.cosinus.swing.file;

import lombok.Getter;

import static org.cosinus.swing.image.icon.IconProvider.*;

@Getter
public enum FileSystemDevice {
    HARD_DRIVE(ICON_STORAGE_INTERNAL),
    SSD(ICON_STORAGE_INTERNAL_SSD),
    EXTERNAL_DRIVE(ICON_STORAGE_EXTERNAL),
    EXTERNAL_DRIVE_SEAGATE(ICON_STORAGE_EXTERNAL_SEAGATE),
    EXTERNAL_DRIVE_WESTERN_DIGITAL(ICON_STORAGE_EXTERNAL_WESTERN_DIGITAL),
    REMOVABLE_FLASH(ICON_STORAGE_REMOVABLE),
    PHONE(ICON_STORAGE_PHONE),
    WATCH(ICON_STORAGE_WATCH),
    WATCH_GARMIN(ICON_STORAGE_WATCH_GARMIN),
    CDROM(ICON_STORAGE_COMPACT_DISK);

    private final String iconName;

    FileSystemDevice(String iconName) {
        this.iconName = iconName;
    }
}
