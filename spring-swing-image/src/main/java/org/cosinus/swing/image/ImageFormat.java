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

import java.util.List;
import java.util.stream.Stream;

import static javax.imageio.ImageIO.getReaderFormatNames;
import static javax.imageio.ImageIO.getWriterFormatNames;

/**
 * The image formats, as source or target of a transformation, given by the image readers and writers available.
 */
public final class ImageFormat {

    private ImageFormat() {
    }

    /**
     * @return the formats of the available image readers, in lower case, sorted
     */
    public static List<String> readerFormats() {
        return normalize(getReaderFormatNames());
    }

    /**
     * @return the formats of the available image writers (e.g. png, jpg, bmp), in lower case, sorted
     */
    public static List<String> writerFormats() {
        return normalize(getWriterFormatNames());
    }

    private static List<String> normalize(final String[] formats) {
        return Stream.of(formats)
            .map(String::toLowerCase)
            .distinct()
            .sorted()
            .toList();
    }
}
