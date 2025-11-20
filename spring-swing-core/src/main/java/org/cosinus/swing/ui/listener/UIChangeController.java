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

package org.cosinus.swing.ui.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class UIChangeController {

    private static final Logger LOG = LogManager.getLogger(UIChangeController.class);

    private final UIThemeProvider uiThemeProvider;

    private final List<UIChangeListener> uiChangeListeners;

    private UIThemeChecksum currentUITheme;

    public UIChangeController(final UIThemeProvider uiThemeProvider) {
        this.uiThemeProvider = uiThemeProvider;
        this.uiChangeListeners = new ArrayList<>();
    }

    public void registerUIChangeListener(final UIChangeListener uiChangeListener) {
        this.uiChangeListeners.add(uiChangeListener);
    }

    public void removeUIChangeListener(final UIChangeListener uiChangeListener) {
        this.uiChangeListeners.remove(uiChangeListener);
    }

    @Scheduled(fixedDelay = 200)
    public void checkForUIChanges() {
        ofNullable(uiThemeProvider.getUITheme())
            .ifPresent(uiTheme -> {
                checkAndFireUIChange(uiTheme,
                    UIThemeChecksum::getUiThemeChecksum,
                    UIChangeListener::uiThemeChanged);
                checkAndFireUIChange(uiTheme,
                    UIThemeChecksum::getIconThemeChecksum,
                    UIChangeListener::iconThemeChanged);
                checkAndFireUIChange(uiTheme,
                    UIThemeChecksum::getColorThemeChecksum,
                    UIChangeListener::colorThemeChanged);
                checkAndFireUIChange(uiTheme,
                    UIThemeChecksum::getCursorThemeChecksum,
                    UIChangeListener::cursorThemeChanged);

                currentUITheme = uiTheme;
            });
    }

    protected void checkAndFireUIChange(final UIThemeChecksum uiTheme,
                                        final Function<UIThemeChecksum, String> uiThemeChecksumAction,
                                        final Consumer<UIChangeListener> uiChangeListenerAction) {
        ofNullable(currentUITheme)
            .map(uiThemeChecksumAction)
            .filter(checksum -> !checksum.equals(uiThemeChecksumAction.apply(uiTheme)))
            .ifPresent(checksum -> fireUIChange(uiChangeListenerAction));
    }

    protected void fireUIChange(final Consumer<UIChangeListener> uiChangeListenerAction) {
        uiChangeListeners.forEach(uiChangeListenerAction);
    }
}
