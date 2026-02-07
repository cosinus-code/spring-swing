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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.File;
import java.util.Optional;

import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.*;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.ImageHandler.DISABLED_FILTER;
import static org.cosinus.swing.image.ImageHandler.GRAY_FILTER;
import static org.cosinus.swing.image.ImageSettings.QUALITY;

/**
 * Icons handler
 */
public class IconHandler {

    private static final Logger LOG = LogManager.getLogger(IconHandler.class);

    private static final String SPRING_SWING_ICONS_CACHE_NAME = "spring.swing.icons";

    private final ClasspathResourceResolver resourceResolver;

    private final IconProvider iconProvider;

    private final ApplicationUIHandler uiHandler;

    private final ImageHandler imageHandler;

    public IconHandler(final ClasspathResourceResolver resourceResolver,
                       final IconProvider iconProvider,
                       final ApplicationUIHandler uiHandler,
                       final ImageHandler imageHandler) {
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
     * @param rounded whether the icon must be rounded
     * @return the found icon, or {@link Optional#empty()}
     */
    @Cacheable(value = SPRING_SWING_ICONS_CACHE_NAME)
    public Optional<Icon> findIconByName(String name, IconSize size, boolean rounded) {
        return getRemoteIcon(name)
            .or(() -> iconProvider.findIconByName(name, size))
            .or(() -> this.findIconByResource(name + ".png"))
            .map(icon -> scaleIcon(icon, size))
            .map(icon -> rounded ? toCircularIcon(icon, size) : icon);
    }

    protected boolean isUrl(String text) {
        return ofNullable(text)
            .map(url -> url.startsWith("http://")
                || url.startsWith("https://"))
            .orElse(false);
    }

    protected Optional<Icon> getRemoteIcon(String url) {
        return isUrl(url) ?
            ofNullable(imageHandler.createImage(url))
                .map(ImageIcon::new) :
            empty();
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
            .or(() -> uiHandler.getDefaultFileIcon(file.isDirectory()))
            .map(icon -> scaleIcon(icon, size));
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

    @CacheEvict(value = SPRING_SWING_ICONS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public void resetIcons() {
        LOG.info("'{}' cache evicted due to icon theme changed.", SPRING_SWING_ICONS_CACHE_NAME);
        iconProvider.initialize();
    }


    public Icon toCircularIcon(Icon icon, IconSize iconSize) {
        Image image = imageHandler.iconToImage(icon);
        int size = iconSize.getSize();

        BufferedImage output = new BufferedImage(size, size, TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();

        g2d.setRenderingHint(KEY_RENDERING, QUALITY.getRenderingHint());
        g2d.setRenderingHint(KEY_ANTIALIASING, QUALITY.getAntialiasingHint());
        g2d.setRenderingHint(KEY_INTERPOLATION, QUALITY.getInterpolationHint());
        g2d.setRenderingHint(KEY_ALPHA_INTERPOLATION, QUALITY.getAlphaInterpolationHint());

        g2d.setClip(new Ellipse2D.Float(1, 1, size - 2, size - 2));
        g2d.drawImage(image, 0, 0, size, size, null);

        g2d.setClip(null);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(WHITE);
        g2d.drawOval(1, 1, size - 2, size - 2);

        g2d.dispose();

        return new ImageIcon(output);
    }
}
