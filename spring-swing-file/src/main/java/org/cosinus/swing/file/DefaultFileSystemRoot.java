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

import oshi.software.os.OSFileStore;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.file.DriveVendor.findByName;
import static org.cosinus.swing.file.FileSystemDevice.HARD_DRIVE;
import static org.cosinus.swing.file.FileSystemDevice.REMOVABLE_FLASH;

public class DefaultFileSystemRoot implements FileSystemRoot {

    private final OSFileStore fileStore;

    private FileSystemDevice fileSystemDevice;

    private String type;

    public DefaultFileSystemRoot(final OSFileStore fileStore) {
        this.fileStore = fileStore;
        this.type = fileStore.getType();
        this.fileSystemDevice = ofNullable(getUuid())
            .filter(not(String::isEmpty))
            .isPresent() ?
            HARD_DRIVE :
            findByName(getLabel())
                .map(DriveVendor::getDevice)
                .orElse(REMOVABLE_FLASH);
    }

    @Override
    public String getId() {
        return fileStore.getVolume();
    }

    @Override
    public String getUuid() {
        return fileStore.getUUID();
    }

    @Override
    public String getName() {
        return fileStore.getName();
    }

    @Override
    public String getDescription() {
        return fileStore.getDescription();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getLabel() {
        return fileStore.getLabel();
    }

    @Override
    public String getVolume() {
        return fileStore.getVolume();
    }

    @Override
    public String getMountPoint() {
        return fileStore.getMount();
    }

    @Override
    public void setMountPoint(String mountPoint) {

    }

    @Override
    public long getTotalSpace() {
        return fileStore.getTotalSpace();
    }

    @Override
    public long getFreeSpace() {
        return fileStore.getFreeSpace();
    }

    @Override
    public FileSystemDevice getDevice() {
        return fileSystemDevice;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public boolean isMounted() {
        return true;
    }

    public void setFileSystemDevice(final FileSystemDevice fileSystemDevice) {
        this.fileSystemDevice = fileSystemDevice;
    }

    public void setType(String type) {
        this.type = type;
    }
}
