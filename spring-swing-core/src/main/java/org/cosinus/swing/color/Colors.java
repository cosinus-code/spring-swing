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

package org.cosinus.swing.color;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

/**
 * Helper for colors
 */
public final class Colors {

    public static final String COLOR_SEPARATOR = ",";

    public static Color getControlLtHighlightColor() {
        return UIManager.getColor("colorControlLtHighlight");
    }

    public static Color getControlHighlightColor() {
        return UIManager.getColor("colorControlHighlight");
    }

    public static Color getControlColor() {
        return UIManager.getColor("colorControl");
    }

    public static Color getControlShadowColor() {
        return UIManager.getColor("colorControlShadow");
    }

    public static Optional<Color> toColor(String descriptor) {
        try {
            String[] pieces = descriptor.split(COLOR_SEPARATOR);
            if (pieces.length > 2) {
                int red = Integer.parseInt(pieces[0].trim());
                int green = Integer.parseInt(pieces[1].trim());
                int blue = Integer.parseInt(pieces[2].trim());
                return Optional.of(new Color(red, green, blue));
            }
        } catch (Exception ex) {
            //TODO: log
        }
        return Optional.empty();
    }

    public static String getColorDescription(Color color) {
        return color.red + COLOR_SEPARATOR + color.green + COLOR_SEPARATOR + color.blue;
    }

    public static Color getLighterColor(Color color) {
        int red = color.getRed() + 12;
        int green = color.getGreen() + 14;
        int blue = color.getBlue() + 16;

        if (red > 255) red = 255;
        if (green > 255) green = 255;
        if (blue > 255) blue = 255;

        return new Color(red, green, blue);
    }

    private Colors() {
    }
}
