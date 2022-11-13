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
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Optional;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.lang.Math.max;
import static java.util.Optional.ofNullable;

/**
 * Image handler
 */
public class ImageHandler {

    public static final ImageFilter GRAY_FILTER = new GrayFilter();

    public static final ImageFilter DISABLED_FILTER = new javax.swing.GrayFilter(true, 50);

    /**
     * Get the preview icon of a file.
     *
     * @param file the file to preview
     * @param size the size of the preview image
     * @return the preview image
     * @throws IOException if an IO error occurs
     */
    public Optional<Icon> getPreviewImage(File file, int size) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return ofNullable(ImageIO.read(in))
                .map(image -> scaleImage(image, size))
                .map(ImageIcon::new);
        }
    }

    /**
     * Scale image to a new size.
     * <p>
     * New size is relative to the maximum between with and height.
     *
     * @param image the image to scale
     * @param size  the new size
     * @return the new scaled image
     */
    public Image scaleImage(Image image, int size) {
        if (image == null) return null;
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) return null;

        double scale = (double) size / max(width, height);
        if (scale <= 0) {
            return null;
        }

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

    /**
     * Apply a filter to an image.
     *
     * @param image  the image
     * @param filter the filter
     * @return the new filtered image
     */
    public Image applyFilter(Image image, ImageFilter filter) {
        return ofNullable(image)
            .map(Image::getSource)
            .map(imageSource -> new FilteredImageSource(imageSource, filter))
            .map(imageSource -> Toolkit.getDefaultToolkit().createImage(imageSource))
            .orElse(image);
    }

    /**
     * Get the image from an icon.
     *
     * @param icon the icon
     * @return the image
     */
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

    /**
     * Convert bytes array to an image.
     *
     * @param bytes the bytes to convert
     * @return the created image
     */
    public Image bytesToImage(byte[] bytes) {
        try (InputStream input = new ByteArrayInputStream(bytes)) {
            return ImageIO.read(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create an image from an image uri.
     *
     * @param uri the image uri
     * @return the created image
     */
    public Image createImage(String uri) {
        try {
            return ImageIO.read(new URL(uri));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Image grayToTransparency(Image image) {
        ImageProducer ip = new FilteredImageSource(image.getSource(), new RGBImageFilter() {
            public int filterRGB(int x, int y, int rgb) {
                return (rgb << 8) & 0xFF000000;
            }
        });
        return getDefaultToolkit().createImage(ip);
    }

    public Image colorToTransparency(Image im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;

            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }

    public Image colorToTransparency(Image image, Color c1, Color c2) {
        final int r1 = c1.getRed();
        final int g1 = c1.getGreen();
        final int b1 = c1.getBlue();
        final int r2 = c2.getRed();
        final int g2 = c2.getGreen();
        final int b2 = c2.getBlue();
        ImageFilter filter = new RGBImageFilter() {
            public int filterRGB(int x, int y, int rgb) {
                int r = (rgb & 0xFF0000) >> 16;
                int g = (rgb & 0xFF00) >> 8;
                int b = rgb & 0xFF;
                if (r >= r1 && r <= r2 &&
                    g >= g1 && g <= g2 &&
                    b >= b1 && b <= b2) {
                    return rgb & 0xFFFFFF;
                }
                return rgb;
            }
        };

        ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
        return getDefaultToolkit().createImage(ip);
    }

    public BufferedImage toBufferedImage(Image image, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

}
