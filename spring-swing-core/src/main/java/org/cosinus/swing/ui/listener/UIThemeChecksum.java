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

package org.cosinus.swing.ui.listener;

import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

@Getter
public class UIThemeChecksum {

    private String uiThemeChecksum;

    private String iconThemeChecksum;

    private String colorThemeChecksum;

    private String cursorThemeChecksum;

    public void setUIThemeChecksum(Object... values) {
        this.uiThemeChecksum = toChecksum(values);
    }

    public void setIconThemeChecksum(Object... values) {
        this.iconThemeChecksum = toChecksum(values);
    }

    public void setColorThemeChecksum(Object... values) {
        this.colorThemeChecksum = toChecksum(values);
    }

    public void setCursorThemeChecksum(Object... values) {
        this.cursorThemeChecksum = toChecksum(values);
    }

    protected String toChecksum(Object... pieces) {
        return stream(pieces)
            .map(piece -> piece instanceof Optional optional ? optional.orElse(null) : piece)
            .filter(Objects::nonNull)
            .map(Object::toString)
            .filter(not(String::isEmpty))
            .collect(joining("|"));
    }

    public static String cleanup(String setting) {
        return ofNullable(setting)
            .map(piece -> piece.replaceAll("('|\\r|\\n)", ""))
            .map(String::trim)
            .orElse(setting);
    }
}
