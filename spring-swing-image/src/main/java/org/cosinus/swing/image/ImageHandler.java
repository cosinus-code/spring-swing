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

package org.cosinus.swing.image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Image handler
 */
public class ImageHandler {

    public final Map<String, ImageIcon> previewCache = new HashMap<>();

    public static final ImageFilter GRAY_FILTER = new GrayFilter();

    public static final ImageFilter DISABLED_FILTER = new javax.swing.GrayFilter(true, 50);

    public ImageIcon getPreviewImage(File file, int size) throws IOException {
        String key = file.getPath();
        if (previewCache.containsKey(key)) return previewCache.get(key);

        try (InputStream in = new FileInputStream(file)) {
            ImageIcon icon = new ImageIcon(scaleImage(ImageIO.read(in), size));
            previewCache.put(key, icon);
            return icon;
        }
    }

    public Image scaleImage(Image image, int size) {
        if (image == null) return null;
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) return null;

        double scale = (double) size / Math.max(width, height);
        if (scale <= 0) return null;

        // If the image is smaller than the desired image size, don't bother scaling.
        if (scale >= 1.0d) return image;

        AffineTransform tx = new AffineTransform();
        tx.scale(scale, scale);

        BufferedImage outImage = new BufferedImage((int) (scale * width),
                                                   (int) (scale * height),
                                                   BufferedImage.TYPE_INT_ARGB);

        // Paint image.
        Graphics2D g2d = outImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(image, tx, null);
        g2d.dispose();

        return outImage;
    }

    public Image applyFilter(Image image, ImageFilter filter) {
        return Optional.ofNullable(image)
            .map(Image::getSource)
            .map(imageSource -> new FilteredImageSource(imageSource, filter))
            .map(imageSource -> Toolkit.getDefaultToolkit().createImage(imageSource))
            .orElse(image);
    }

    public Image iconToImage(Icon icon) {
        if (icon == null) {
            return null;
        }

        if (icon instanceof ImageIcon) {
            return ((ImageIcon) icon).getImage();
        }

        BufferedImage image = new BufferedImage(icon.getIconWidth(),
                                                icon.getIconHeight(),
                                                BufferedImage.TYPE_INT_ARGB);
        icon.paintIcon(new JPanel(), image.getGraphics(), 0, 0);
        return image;
    }

    public Image bytesToImage(byte[] bytes) {
        try (InputStream input = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Image downloadImage(String uri) {
        try {
            return ImageIO.read(new URL(uri));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
