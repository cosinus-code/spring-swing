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

package org.cosinus.swing.image.icon;

import org.cosinus.swing.icon.IconHolder;
import org.cosinus.swing.ui.listener.UIChangeListener;

import java.awt.*;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static org.cosinus.stream.Streams.flatComponentsStream;
import static org.cosinus.swing.icon.IconSize.X16;

public class IconInitializer implements UIChangeListener {

    private final IconHandler iconHandler;

    public IconInitializer(final IconHandler iconHandler) {
        this.iconHandler = iconHandler;
    }

    @Override
    public void iconThemeChanged() {
        initializeIcons();
    }

    public void initializeIcons() {
        iconHandler.resetIcons();

        stream(Frame.getWindows())
            .filter(Component::isVisible)
            .forEach(window -> {
                flatComponentsStream(window)
                    .filter(component -> component instanceof IconHolder)
                    .map(IconHolder.class::cast)
                    .forEach(this::updateIcon);
                window.repaint();
            });
    }

    public void updateIcon(final IconHolder... iconHolders) {
        stream(iconHolders)
            .forEach(iconHolder -> ofNullable(iconHolder.getIconName())
                .flatMap(iconName -> iconHandler.findIconByName(iconName, iconHolder.getIconSize()))
                .ifPresent(iconHolder::setIcon));
    }
}
