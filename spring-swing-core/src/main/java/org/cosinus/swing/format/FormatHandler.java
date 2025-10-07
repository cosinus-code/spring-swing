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

package org.cosinus.swing.format;

import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.time.Instant.ofEpochMilli;
import static java.time.LocalDateTime.ofInstant;
import static java.time.format.FormatStyle.MEDIUM;
import static javax.swing.SwingUtilities.computeStringWidth;

@Component
public class FormatHandler {

    public static final long KILO = 1024;

    public static final long MEGA = KILO * KILO;

    public static final long GIGA = KILO * KILO * KILO;

    public static final long TERA = KILO * KILO * KILO * KILO;

    public static final int KILO_INT = (int) KILO;

    public static final int MEGA_INT = (int) MEGA;

    public static final int GIGA_INT = (int) GIGA;

    public static final long KILOMETER = 1000;

    public static final String DECIMALS_FORMAT_2 = "#,##0.00";

    public String formatMemorySize(long memorySize) {
        if (memorySize < 0) {
            return "";
        }
        if (memorySize >= TERA) {
            return get2DecimalsFormatted((double) memorySize / TERA) + " T";
        }
        if (memorySize >= GIGA) {
            return get2DecimalsFormatted((double) memorySize / GIGA) + " G";
        }
        if (memorySize >= MEGA) {
            return get2DecimalsFormatted((double) memorySize / MEGA) + " M";
        }
        if (memorySize >= KILO) {
            return get2DecimalsFormatted((double) memorySize / KILO) + " K";
        }
        return get2DecimalsFormatted(memorySize) + " b";
    }

    public String formatShortMemorySize(long memorySize) {
        if (memorySize < 0) {
            return "";
        }
        if (memorySize >= TERA) {
            return (int) ceil((double) memorySize / TERA) + " G";
        }
        if (memorySize >= GIGA) {
            return (int) ceil((double) memorySize / GIGA) + " G";
        }
        if (memorySize >= MEGA) {
            return (int) ceil((double) memorySize / MEGA) + " M";
        }
        if (memorySize >= KILO) {
            return (int) ceil((double) memorySize / KILO) + " K";
        }
        return memorySize + " b";
    }

    public String get2DecimalsFormatted(double input) {
        DecimalFormat format_decimal = (DecimalFormat) NumberFormat.getNumberInstance();
        format_decimal.applyPattern(DECIMALS_FORMAT_2);
        try {
            return format_decimal.format(ceil(input * 100) / 100);
        } catch (IllegalArgumentException e) {
            return Double.toString(input);
        }
    }

    public String formatTime(long seconds) {
        StringBuilder formattedTime = new StringBuilder();
        if (seconds >= 3600) {
            int hours = (int) floor((double) seconds / 3600);
            seconds = seconds % 3600;
            formattedTime.append(hours).append("h ");
        }
        if (seconds >= 60) {
            int minutes = (int) floor((double) seconds / 60);
            seconds = seconds % 60;
            formattedTime.append(minutes).append("m ");
        }
        formattedTime.append(seconds).append("s");
        return formattedTime.toString();
    }

    public String formatTextForLabel(JComponent component, String text) {
        FontMetrics labelFontMetrics = component.getFontMetrics(component.getFont());
        int textWidth = computeStringWidth(labelFontMetrics, text);

        int iconWidth = Optional.of(component)
            .filter(c -> JLabel.class.isAssignableFrom(c.getClass()))
            .map(JLabel.class::cast)
            .map(JLabel::getIcon)
            .map(Icon::getIconWidth)
            .orElse(0);

        int lblWidth = component.getWidth() - component.getInsets().left - component.getInsets().right - iconWidth;

        String newText = text;
        int middle = newText.length() / 2;
        int middle1 = middle;
        int middle2 = middle;
        while (textWidth > lblWidth && middle1 > 0 && middle2 < text.length() - 1) {
            newText = text.substring(0, --middle1) + "..." + text.substring(++middle2);
            textWidth = computeStringWidth(labelFontMetrics, newText);
        }
        return newText;
    }

    public String formatDate(long timestamp) {
        return DateTimeFormatter
            .ofLocalizedDate(MEDIUM)
            .format(ofInstant(ofEpochMilli(timestamp), TimeZone.getDefault().toZoneId()));
    }

    public String formatDistance(double distance) {
        if (distance < 0) {
            return "";
        }
        if (distance >= KILOMETER) {
            return get2DecimalsFormatted((double) distance / KILOMETER) + " Km";
        }
        return get2DecimalsFormatted(distance) + " m";
    }

    public String formatElevation(double elevation) {
        return get2DecimalsFormatted(elevation) + " m";
    }

    public String formatPace(double pace) {
        return get2DecimalsFormatted(pace) + " min/km";
    }

    public String formatIndexAsString(int index, int count) {
        String indexFormat = count < 10 ? "%01d" :
            count < 100 ? "%02d" :
                count < 1000 ? "%03d" : "%04d";
        return indexFormat.formatted(index);
    }
}
