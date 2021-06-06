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

package org.cosinus.swing.math;

public final class MoreMath {

    public static int fitInRange(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int divideAndCeil(int value1, int value2) {
        return (int) Math.ceil((double) value1 / (double) value2);
    }

    public static int divideAndFloor(int value1, int value2) {
        return (int) Math.floor((double) value1 / (double) value2);
    }

    private MoreMath() {
    }
}
