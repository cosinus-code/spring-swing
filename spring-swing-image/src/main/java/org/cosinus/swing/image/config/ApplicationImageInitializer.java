/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.image.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.boot.initialize.ApplicationInitializer;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconProvider;
import org.cosinus.swing.menu.MenuBar;
import org.cosinus.swing.menu.MenuItemModel;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.icon.IconSize.X16;

/**
 * Swing UI initializer
 */
public class ApplicationImageInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(ApplicationImageInitializer.class);

    private final IconHandler iconHandler;

    private final IconProvider iconProvider;

    private final SwingApplicationFrame applicationFrame;


    public ApplicationImageInitializer(final IconHandler iconHandler,
                                       final IconProvider iconProvider,
                                       final SwingApplicationFrame applicationFrame) {
        this.iconHandler = iconHandler;
        this.iconProvider = iconProvider;
        this.applicationFrame = applicationFrame;
    }

    @Override
    public void initialize() {
        LOG.info("Initializing application image handlers...");
        new Thread(this::initializeImages).start();
    }

    protected void initializeImages() {
        iconProvider.initialize();
        ofNullable(applicationFrame.getMenu())
            .ifPresent(menuBar -> menuBar.getMenuModel()
                .values()
                .stream()
                .flatMap(menu -> menu.entrySet().stream())
                .forEach(menuItamEntry ->
                    setIcon(menuBar, menuItamEntry.getKey(), menuItamEntry.getValue())));
    }

    private void setIcon(final MenuBar menuBar,
                         final String menuItemKey,
                         final MenuItemModel menuItemModel) {
        iconHandler.findIconByName(menuItemModel.getIcon(), X16)
            .ifPresent(icon -> ofNullable(menuBar.getMenuComponent(menuItemKey))
                .ifPresent(menuComponent -> menuComponent.setIcon(icon)));
    }
}
