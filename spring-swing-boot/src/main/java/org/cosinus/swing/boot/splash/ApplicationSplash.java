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

package org.cosinus.swing.boot.splash;

import org.cosinus.swing.color.Colors;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Application startup splash handler
 */
public class ApplicationSplash implements AutoCloseable {

    private final SplashScreen splash;

    private Color progressColor;

    private Rectangle splashBounds;

    private int percent;

    private final boolean progress;

    private int progressX;

    private int progressY;

    public ApplicationSplash(boolean progress,
                             String progressColor,
                             String splashProgressX,
                             String splashProgressY) {
        this.splash = SplashScreen.getSplashScreen();
        this.progress = progress;

        if (this.splash != null) {
            this.progressColor = ofNullable(progressColor)
                    .flatMap(Colors::toColor)
                    .or(() -> ofNullable(UIManager.getLookAndFeelDefaults()
                                                 .getColor("TextArea.selectionBackground")))
                    .orElse(Color.black);
            this.splashBounds = splash.getBounds();
            this.progressX = parseInt(splashProgressX).orElse(0);
            this.progressY = parseInt(splashProgressY).orElse(splashBounds.height);
        }
    }

    private Optional<Integer> parseInt(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void update(String status, int percent) {
        if (!progress || (percent > 5 && percent - this.percent < 5)) {
            return;
        }

        Optional.ofNullable(splash)
                .filter(SplashScreen::isVisible)
                .map(SplashScreen::createGraphics)
                .ifPresent(g -> {
                    g.setColor(progressColor);
                    int progressHeight = 3;
                    g.fillRect(progressX,
                               progressY - progressHeight,
                               (splashBounds.width - 2 * progressX) * percent / 100,
                               progressHeight);
                    splash.update();
                    this.percent = percent;
                });
    }

    @Override
    public void close() {
        Optional.ofNullable(splash)
                .filter(SplashScreen::isVisible)
                .ifPresent(SplashScreen::close);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (splash == null) {
            builder.append("No splash");
        } else {
            builder.append("Show splash");
            if (!progress) {
                builder.append(" without progress");
            } else {
                builder.append(" with progress: color ")
                        .append(Arrays.toString(new int[]{
                                progressColor.getRed(),
                                progressColor.getGreen(),
                                progressColor.getBlue()}))
                        .append(", position (")
                        .append(progressX)
                        .append(", ")
                        .append(progressY)
                        .append(")");
            }
        }
        return builder.toString();
    }
}
