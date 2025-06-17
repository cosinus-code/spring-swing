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

package org.cosinus.swing.border;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Handler for borders
 */
public final class Borders {

    public static Border emptyBorder(int size) {
        return new EmptyBorder(new Insets(size, size, size, size));
    }

    public static Border emptyBorder(int top, int left, int bottom, int right) {
        return new EmptyBorder(new Insets(top, left, bottom, right));
    }

    public static Insets emptyInsets() {
        return new Insets(0, 0, 0, 0);
    }

    public static Border lineBorder(Color color, int top, int left, int bottom, int right) {
        return new LineBorder(color, top, left, bottom, right);
    }

    public static Border lineBorder(Color color) {
        return new LineBorder(color);
    }

    public static Border borderWithMargin(Border border, int margin) {
        return new CompoundBorder(border, emptyBorder(margin));
    }

    public static Border borderWithMargin(Border border, int top, int left, int bottom, int right) {
        return new CompoundBorder(border, emptyBorder(top, left, bottom, right));
    }

    private Borders() {
    }
}
