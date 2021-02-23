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

import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.*;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Optional;

import static org.cosinus.swing.image.ImageHandler.DISABLED_FILTER;
import static org.cosinus.swing.image.ImageHandler.GRAY_FILTER;
import static org.cosinus.swing.util.FileUtils.getExtension;

/**
 * Icons handler
 */
@SpringSwingComponent
public class IconHandler {

    private final ResourceResolver resourceResolver;

    private final IconCache iconsCache;

    private final IconProvider iconProvider;

    private final ApplicationUIHandler uiHandler;

    private final ImageHandler imageHandler;

    public IconHandler(ResourceResolver resourceResolver,
                       IconCache iconsCache,
                       IconProvider iconProvider,
                       ApplicationUIHandler uiHandler, ImageHandler imageHandler) {
        this.resourceResolver = resourceResolver;
        this.iconsCache = iconsCache;
        this.iconProvider = iconProvider;
        this.uiHandler = uiHandler;
        this.imageHandler = imageHandler;
    }

    public Optional<Icon> findIconByResource(String name) {
        return iconsCache.getValue(name)
            .or(() -> createResourceIconAndCache(name));
    }

    public Optional<Icon> createResourceIconAndCache(String name) {
        return createResourceIcon(name)
            .map(icon -> iconsCache.cache(icon, name));
    }

    public Optional<ImageIcon> createResourceIcon(String name) {
        return resourceResolver.resolveImageAsBytes(name)
            .map(ImageIcon::new);
    }

    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return Optional.ofNullable(getFileIconName(file))
            .flatMap(fileIconName -> iconsCache.getValue(fileIconName, size, file.isHidden()))
            .or(() -> createFileIcon(file, size));
    }

    protected String getFileIconName(File file) {
        return file.isDirectory() ?
            iconProvider.getFolderKey() :
            getExtension(file);
    }

    public Optional<Icon> createFileIcon(File file, IconSize size) {
        return iconProvider.findIconByFile(file, size)
            .or(() -> uiHandler.getDefaultFileIcon(file))
            //.map(icon -> file.isHidden() ? getGrayFilteredIcon(icon) : icon)
            .map(icon -> cache(icon, file, size));
    }

    protected Icon cache(Icon icon, File file, IconSize size) {
        return Optional.ofNullable(file)
            .filter(f -> !isIconFile(file))
            .map(this::getFileIconName)
            .map(fileIconName -> cache(icon, fileIconName, size, file.isHidden()))
            .orElse(icon);
    }

    protected boolean isIconFile(File file) {
        //TODO
        //String extension = getExtension(file);
        //return extension.equals("exe") || extension.equals("ico") || extension.equals("icns");
        return false;
    }

    protected Icon cache(Icon icon, String name, IconSize size, boolean hidden) {
        if (name == null) return null;
        return iconsCache.cache(icon, name, size, hidden);
    }

    public Icon getGrayFilteredIcon(Icon icon) {
        return applyFilter(icon, GRAY_FILTER);
    }

    public Icon getDisabledIcon(Icon icon) {
        return applyFilter(icon, DISABLED_FILTER);
    }

    public Icon applyFilter(Icon iconToFilter, ImageFilter filter) {
        return Optional.ofNullable(iconToFilter)
            .map(imageHandler::iconToImage)
            .map(image -> imageHandler.applyFilter(image, filter))
            .map(ImageIcon::new)
            .map(Icon.class::cast)
            .orElse(iconToFilter);
    }

    public Optional<Icon> findIconByName(String name, IconSize size) {
        return iconsCache.getValue(name, size)
            .or(() -> createNamedIconAndCache(name, size));
    }

    private Optional<Icon> createNamedIconAndCache(String name, IconSize size) {
        return iconProvider.findIconByName(name, size)
            .map(icon -> iconsCache.cache(icon, name, size));
    }
}
