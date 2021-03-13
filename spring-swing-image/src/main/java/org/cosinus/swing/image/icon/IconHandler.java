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

import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.cache.annotation.Cacheable;

import javax.swing.*;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Optional;

import static org.cosinus.swing.image.ImageHandler.DISABLED_FILTER;
import static org.cosinus.swing.image.ImageHandler.GRAY_FILTER;

/**
 * Icons handler
 */
public class IconHandler {

    private static final String SPRING_SWING_ICONS_CACHE_NAME = "spring.swing.icons";

    private final ResourceResolver resourceResolver;

    private final IconProvider iconProvider;

    private final ApplicationUIHandler uiHandler;

    private final ImageHandler imageHandler;

    public IconHandler(ResourceResolver resourceResolver,
                       IconProvider iconProvider,
                       ApplicationUIHandler uiHandler, ImageHandler imageHandler) {
        this.resourceResolver = resourceResolver;
        this.iconProvider = iconProvider;
        this.uiHandler = uiHandler;
        this.imageHandler = imageHandler;
    }

    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME)
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return iconProvider.findIconByName(name, size);
    }

    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME, key = "{':resource:', #name}")
    public Optional<Icon> findIconByResource(String name) {
        return resourceResolver.resolveImageAsBytes(name)
            .map(ImageIcon::new);
    }

    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME, keyGenerator = "fileExtensionKeyGenerator")
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return iconProvider.findIconByFile(file, size)
            .or(() -> uiHandler.getDefaultFileIcon(file));
        //TODO
        //.map(icon -> file.isHidden() ? getGrayFilteredIcon(icon) : icon);
    }

    protected boolean isIconFile(File file) {
        //TODO
        //String extension = getExtension(file);
        //return extension.equals("exe") || extension.equals("ico") || extension.equals("icns");
        return false;
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
}
