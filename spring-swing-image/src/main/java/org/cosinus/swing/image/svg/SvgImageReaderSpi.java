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

import com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi;
import com.twelvemonkeys.imageio.spi.ImageReaderSpiBase;

import javax.imageio.ImageReader;
import java.io.IOException;
import java.util.Locale;

/**
 * Custom Implementation of {@link ImageReaderSpiBase} for SVG which delegate to {@link SVGImageReaderSpi}
 * but in order to fix the backward compatibility to version 1,
 * it falls to the custom {@link SvgImageReader} as image reader
 */
public class SvgImageReaderSpi extends ImageReaderSpiBase {

    private final SVGImageReaderSpi svgImageReaderSpi;

    public SvgImageReaderSpi() {
        super(new SvgProviderInfo());
        this.svgImageReaderSpi = new SVGImageReaderSpi();
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        return svgImageReaderSpi.canDecodeInput(source);
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new SvgImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return svgImageReaderSpi.getDescription(locale);
    }
}
