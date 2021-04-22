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

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static java.util.Optional.ofNullable;

public class IconThemeIndex extends SectionedProperties {

    public static final String INDEX_THEME_FILE_NAME = "index.theme";

    public static final String ICON_THEME = "Icon Theme";

    public static final String INHERITS = "Inherits";

    private List<Path> iconPaths;

    public IconThemeIndex() {
        this.iconPaths = new ArrayList<>();
    }

    public IconThemeIndex load(File iconThemeFolder) {
        Optional.of(iconThemeFolder)
            .map(File::toPath)
            .map(path -> path.resolve(INDEX_THEME_FILE_NAME))
            .map(Path::toFile)
            .filter(File::exists)
            .ifPresent(this::loadFromIndexFile);

        initIconPaths(iconThemeFolder);
        return this;
    }

    protected void loadFromIndexFile(File indexFile) {
        try (InputStream input = new FileInputStream(indexFile)) {
            load(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void initIconPaths(File iconThemeFolder) {
        iconPaths = ofNullable(iconThemeFolder)
            .map(File::toPath)
            .map(iconThemPath -> concat(Stream.of(iconThemPath),
                                        getIconThemeInherits()
                                            .map(iconThemPath::resolveSibling)))
            .orElseGet(Stream::empty)
            .collect(Collectors.toList());
    }

    public List<Path> getIconPaths() {
        return iconPaths;
    }

    public Stream<String> getIconThemeInherits() {
        return getProperty(ICON_THEME, INHERITS)
            .map(inherits -> inherits.split(","))
            .stream()
            .flatMap(Arrays::stream);
    }

    public Stream<String> getIconInternalPath(IconSize size) {
        return keySet()
            .stream()
            .filter(key -> key.startsWith(size.toString() + "/") ||
                key.endsWith("/" + size.getSize()) ||
                key.startsWith(size.getSize() + "/") ||
                key.endsWith("/" + size.toString()));
    }
}
