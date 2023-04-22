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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.Optional;

/**
 * Handler for colors
 */
public final class Colors {

    private static final Logger LOG = LogManager.getLogger(Colors.class);

    public static final String COLOR_SEPARATOR = ",";

    /**
     * Used to parse a string descriptor of format "[red],[green],[blue]" into a Color object,
     * where red, green and blue are 0..255 numbers.
     *
     * @param descriptor the string descriptor
     * @return the parsed Color object, otherwise Optional.empty
     */
    public static Optional<Color> toColor(String descriptor) {
        try {
            String[] pieces = descriptor.split(COLOR_SEPARATOR);
            if (pieces.length > 2) {
                int red = Integer.parseInt(pieces[0].trim());
                int green = Integer.parseInt(pieces[1].trim());
                int blue = Integer.parseInt(pieces[2].trim());
                return Optional.of(new Color(red, green, blue));
            }
        } catch (NumberFormatException ex) {
            LOG.error("Invalid color descriptor: " + descriptor, ex);
        }
        return Optional.empty();
    }

    /**
     * Get the string descriptor of format "[red],[green],[blue]" corresponding to Color object,
     * where red, green and blue are 0..255 numbers.
     *
     * @param color the source color
     * @return the color descriptor
     */
    public static String getColorDescription(Color color) {
        return color.getRed() + COLOR_SEPARATOR + color.getGreen() + COLOR_SEPARATOR + color.getBlue();
    }

    private Colors() {
    }
}
