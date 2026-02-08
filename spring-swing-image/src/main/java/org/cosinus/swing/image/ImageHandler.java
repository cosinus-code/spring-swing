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

package org.cosinus.swing.image;

import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.form.Canvas;
import org.cosinus.swing.form.SquareCanvas;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Optional;

import static java.awt.Image.SCALE_SMOOTH;
import static java.awt.RenderingHints.*;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.Transparency.OPAQUE;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static javax.imageio.ImageIO.read;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cosinus.swing.image.ImageSettings.QUALITY;

/**
 * Image handler
 */
public class ImageHandler {

    public static final String SPRING_SWING_IMAGE_THUMBNAIL_CACHE_NAME = "spring.swing.image.thumbnails";

    public static final ImageFilter GRAY_FILTER = new GrayFilter();

    public static final ImageFilter DISABLED_FILTER = new javax.swing.GrayFilter(true, 50);

    private final FileHandler fileHandler;

    public ImageHandler(final FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    /**
     * Get the preview icon of a file.
     *
     * @param file the file to preview
     * @param size the size of the preview image
     * @return the preview image
     * @throws IOException if an IO error occurs
     */
    @Cacheable(SPRING_SWING_IMAGE_THUMBNAIL_CACHE_NAME)
    public Optional<Icon> getThumbnail(File file, int size) throws IOException {
        return empty();
    }

    /**
     * Put in cache the preview icon of a file.
     *
     * @param file the file to preview
     * @param size the size of the preview image
     * @return the preview image
     * @throws IOException if an IO error occurs
     */
    @CachePut(SPRING_SWING_IMAGE_THUMBNAIL_CACHE_NAME)
    public Optional<Icon> createThumbnail(File file, int size) throws IOException {
        if (file.exists() && fileHandler.isImage(file.toPath())) {
            try (InputStream input = new FileInputStream(file)) {
                final UpdatableImage image = new UpdatableImage(getExtension(file.getName()));
                image.update(toByteArray(input));
                return Optional.of(new ImageIcon(scaleImage(image.getImage(), size)));
            }
        } else {
            return empty();
        }
    }

    /**
     * Scale image to a square of the provided size.
     *
     * @param image the image to scale
     * @param size  the new size
     * @return the scaled image
     */
    public BufferedImage scaleImage(Image image, int size) {
        return fitImageToCanvas(image, new SquareCanvas(size));
    }

    /**
     * Scale an image to fit to the provided canvas.
     *
     * @param image  the image to scale
     * @param canvas the canvas
     * @return the scaled image
     */
    public BufferedImage fitImageToCanvas(Image image, Canvas canvas) {
        return fitImageToCanvas(image, canvas, QUALITY);
    }

    /**
     * Scale an image to fit to the provided width/height.
     *
     * @param image         the image to scale
     * @param canvas        the canvas
     * @param imageSettings the image processing hints
     * @return the scaled image, or null if the image cannot be scaled
     */
    public BufferedImage fitImageToCanvas(final Image image, Canvas canvas,
                                          final ImageSettings imageSettings) {

        boolean opaqueImage = Optional.of(image)
            .filter(img -> BufferedImage.class.isAssignableFrom(img.getClass()))
            .map(BufferedImage.class::cast)
            .map(img -> img.getTransparency() == OPAQUE)
            .orElse(false);
        int imageType = opaqueImage ? TYPE_INT_RGB : TYPE_INT_ARGB;

        Image imageToScale = prepareImageForScaling(image, canvas, imageSettings, imageType);
        return fitImageToCanvas(imageToScale, canvas.getWidth(), canvas.getHeight(), imageSettings, imageType);
    }

    public Image prepareImageForScaling(final Image image,
                                        Canvas canvas,
                                        final ImageSettings imageSettings) {
        boolean opaqueImage = Optional.of(image)
            .filter(img -> BufferedImage.class.isAssignableFrom(img.getClass()))
            .map(BufferedImage.class::cast)
            .map(img -> img.getTransparency() == OPAQUE)
            .orElse(false);
        int imageType = opaqueImage ? TYPE_INT_RGB : TYPE_INT_ARGB;

        return prepareImageForScaling(image, canvas, imageSettings, imageType);
    }

    private Image prepareImageForScaling(final Image image,
                                         Canvas canvas,
                                         final ImageSettings imageSettings,
                                         int imageType) {
        Image preparedimage = image;
        if (imageSettings.isHighQualityOnScaling()) {
            while (isCanvasLessThanHalfOfImageDimension(preparedimage, canvas)) {
                preparedimage = scaleImageByHalf(preparedimage, imageSettings, imageType);
            }
        }

        return preparedimage;
    }

    /**
     * Scale image by half.
     *
     * @param image         yhe image to scale
     * @param imageSettings the image processing hints
     * @param imageType     the image type
     * @return the scaled image
     */
    public BufferedImage scaleImageByHalf(final Image image,
                                          final ImageSettings imageSettings,
                                          int imageType) {
        return fitImageToCanvas(image,
            image.getWidth(null) / 2,
            image.getHeight(null) / 2,
            imageSettings, imageType);
    }

    private BufferedImage fitImageToCanvas(final Image image,
                                           int width, int height,
                                           final ImageSettings imageSettings,
                                           int imageType) {
        Dimension fitDimension = getFitDimension(image, width, height);
        if (fitDimension == null) {
            return null;
        }

        int fitWidth = fitDimension.width;
        int fitHeight = fitDimension.height;

        BufferedImage scaledImage = new BufferedImage(fitWidth, fitHeight, imageType);
        Graphics2D g2d = scaledImage.createGraphics();
        drawImage(g2d, image, 0, 0, fitWidth, fitHeight, imageSettings, null);
        g2d.dispose();

        return scaledImage;
    }

    public boolean isCanvasLessThanHalfOfImageDimension(final Image image, Canvas canvas) {
        if (image == null) {
            return false;
        }
        return canvas.getWidth() < image.getWidth(null) / 2 ||
            canvas.getHeight() < image.getHeight(null) / 2;
    }

    public boolean isCanvasLessThanImageDimension(final Image image, int canvasWidth, int canvasHeight) {
        if (image == null) {
            return false;
        }
        return canvasWidth < image.getWidth(null) ||
            canvasHeight < image.getHeight(null);
    }

    public boolean isImageFitInCanvas(final Image image, Canvas canvas) {
        if (image == null) {
            return false;
        }
        return canvas.getWidth() == image.getWidth(null) ||
            canvas.getHeight() == image.getHeight(null);
    }

    private Dimension getFitDimension(final Image image, int canvasWidth, int canvasHeight) {
        return getFitDimension(image, canvasWidth, canvasHeight, null);
    }

    private Dimension getFitDimension(final Image image, int canvasWidth, int canvasHeight,
                                      final ImageObserver imageObserver) {
        if (image == null) {
            return null;
        }

        return getFitDimension(
            image.getWidth(imageObserver), image.getHeight(imageObserver),
            canvasWidth, canvasHeight);
    }

    private Dimension getFitDimension(int originalWidth, int originalHeight, int canvasWidth, int canvasHeight) {
        if (originalWidth <= 0 || originalHeight <= 0 || canvasWidth <= 0 || canvasHeight <= 0) {
            return null;
        }

        if (originalWidth > canvasWidth || originalHeight > canvasHeight) {
            double scale = min(
                (double) canvasWidth / (double) originalWidth,
                (double) canvasHeight / (double) originalHeight);
            if (scale <= 0) {
                return null;
            }
            return new Dimension((int) (originalWidth * scale), (int) (originalHeight * scale));
        }

        return new Dimension(originalWidth, originalHeight);
    }

    public void drawFitImage(final Graphics2D g2d,
                             final Image image,
                             int width, int height,
                             boolean centered,
                             final ImageSettings imageSettings) {
        drawFitImage(g2d, image, width, height, centered, imageSettings, null);
    }

    public void drawFitImage(final Graphics2D g2d,
                             final Image image,
                             int width, int height,
                             boolean centered,
                             final ImageSettings imageSettings,
                             final ImageObserver imageObserver) {
        Dimension fitDimension = getFitDimension(image, width, height, imageObserver);
        if (fitDimension == null) {
            throw new IllegalArgumentException("Cannot scale image within dimensions of " + width + "x" + height);
        }

        int x = centered ? (width - fitDimension.width) / 2 : 0;
        int y = centered ? (height - fitDimension.height) / 2 : 0;
        drawImage(g2d, image, x, y, fitDimension.width, fitDimension.height, imageSettings, imageObserver);
    }

    public void drawImage(final Graphics2D g2d,
                          final Image image,
                          int x, int y,
                          final ImageSettings imageSettings) {
        drawImage(g2d, image, x, y,
            image.getWidth(null), image.getHeight(null),
            imageSettings,
            null);
    }

    public void drawImage(final Graphics2D g2d,
                          final Image image,
                          int x, int y,
                          final ImageSettings imageSettings,
                          final ImageObserver imageObserver) {
        drawImage(g2d, image, x, y,
            image.getWidth(imageObserver), image.getHeight(imageObserver),
            imageSettings,
            imageObserver);
    }

    public void drawImage(final Graphics2D g2d,
                          final Image image,
                          int x, int y,
                          int width, int height,
                          final ImageSettings imageSettings) {
        drawImage(g2d, image, x, y, width, height, imageSettings, null);
    }

    public void drawImage(final Graphics2D g2d,
                          final Image image,
                          int x, int y,
                          int width, int height,
                          final ImageSettings imageSettings,
                          final ImageObserver imageObserver) {
        g2d.setRenderingHint(KEY_RENDERING, imageSettings.getRenderingHint());
        g2d.setRenderingHint(KEY_ANTIALIASING, imageSettings.getAntialiasingHint());
        g2d.setRenderingHint(KEY_INTERPOLATION, imageSettings.getInterpolationHint());
        g2d.setRenderingHint(KEY_ALPHA_INTERPOLATION, imageSettings.getAlphaInterpolationHint());
        g2d.drawImage(image, x, y, width, height, imageObserver);
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
            TYPE_INT_ARGB);
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
        RGBImageFilter filter = new RGBImageFilter() {
            @Override
            public int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == (color.getRGB() | 0xFF000000)) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                }
                return rgb;
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
        BufferedImage bufferedImage = new BufferedImage(width, height, TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    /**
     * Create an image from image bytes.
     *
     * @param imageBytes yje image bytes
     * @return the created image
     */
    public BufferedImage getImage(byte[] imageBytes) {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
            return read(inputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Scale up an image.
     *
     * @param image  the image to scale
     * @param width  the new width
     * @param height the new height
     * @return the scaled image
     */
    public Image scaleUpImage(Image image, int width, int height) {
        int max = max(image.getWidth(null), image.getHeight(null));
        int scaleWidth = width - max + image.getWidth(null);
        int scaleHeight = height - max + image.getHeight(null);
        return image.getScaledInstance(scaleWidth, scaleHeight, SCALE_SMOOTH);
    }
}
