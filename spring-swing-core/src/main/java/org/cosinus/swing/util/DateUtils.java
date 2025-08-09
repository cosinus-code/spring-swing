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

import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.TimeZone;

import static java.time.ZoneId.systemDefault;

public final class DateUtils {

    public static LocalDateTime startOfYear(int year) {
        return Year.of(year)
            .atDay(1)
            .atStartOfDay();
    }

    public static LocalDateTime lastSecondOfYear(int year) {
        return startOfYear(year + 1)
            .minusSeconds(1);
    }

    public static long toEpochSecond(final LocalDateTime localDateTime) {
        return toEpochSecond(localDateTime, systemDefault());
    }

    public static long toEpochSecond(final LocalDateTime localDateTime,
                                             final ZoneId timeZone) {
        return localDateTime
            .atZone(timeZone)
            .toInstant()
            .getEpochSecond();
    }

    private DateUtils() {}
}
