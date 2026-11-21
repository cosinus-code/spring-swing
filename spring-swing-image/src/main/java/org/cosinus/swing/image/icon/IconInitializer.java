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
import org.cosinus.swing.worker.WorkerListener;
import org.springframework.context.ApplicationEventPublisher;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;

public class IconInitializer implements UIChangeListener {

    private final IconHandler iconHandler;

    private final ApplicationEventPublisher eventPublisher;

    public IconInitializer(final IconHandler iconHandler,
                           final ApplicationEventPublisher eventPublisher) {
        this.iconHandler = iconHandler;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void iconThemeChanged() {
        initializeIcons();
    }

    /**
     * Reset the icons and update the icon holders in background,
     * then publish an {@link IconsResetEvent} for the icons which are not held by an icon holder.
     */
    public void initializeIcons() {
        new UpdateIconsWorker()
            .registerListener(new WorkerListener<>() {
                @Override
                public void workerFinished(IconInitializerModel workerModel) {
                    eventPublisher.publishEvent(new IconsResetEvent(IconInitializer.this));
                }
            })
            .start();
    }

    public void updateIcon(final IconHolder... iconHolders) {
        stream(iconHolders)
            .forEach(iconHolder -> ofNullable(iconHolder.getIconName())
                .flatMap(iconName ->
                    iconHandler.findIconByName(iconName, iconHolder.getIconSize(), iconHolder.isIconRounded()))
                .ifPresent(iconHolder::setIcon));
    }
}
