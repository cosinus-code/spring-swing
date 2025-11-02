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

import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.file.FileSystemDevice.*;

@Getter
public enum DriveVendor {
    SEAGATE("Seagate", EXTERNAL_DRIVE_SEAGATE),
    WESTERN_DIGITAL("WD", EXTERNAL_DRIVE_WESTERN_DIGITAL),
    GARMIN("Garmin", WATCH_GARMIN),
    SAMSUNG("SAMSUNG Android", PHONE),
    ATA("ATA", HARD_DRIVE);

    private final String name;

    private final FileSystemDevice device;

    DriveVendor(String name, FileSystemDevice device) {
        this.name = name;
        this.device = device;
    }

    public static Optional<DriveVendor> findByName(String name) {
        return ofNullable(name)
            .map(String::trim)
            .filter(not(String::isEmpty))
            .flatMap(deviceName -> stream(values())
                .filter(vendor -> vendor.getName().equalsIgnoreCase(deviceName))
                .findFirst());
    }
}
