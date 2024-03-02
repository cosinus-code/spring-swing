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

package org.cosinus.swing.image.icon;

import java.util.Optional;

import static java.util.Arrays.stream;

/**
 * Enum for icon sizes
 */
public enum IconSize {
    X16(16, "small"),
    X32(32, "normal"),
    X48(48, "large"),
    X64(64, "extra-large");

    private final int size;

    private final String text;

    IconSize(int size, String text) {
        this.size = size;
        this.text = text;
    }

    public int getSize() {
        return size;
    }

    public String getText() {
        return text;
    }

    public static Optional<IconSize> forSize(int size) {
        return stream(values())
            .filter(iconSize -> iconSize.getSize() == size)
            .findFirst();
    }

    public static Optional<IconSize> forText(String text) {
        return stream(values())
            .filter(iconSize -> iconSize.getText().equals(text))
            .findFirst();
    }

    @Override
    public String toString() {
        return size + "x" + size;
    }
}
