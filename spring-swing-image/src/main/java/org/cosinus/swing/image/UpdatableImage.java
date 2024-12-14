package org.cosinus.swing.image;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.drew.imaging.ImageMetadataReader.readMetadata;
import static com.drew.metadata.exif.ExifDirectoryBase.TAG_ORIENTATION;
import static javax.imageio.ImageIO.createImageInputStream;
import static org.cosinus.swing.image.ImageRotation.NO_ROTATION;
import static org.cosinus.swing.image.ImageRotation.findImageRotation;

/**
 * Encapsulation for a streamed image
 */
public class UpdatableImage {

    private static final Logger LOG = LogManager.getLogger(UpdatableImage.class);

    private final String imageType;

    private BufferedImage originalImage;

    private BufferedImage image;

    private Metadata imageMetadata;

    private ImageRotation imageRotation = NO_ROTATION;

    private long size = -1;

    public UpdatableImage(String imageType) {
        this.imageType = imageType;
    }

    public BufferedImage getImage() {
        return image;
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
        if (imageMetadata == null) {
            try (InputStream input = new ByteArrayInputStream(imageBytes)) {
                imageMetadata = readMetadata(input);
                ExifIFD0Directory exifInfo = imageMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                imageRotation = findImageRotation(exifInfo.getInt(TAG_ORIENTATION));
            } catch (ImageProcessingException | MetadataException ex) {
                LOG.error("Failed to read image metadata", ex);
            }
        }

        try (InputStream input = new ByteArrayInputStream(imageBytes);
             ImageInputStream imageInputStream = createImageInputStream(input);
             ImageUpdater imageUpdater = new ImageUpdater(originalImage, imageType)) {

            originalImage = imageUpdater.updateImage(imageInputStream);
            image = imageRotation.apply(originalImage);
            size = imageBytes.length;
        }
    }
}
