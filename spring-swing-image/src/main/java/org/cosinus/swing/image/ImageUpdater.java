package org.cosinus.swing.image;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;
import static javax.imageio.ImageIO.getImageReaders;

/**
 * Buffered image updater. If the provide image is null, it will be created.
 */
public class ImageUpdater implements AutoCloseable {

    private ImageReader imageReader;

    private BufferedImage image;

    /**
     * @param image the image to update
     */
    public ImageUpdater(final BufferedImage image) {
        this.image = image;
    }

    /**
     * Update the image.
     *
     * @param imageInputStream the image input stream to update with
     * @return the updated image
     * @throws IOException if something goes wrong
     */
    public BufferedImage updateImage(final ImageInputStream imageInputStream) throws IOException {
        Iterable<ImageReader> imageReaders = () -> getImageReaders(imageInputStream);
        imageReader = StreamSupport.stream(imageReaders.spliterator(), false)
            .findFirst()
            .orElseThrow(() -> new IOException("Failed to open image reader"));
        imageReader.setInput(imageInputStream);

        ImageReadParam param = imageReader.getDefaultReadParam();
        if (image != null) {
            param.setDestination(image);
        }
        image = imageReader.read(imageReader.getMinIndex(), param);
        return image;
    }

    @Override
    public void close() {
        ofNullable(imageReader)
            .ifPresent(ImageReader::dispose);
    }
}