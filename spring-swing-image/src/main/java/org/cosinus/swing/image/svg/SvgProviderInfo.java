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

package org.cosinus.swing.image.svg;

import com.twelvemonkeys.imageio.spi.ReaderWriterProviderInfo;

/**
 * implementation of {@link ReaderWriterProviderInfo} for SVG format
 */
final class SvgProviderInfo extends ReaderWriterProviderInfo {

    private static final String SVG = "svg";

    private static final String SVG_MIMETYPE = "image/svg";
    private static final String X_SVG_MIMETYPE = "image/x-svg";
    private static final String SVG_XML_MIMETYPE = "image/svg-xml";
    private static final String SVG_PLUS_XML_MIMETYPE = "image/svg+xml";

    private static final String[] SVG_NAMES = {SVG, SVG.toUpperCase()};
    private static final String[] SVG_SUFFIXES = {SVG};
    private static final String[] SVG_MIMETYPES = {
        SVG_MIMETYPE,
        X_SVG_MIMETYPE,
        SVG_PLUS_XML_MIMETYPE,
        SVG_XML_MIMETYPE
    };
    private static final String SVG_IMAGE_READER_CLASS_NAME = SvgImageReader.class.getName();
    private static final String[] SVG_IMAGE_READER_SPI_CLASS_NAMES = new String[]{SvgImageReaderSpi.class.getName()};

    SvgProviderInfo() {
        super(
            SvgProviderInfo.class,
            SVG_NAMES,
            SVG_SUFFIXES,
            SVG_MIMETYPES,
            SVG_IMAGE_READER_CLASS_NAME,
            SVG_IMAGE_READER_SPI_CLASS_NAMES,
            null, null, false, null, null, null, null,
            true, null, null, null, null
        );
    }
}
