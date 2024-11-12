package org.cosinus.swing.image;

import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static javax.imageio.ImageIO.createImageInputStream;

/**
 * Encapsulation for a streamed image
 */
public class UpdatableImage {

    private BufferedImage image;

    private long size = -1;

    public UpdatableImage() {
    }

    public UpdatableImage(BufferedImage image) {
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public long getSize() {
        return size;
    }

    /**
     * Update the current image with image bytes.
     * If the current image to update is null, it will be created.
     *
     * @param imageBytes the image bytes to add to the image
     * @throws IOException if something goes wrong
     */
    public void update(byte[] imageBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
             ImageInputStream imageInputStream = createImageInputStream(inputStream);
             ImageUpdater imageUpdater = new ImageUpdater(image)) {

            image = imageUpdater.updateImage(imageInputStream);
            size = imageBytes.length;
        }
    }

}
