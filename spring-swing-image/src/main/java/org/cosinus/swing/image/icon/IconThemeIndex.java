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
import org.cosinus.swing.ui.listener.UIThemeProvider;
import org.cosinus.swing.util.GroupedProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Icon theme index representation.
 * <p>
 * It is used to read icon theme index files in Linux.
 */
public class IconThemeIndex extends GroupedProperties {

    @Serial
    private static final long serialVersionUID = 5424656492329188366L;

    @Autowired
    private UIThemeProvider uiThemeProvider;

    public static final String INDEX_THEME_FILE_NAME = "index.theme";

    public static final String ICON_THEME = "Icon Theme";

    public static final String INHERITS = "Inherits";

    private List<Path> iconPaths;

    private EnumMap<IconSize, List<String>> iconInternalPathMap;

    private Set<String> internalPathsWithoutSize;

    public IconThemeIndex() {
        injectContext(this);
        this.iconPaths = new ArrayList<>();
    }

    public IconThemeIndex load(File iconThemeFolder) {
        loadTheme(iconThemeFolder);
        initIconPaths(iconThemeFolder);
        iconPaths
            .stream()
            .map(Path::toFile)
            .forEach(this::loadTheme);

        this.iconInternalPathMap = new EnumMap<>(stream(IconSize.values())
            .collect(toMap(identity(), size -> keySet()
                .stream()
                .filter(key -> key.startsWith(size + "/") ||
                    key.endsWith("/" + size) ||
                    key.startsWith(size.getSize() + "/") ||
                    key.endsWith("/" + size.getSize()))
                .toList())));

        this.internalPathsWithoutSize = keySet()
            .stream()
            .filter(key -> !key.matches("^\\d.*\\d$"))
            .collect(toSet());

        return this;
    }

    protected void loadTheme(File iconThemeFolder) {
        Optional.of(iconThemeFolder)
            .map(File::toPath)
            .map(path -> path.resolve(INDEX_THEME_FILE_NAME))
            .map(Path::toFile)
            .filter(File::exists)
            .ifPresent(this::loadFromIndexFile);
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
            .toList();
    }

    public List<Path> getIconPaths() {
        return iconPaths;
    }

    public Stream<String> getIconThemeInherits() {
        return concat(
            getProperty(ICON_THEME, INHERITS)
                .map(inherits -> inherits.split(","))
                .stream()
                .flatMap(Arrays::stream),
            uiThemeProvider.getAdditionalIconThemes()
                .map(iconTheme -> uiThemeProvider.isDarkOsTheme() ?
                    iconTheme.darkName() :
                    iconTheme.lightName()));
    }

    public Stream<String> getIconInternalPath(IconSize size) {
        return concat(
            ofNullable(iconInternalPathMap)
                .map(pathsMap -> pathsMap.get(size))
                .stream()
                .flatMap(Collection::stream),
            ofNullable(internalPathsWithoutSize)
                .stream()
                .flatMap(Collection::stream));
    }
}
