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
package org.cosinus.swing.file.mac;

import org.apache.commons.lang3.StringUtils;
import org.cosinus.swing.file.FileSystemDevice;
import org.cosinus.swing.file.FileSystemRoot;
import oshi.software.os.OSFileStore;

import java.util.HashMap;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.file.FileSystemDevice.EXTERNAL_DRIVE;
import static org.cosinus.swing.file.FileSystemDevice.HARD_DRIVE;

/**
 * Implementation of {@link OSFileStore} built from the output of "diskutil info <id>" command line on Mac
 */
public class MacFileSystemRoot extends HashMap<String, String> implements FileSystemRoot {
    private static final String DISK_ROOT_NAME = "Volume Name";
    private static final String DISK_ROOT_MOUNT = "Mount Point";
    private static final String DISK_ROOT_TYPE = "Type (Bundle)";
    private static final String DISK_ROOT_VOLUME = "Device Node";
    private static final String DISK_ROOT_UUID = "Volume UUID";
    private static final String DISK_ROOT_TOTAL_SPACE = "Container Total Space";
    private static final String DISK_ROOT_FREE_SPACE = "Container Free Space";
    private static final String DISK_ROOT_INTERNAL = "Internal";
    private static final String DISK_ROOT_LOCATION = "Device Location:";
    private static final String DISK_ROOT_MOUNTED = "Mounted";

    private static final String YES = "Yes";

    private Long totalSpace;

    private Long freeSpace;

    private Boolean mounted;

    @Override
    public String getId() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getName() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getVolume() {
        return get(DISK_ROOT_VOLUME);
    }

    @Override
    public String getLabel() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getMountPoint() {
        return get(DISK_ROOT_MOUNT);
    }

    @Override
    public void setMountPoint(String mountPoint) {
        put(DISK_ROOT_MOUNT, mountPoint);
    }

    @Override
    public String getDescription() {
        return get(DISK_ROOT_NAME);
    }

    @Override
    public String getType() {
        return get(DISK_ROOT_TYPE);
    }

    @Override
    public String getUuid() {
        return get(DISK_ROOT_UUID);
    }

    @Override
    public long getFreeSpace() {
        if (freeSpace == null) {
            freeSpace = getRawLong(DISK_ROOT_FREE_SPACE);
        }
        return freeSpace;
    }

    @Override
    public long getTotalSpace() {
        if (totalSpace == null) {
            totalSpace = getRawLong(DISK_ROOT_TOTAL_SPACE);
        }
        return totalSpace;
    }

    @Override
    public boolean isHidden() {
        return getMountPoint().startsWith("/System/Volumes/");
    }

    @Override
    public FileSystemDevice getDevice() {
        //internal = DISK_ROOT_INTERNAL.equals(get(DISK_ROOT_LOCATION));
        return !getMountPoint().startsWith("/Volumes/") ? HARD_DRIVE : EXTERNAL_DRIVE;
    }

    public boolean isMounted() {
        if (mounted == null) {
            mounted = YES.equals(get(DISK_ROOT_MOUNTED));
        }
        return mounted;
    }

    public boolean isValid() {
        return !StringUtils.isEmpty(getMountPoint()) && isMounted();
    }

    private long getRawLong(String propertyName) {
        return ofNullable(get(propertyName))
            .map(totalSpaceProperty -> totalSpaceProperty.split(" "))
            .map(rawFreeSpace -> rawFreeSpace[2].substring(1))
            .map(Long::parseLong)
            .orElse(-1L);
    }

}
