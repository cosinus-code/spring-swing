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

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.ui.listener.UIThemeProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.concat;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Icon theme index representation.
 * <p>
 * It is used to read icon theme index files in Linux.
 */
public class IconThemeIndex {

    @Autowired
    private UIThemeProvider uiThemeProvider;

    public static final String INDEX_THEME_FILE_NAME = "index.theme";

    public static final String ICON_THEME_INHERITS = "Icon Theme.Inherits";

    private final List<Path> iconPaths;

    private final EnumMap<IconSize, Set<String>> iconInternalPathMap;

    private final Set<String> internalPathsWithoutSize;

    public IconThemeIndex() {
        injectContext(this);
        this.iconPaths = new ArrayList<>();
        this.iconInternalPathMap = new EnumMap<>(IconSize.class);
        this.internalPathsWithoutSize = new HashSet<>();
    }

    public IconThemeIndex load(File iconThemeFolder) {
        ofNullable(iconThemeFolder)
            .map(this::loadTheme)
            .stream()
            .flatMap(index -> getSiblingIconThemes(iconThemeFolder, index).stream())
            .forEach(this::loadTheme);

        return this;
    }

    protected boolean isPathForSize(String path, IconSize iconSize) {
        String size = "" + iconSize.getSize();
        return path.matches("^" + size + "[x/].*") ||
            path.matches(".*" + "[x/]" + size + "@?2?x?$");
    }

    protected boolean isPathWithoutSize(String path) {
        return !path.matches("^\\d.*") &&
            !path.matches(".*\\d$") &&
            !path.matches(".*@\\dx$");
    }

    protected INIConfiguration loadTheme(File iconThemeFolder) {
        iconPaths.add(iconThemeFolder.toPath());

        INIConfiguration iconThemeIndex = Optional.of(iconThemeFolder)
            .map(File::toPath)
            .map(path -> path.resolve(INDEX_THEME_FILE_NAME))
            .map(Path::toFile)
            .filter(File::exists)
            .map(this::parseIconThemeIndexFile)
            .orElse(null);

        if (iconThemeIndex != null) {
            iconThemeIndex.getSections()
                .forEach(path -> stream(IconSize.values())
                    .filter(size -> isPathForSize(path, size))
                    .findFirst()
                    .ifPresentOrElse(size ->
                            iconInternalPathMap
                                .computeIfAbsent(size, k -> new HashSet<>())
                                .add(path),
                        () -> {
                            if (isPathWithoutSize(path)) {
                                internalPathsWithoutSize.add(path);
                            }
                        }));
        }

        return iconThemeIndex;
    }

    protected INIConfiguration parseIconThemeIndexFile(File indexIconThemeFile) {
        try {
            INIConfiguration index = new INIConfiguration();
            FileHandler handler = new FileHandler(index);
            handler.load(indexIconThemeFile);

            return index;
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> getSiblingIconThemes(File iconThemeFolder, INIConfiguration index) {
        return ofNullable(iconThemeFolder)
            .map(File::toPath)
            .map(iconThemPath -> getSiblingIconThemeNames(index)
                .map(iconThemPath::resolveSibling))
            .orElseGet(Stream::empty)
            .map(Path::toFile)
            .toList();
    }

    public Stream<String> getSiblingIconThemeNames(INIConfiguration index) {
        return concat(
            ofNullable(index.getString(ICON_THEME_INHERITS))
                .map(inherits -> inherits.split(","))
                .stream()
                .flatMap(Arrays::stream),
            uiThemeProvider.getMainIconThemes()
                .map(iconTheme -> uiThemeProvider.isDarkOsTheme() ?
                    iconTheme.darkName() :
                    iconTheme.lightName()));
    }

    public List<Path> getPathsToIcons(IconSize size) {
        return iconPaths
            .stream()
            .flatMap(path -> getIconInternalPath(size)
                .map(path::resolve))
            .toList();
    }

    protected Stream<String> getIconInternalPath(IconSize iconSize) {
        return ofNullable(iconSize)
            .map(size -> ofNullable(iconInternalPathMap)
                .map(pathsMap -> pathsMap.get(size))
                .stream()
                .flatMap(Collection::stream))
            .orElseGet(() -> ofNullable(internalPathsWithoutSize)
                .stream()
                .flatMap(Collection::stream));
    }
}
