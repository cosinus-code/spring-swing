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

    public void setImageRotation(ImageRotation imageRotation) {
        this.imageRotation = imageRotation;
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
                if (exifInfo != null) {
                    findImageRotation(exifInfo.getInt(TAG_ORIENTATION))
                        .ifPresent(this::setImageRotation);
                }
            } catch (ImageProcessingException ex) {
                LOG.error("Failed to read image metadata", ex);
            } catch (MetadataException ex) {
                LOG.debug("No 'Orientation' metadata", ex);
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
