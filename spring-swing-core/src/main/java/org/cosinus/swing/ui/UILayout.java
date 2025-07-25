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
package org.cosinus.swing.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.cosinus.swing.layout.SpringGridLayout;

import java.awt.*;
import java.util.function.BiFunction;

import static java.util.Arrays.stream;

public enum UILayout {
    VERTICAL_FLOW((target, rows) -> new GridLayout(rows * 2, 1, 3, 3)),
    SPRING_GRID((target, rows) -> new SpringGridLayout(target, rows, 2, 5, 5, 5, 5));

    private final BiFunction<Container, Integer, LayoutManager> layoutManagerSupplier;

    UILayout(final BiFunction<Container, Integer, LayoutManager> layoutManagerSupplier) {
        this.layoutManagerSupplier = layoutManagerSupplier;
    }

    public LayoutManager layoutManager(Container target, int rows) {
        return layoutManagerSupplier.apply(target, rows);
    }

    @JsonCreator
    public static UILayout fromValue(String value) {
        return stream(values())
            .filter(type -> type.toString().equals(value))
            .findFirst()
            .orElse(null);
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
