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

package org.cosinus.swing.image.config;

import com.twelvemonkeys.imageio.plugins.svg.SVGImageReaderSpi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.boot.initialize.ApplicationInitializer;
import org.cosinus.swing.image.icon.IconInitializer;
import org.cosinus.swing.image.svg.SvgImageReaderSpi;
import org.cosinus.swing.ui.listener.UIChangeController;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;

import static org.cosinus.stream.Streams.stream;

/**
 * Swing UI initializer
 */
public class ApplicationImageInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(ApplicationImageInitializer.class);

    private final IconInitializer iconInitializer;

    private final UIChangeController uiChangeController;

    public ApplicationImageInitializer(final IconInitializer iconInitializer,
                                       final UIChangeController uiChangeController) {
        this.iconInitializer = iconInitializer;
        this.uiChangeController = uiChangeController;
    }

    @Override
    public void initialize() {
        LOG.info("Initializing application image handlers...");
        new Thread(this::initializeImages).start();
    }

    protected void initializeImages() {
        registerSvgImageReader();
        uiChangeController.registerUIChangeListener(iconInitializer);
        iconInitializer.initializeIcons();
    }

    protected void registerSvgImageReader() {
        try {
            IIORegistry registry = IIORegistry.getDefaultInstance();
            stream(registry.getServiceProviders(ImageReaderSpi.class, true))
                .filter(SVGImageReaderSpi.class::isInstance)
                .forEach(registry::deregisterServiceProvider);
            registry.registerServiceProvider(new SvgImageReaderSpi());
        } catch (Exception e) {
            LOG.warn("Error deregistering svg image reader", e);
        }
    }

    @Override
    public boolean beforeInitializeApplicationFrame() {
        return false;
    }
}
