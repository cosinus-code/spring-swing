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

package org.cosinus.swing.file.linux;

import org.cosinus.swing.file.mac.BlockDevice;
import org.cosinus.swing.file.DriveVendor;
import org.cosinus.swing.file.FileSystemDevice;
import org.cosinus.swing.file.FileSystemRoot;

import static org.cosinus.swing.file.DriveVendor.findByName;
import static org.cosinus.swing.file.FileSystemDevice.*;

public class LinuxFileSystemRoot implements FileSystemRoot {

    private final BlockDevice blockDevice;

    public LinuxFileSystemRoot(final BlockDevice blockDevice) {
        this.blockDevice = blockDevice;
    }

    @Override
    public String getId() {
        return blockDevice.getPath();
    }

    @Override
    public String getName() {
        return blockDevice.getPath();
    }

    @Override
    public String getVolume() {
        return blockDevice.getPath();
    }

    @Override
    public String getLabel() {
        return blockDevice.getLabel();
    }

    @Override
    public String getMountPoint() {
        return blockDevice.getMountPoint();
    }

    @Override
    public void setMountPoint(String mountPoint) {
        blockDevice.setMountPoint(mountPoint);
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public String getType() {
        return blockDevice.getFileSystemType();
    }

    @Override
    public String getUuid() {
        return blockDevice.getUuid();
    }

    @Override
    public long getFreeSpace() {
        return -1;
    }

    @Override
    public FileSystemDevice getDevice() {
        return blockDevice.isPlugAndPlayDevice() ?
            blockDevice.isRotationalDevice() ?
                blockDevice.isRemovableDevice() ?
                    findByName(blockDevice.getVendor())
                        .map(DriveVendor::getDevice)
                        .orElse(WATCH) :
                    findByName(blockDevice.getVendor())
                        .map(DriveVendor::getDevice)
                        .orElse(EXTERNAL_DRIVE) :
                REMOVABLE_FLASH :
            blockDevice.isRotationalDevice() ?
                HARD_DRIVE :
                SSD;

    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long getTotalSpace() {
        return blockDevice.getSize();
    }

    public boolean isMounted() {
        return blockDevice.getMountPoint() != null;
    }

    public boolean isInternal() {
        return !blockDevice.isRemovableDevice();
    }

}
