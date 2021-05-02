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
 * The application startup splash.
 * <p>
 * It can be configured to show the application startup progress
 * using a custom progress bar.
 * The progress bar can customized by specifying:
 * <ul>
 *     <li>the color</li>
 *     <li>the painting starting point (x, y)</li>
 *     <li>the width</li>
 *     <li>the height</li>
 * </ul>
 * <p>
 * If the color is missing or not a valid color, then the default "TextArea.selectionBackground" is used.
 * <p>
 * If the starting point x coordinate is missing or not a number, then the default 0 is used,
 * meaning that the progress bar is starting from the left most side of the splash.
 * <p>
 * If the starting point y coordinate is missing or not a number, then the default splash height is used,
 * meaning that the progress bar is located in the bottom of the splash.
 * <p>
 * If the width is missing or not a number, then the width is computed
 * to place the progress bar in the middle of the splash (on x coordinate).
 * <p>
 * If the height is missing or not a number, then the default height of 3 is used.
 */
public class ApplicationSplash implements AutoCloseable {

    private final SplashScreen splash;

    private Color progressColor;

    private Rectangle splashBounds;

    private int percent;

    private final boolean progress;

    private int progressX;

    private int progressY;

    private int progressWidth;

    private int progressHeight;

    public ApplicationSplash(boolean progress,
                             String progressColor,
                             String splashProgressX,
                             String splashProgressY,
                             String splashProgressWidth,
                             String splashProgressHeight) {
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
            this.progressWidth = parseInt(splashProgressWidth)
                .filter(width -> progressX + width < splashBounds.width)
                .orElseGet(() -> splashBounds.width - 2 * progressX);
            this.progressHeight = parseInt(splashProgressHeight)
                .orElse(3);
        }
    }

    private Optional<Integer> parseInt(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Update the splash with the new status and the progress value.
     *
     * @param status the new status
     * @param percent the new progress percent
     */
    public void update(String status, int percent) {
        if (!progress || (percent > 5 && percent - this.percent < 5)) {
            return;
        }

        ofNullable(splash)
            .filter(SplashScreen::isVisible)
            .map(SplashScreen::createGraphics)
            .ifPresent(g -> {
                g.setColor(progressColor);
                g.fillRect(progressX,
                           progressY - progressHeight,
                           progressWidth * percent / 100,
                           progressHeight);
                splash.update();
                this.percent = percent;
            });
    }

    /**
     * Hides the splash screen, closes the window, and releases all associated
     * resources.
     */
    @Override
    public void close() {
        ofNullable(splash)
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
                    .append("), width ")
                    .append(progressWidth);
            }
        }
        return builder.toString();
    }
}
