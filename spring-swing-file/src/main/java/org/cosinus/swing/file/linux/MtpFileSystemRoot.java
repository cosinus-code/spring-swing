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

import org.cosinus.swing.file.DriveVendor;
import org.cosinus.swing.file.FileSystemDevice;
import org.cosinus.swing.file.FileSystemRoot;
import oshi.software.os.OSFileStore;

import java.io.File;
import java.nio.file.Paths;

import static org.cosinus.swing.file.DriveVendor.findByName;
import static org.cosinus.swing.file.FileSystemDevice.REMOVABLE_FLASH;

/**
 * Implementation of {@link OSFileStore} built from the output of "gio mount -li" command line on Linux
 */
public class MtpFileSystemRoot implements FileSystemRoot {

    public static final String MTP = "mtp";

    public static final String MTP_PROTOCOL = MTP + "://";

    public static final String MTP_PROTOCOL_MARK = "-> " + MTP_PROTOCOL;

    public static final String MTP_MOUNT_PREFIX = MTP + ":host=";

    private final String key;

    private final String name;

    private final String mount;

    private final File mountFile;

    public MtpFileSystemRoot(String key, String name, String mtpMountFolder) {
        this.key = key;
        this.name = name;
        this.mountFile = Paths.get(mtpMountFolder, MTP_MOUNT_PREFIX + key).toFile();
        this.mount = mountFile.getAbsolutePath();
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVolume() {
        return "";
    }

    @Override
    public String getLabel() {
        return "";
    }

    @Override
    public String getMountPoint() {
        return mount;
    }

    @Override
    public void setMountPoint(String mountPoint) {
    }

    @Override
    public String getDescription() {
        return name;
    }

    @Override
    public String getType() {
        return MTP;
    }

    @Override
    public String getUuid() {
        return MTP_MOUNT_PREFIX + key;
    }

    @Override
    public long getFreeSpace() {
        return new File(mount).getFreeSpace();
    }

    @Override
    public FileSystemDevice getDevice() {
        return findByName(getName())
            .map(DriveVendor::getDevice)
            .orElse(REMOVABLE_FLASH);
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long getTotalSpace() {
        return new File(mount).getTotalSpace();
    }

    public boolean isMounted() {
        return true;
    }

    public boolean isInternal() {
        return false;
    }

    public boolean isValid() {
        return !key.isBlank() && mountFile.exists();
    }
}
