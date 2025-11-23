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

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.ui.UIProperties;
import org.cosinus.swing.ui.UIProperties.Listener;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

@Slf4j
public class UIChangeController {

    private static final Logger LOG = LogManager.getLogger(UIChangeController.class);

    private final UIThemeProvider uiThemeProvider;

    private final UIProperties uiProperties;

    private final List<UIChangeListener> uiChangeListeners;

    private UIThemeChecksum currentUITheme;

    public UIChangeController(final UIThemeProvider uiThemeProvider,
                              final UIProperties uiProperties) {
        this.uiThemeProvider = uiThemeProvider;
        this.uiProperties = uiProperties;
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
        ofNullable(uiThemeProvider.getUIThemeChecksum())
            .ifPresent(this::checkUiThemeChecksum);
    }

    protected void checkUiThemeChecksum(final UIThemeChecksum uiThemeChecksum) {
        checkAndFireUIChange(uiThemeChecksum,
            UIThemeChecksum::getUiThemeChecksum,
            UIChangeListener::uiThemeChanged);
        checkAndFireUIChange(uiThemeChecksum,
            UIThemeChecksum::getIconThemeChecksum,
            UIChangeListener::iconThemeChanged);
        checkAndFireUIChange(uiThemeChecksum,
            UIThemeChecksum::getColorThemeChecksum,
            UIChangeListener::colorThemeChanged);
        checkAndFireUIChange(uiThemeChecksum,
            UIThemeChecksum::getCursorThemeChecksum,
            UIChangeListener::cursorThemeChanged);

        this.currentUITheme = uiThemeChecksum;

        if (isLogTraceEnabled()) {
            log.trace("KDE ui theme checksum: {}", uiThemeChecksum.getUiThemeChecksum());
            log.trace("KDE color theme checksum: {}", uiThemeChecksum.getColorThemeChecksum());
            log.trace("KDE icon theme checksum: {}", uiThemeChecksum.getIconThemeChecksum());
            log.trace("KDE cursor theme checksum: {}", uiThemeChecksum.getCursorThemeChecksum());
        }
    }

    protected boolean isLogTraceEnabled() {
        return ofNullable(uiProperties.getListener())
            .map(Listener::isTrace)
            .orElse(false);
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
