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
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.cache.annotation.Cacheable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Optional;

import static java.awt.Image.SCALE_SMOOTH;
import static java.lang.Math.max;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.ImageHandler.DISABLED_FILTER;
import static org.cosinus.swing.image.ImageHandler.GRAY_FILTER;

/**
 * Icons handler
 */
public class IconHandler {

    private static final String SPRING_SWING_ICONS_CACHE_NAME = "spring.swing.icons";

    private final ClasspathResourceResolver resourceResolver;

    private final IconProvider iconProvider;

    private final ApplicationUIHandler uiHandler;

    private final ImageHandler imageHandler;

    public IconHandler(ClasspathResourceResolver resourceResolver,
                       IconProvider iconProvider,
                       ApplicationUIHandler uiHandler, ImageHandler imageHandler) {
        this.resourceResolver = resourceResolver;
        this.iconProvider = iconProvider;
        this.uiHandler = uiHandler;
        this.imageHandler = imageHandler;
    }

    /**
     * Find an icon by name.
     * <p>
     * If a cache configuration is defined in the application
     * with the name {@value #SPRING_SWING_ICONS_CACHE_NAME},
     * then the results are cached.
     *
     * @param name the name to search for
     * @param size the size of the icon to search for
     * @return the found icon, or {@link Optional#empty()}
     */
    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME)
    public Optional<Icon> findIconByName(String name, IconSize size) {
        return iconProvider.findIconByName(name, size);
    }

    /**
     * Find an icon by resource name.
     * <p>
     * If a cache configuration is defined in the application
     * with the name {@value #SPRING_SWING_ICONS_CACHE_NAME},
     * then the results are cached.
     *
     * @param resourceName the resource name
     * @return the found icon, or {@link Optional#empty()}
     */
    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME, key = "{':resource:', #resourceName}")
    public Optional<Icon> findIconByResource(String resourceName) {
        return resourceResolver.resolveImageAsBytes(resourceName)
            .map(ImageIcon::new);
    }

    /**
     * Find an icon by file.
     * <p>
     * If a cache configuration is defined in the application
     * with the name {@value #SPRING_SWING_ICONS_CACHE_NAME},
     * then the results are cached.
     *
     * @param file the file
     * @param size the size of the icon to search for
     * @return the found icon, or {@link Optional#empty()}
     */
    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME,
        condition = "#file.parent != null",
        keyGenerator = "fileExtensionKeyGenerator")
    public Optional<Icon> findIconByFile(File file, IconSize size) {
        return iconProvider.findIconByFile(file, size)
            .or(() -> uiHandler.getDefaultFileIcon(file.isDirectory())
                .map(icon -> scaleIcon(icon, size)));
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
        return ofNullable(iconToFilter)
            .map(imageHandler::iconToImage)
            .map(image -> imageHandler.applyFilter(image, filter))
            .map(ImageIcon::new)
            .map(Icon.class::cast)
            .orElse(iconToFilter);
    }

    public Icon scaleIcon(Icon iconToResize, IconSize size) {
        return ofNullable(iconToResize)
            .filter(icon -> icon.getIconWidth() != size.getSize() ||
                icon.getIconHeight() != size.getSize())
            .map(imageHandler::iconToImage)
            .map(image -> imageHandler.scaleUpImage(image, size.getSize(), size.getSize()))
            .<Icon>map(ImageIcon::new)
            .orElse(iconToResize);
    }
}
