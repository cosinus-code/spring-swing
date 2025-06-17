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

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;
import static javax.imageio.ImageIO.getImageReaders;
import static javax.imageio.ImageIO.getImageReadersBySuffix;

/**
 * Buffered image updater. If the provide image is null, it will be created.
 */
public class ImageUpdater implements AutoCloseable {

    private final String imageType;

    private ImageReader imageReader;

    private BufferedImage image;

    /**
     * @param image the image to update
     */
    public ImageUpdater(final BufferedImage image, String imageType) {
        this.image = image;
        this.imageType = imageType;
    }

    /**
     * Update the image.
     *
     * @param imageInputStream the image input stream to update with
     * @return the updated image
     * @throws IOException if something goes wrong
     */
    public BufferedImage updateImage(final ImageInputStream imageInputStream) throws IOException {
        imageReader = findImageReaderByInputStream(imageInputStream)
            .or(this::findImageReaderByImageType)
            .orElseThrow(() -> new IOException("Failed to open image reader"));
        imageReader.setInput(imageInputStream);

        ImageReadParam param = imageReader.getDefaultReadParam();
        if (image != null) {
            param.setDestination(image);
        }
        image = imageReader.read(imageReader.getMinIndex(), param);
        return image;
    }

    private Optional<ImageReader> findImageReaderByInputStream(final ImageInputStream imageInputStream) {
        Iterable<ImageReader> imageReaders = () -> getImageReaders(imageInputStream);
        return StreamSupport.stream(imageReaders.spliterator(), false)
            .findFirst();
    }

    private Optional<ImageReader> findImageReaderByImageType() {
        Iterable<ImageReader> imageReaders = () -> getImageReadersBySuffix(imageType);
        return StreamSupport.stream(imageReaders.spliterator(), false)
            .findFirst();
    }

    @Override
    public void close() {
        ofNullable(imageReader)
            .ifPresent(ImageReader::dispose);
    }
}