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

package org.cosinus.swing.util;

import java.awt.*;
import java.util.Optional;

import static java.awt.Font.ITALIC;
import static java.lang.String.join;
import static java.awt.Font.BOLD;
import static java.util.Optional.ofNullable;

/**
 * Font related utils.
 */
public final class FontUtils {

    /**
     * Create Font from given description
     *
     * @param fontDescription the description
     * @return the created Font
     */
    public static Optional<Font> createFont(String fontDescription) {
        try {
            return ofNullable(fontDescription)
                    .map(String::trim)
                    .map(description -> description.split("\\s*,\\s*"))
                    .filter(descriptors -> descriptors.length > 2)
                    .map(pieces -> new Font(pieces[0], Integer.parseInt(pieces[1]), Integer.parseInt(pieces[2])));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Get the style of a font
     *
     * @param font the given font
     * @return the font style
     */
    public static String getFontStyleText(Font font) {
        String style = (font.isBold() ? "Bold" : "") + (font.isItalic() ? "Italic" : "");
        return !style.isEmpty() ? style : "Plain";
    }

    public static int getFontStyle(String text) {
        return (text.startsWith("Bold") ? BOLD : 0) | (text.startsWith("Italic") ? ITALIC : 0);
    }

    /**
     * Get the description a font in format "[family],[size],[style]"
     *
     * @param font the given font
     * @return the font description
     */
    public static String getFontDescription(Font font) {
        return join(",",
                    font.getFamily(),
                    getFontStyleText(font),
                    Integer.toString(font.getSize()));
    }

    private FontUtils() {
    }
}
