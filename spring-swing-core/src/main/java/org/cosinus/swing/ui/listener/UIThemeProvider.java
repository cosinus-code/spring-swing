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

import org.cosinus.swing.ui.IconTheme;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;

public interface UIThemeProvider {

    String OS_DARK_THEME = "dark";

    UIThemeChecksum getUIThemeChecksum();

    Optional<String> getUITheme();

    Optional<String> getIconTheme();

    Optional<String> getDefaultLookAndFeel();

    default boolean isDarkOsTheme() {
        return getUITheme()
            .map(String::toLowerCase)
            .map(theme -> theme.contains(OS_DARK_THEME))
            .orElse(false);
    }

    default Stream<IconTheme> getAdditionalIconThemes() {
        return empty();
    }
}
