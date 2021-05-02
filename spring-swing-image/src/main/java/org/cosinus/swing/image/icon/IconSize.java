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

/**
 * Enum for icon sizes
 */
public enum IconSize {
    X16(16),
    X18(18),
    X24(24),
    X20(20),
    X32(32),
    X48(48);

    private final int size;

    IconSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return size + "x" + size;
    }
}
