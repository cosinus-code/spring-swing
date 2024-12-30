/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.swing.image.icon;

import java.io.File;
import java.util.Optional;

import static java.util.Arrays.stream;
import static org.cosinus.swing.util.FileUtils.setHomeFolder;

public enum SpecialFileIcon {

    HOME("user-home", "~"),
    APPLICATIONS("folder-applications", "~/Applications"),
    DESKTOP("desktop", "~/Desktop"),
    DOCUMENTS("folder-documents", "~/Documents"),
    DOWNLOADS("folder-download", "~/Downloads"),
    MUSIC("folder-music", "~/Music"),
    PICTURES("folder-pictures", "~/Pictures"),
    PUBLIC("folder-publicshare", "~/Public"),
    TEMPLATES("folder-templates", "~/Templates"),
    VIDEOS("folder-videos", "~/Videos");

    private final String filePath;

    private final String name;

    SpecialFileIcon(String name, String filePath) {
        this.filePath = filePath;
        this.name = name;
    }

    public static Optional<SpecialFileIcon> byFile(File file) {
        return stream(values())
            .filter(specialFileIcon -> new File(setHomeFolder(specialFileIcon.filePath)).equals(file))
            .findFirst();
    }

    public String getName() {
        return name;
    }
}
