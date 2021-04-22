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

package org.cosinus.swing.util;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.lang.Math.ceil;
import static java.lang.Math.floor;

/**
 * Numbers related utils
 */
public class Formatter {

    public static final int KILO = 1024;

    public static final int MEGA = KILO * KILO;

    public static final int GIGA = KILO * KILO * KILO;

    public static final String DECIMALS_FORMAT_2 = "#,##0.00";

    public static String formatMemorySize(long memorySize) {
        if (memorySize < 0) {
            return "";
        }
        if (memorySize >= GIGA) {
            return get2DecimalsFormatted((double) memorySize / (1024 * 1024 * 1024)) + " G";
        }
        if (memorySize >= MEGA) {
            return get2DecimalsFormatted((double) memorySize / (1024 * 1024)) + " M";
        }
        if (memorySize >= KILO) {
            return get2DecimalsFormatted((double) memorySize / 1024) + " K";
        }
        return get2DecimalsFormatted(memorySize) + " b";
    }

    public static String formatShortMemorySize(long memorySize) {
        if (memorySize < 0) {
            return "";
        }
        if (memorySize >= GIGA) {
            return (int) ceil((double) memorySize / (1024 * 1024 * 1024)) + " G";
        }
        if (memorySize >= MEGA) {
            return (int) ceil((double) memorySize / (1024 * 1024)) + " M";
        }
        if (memorySize >= KILO) {
            return (int) ceil((double) memorySize / 1024) + " K";
        }
        return memorySize + " b";
    }

    public static String get2DecimalsFormatted(double input) {
        DecimalFormat format_decimal = (DecimalFormat) NumberFormat.getNumberInstance();
        format_decimal.applyPattern(DECIMALS_FORMAT_2);
        try {
            return format_decimal.format(ceil(input * 100) / 100);
        } catch (IllegalArgumentException e) {
            return Double.toString(input);
        }
    }

    public static String formatTime(long seconds) {
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

    public static String formatTextForLabel(JLabel label, String text) {
        FontMetrics labelFontMetrics = label.getFontMetrics(label.getFont());
        int textWidth = SwingUtilities.computeStringWidth(labelFontMetrics, text);
        int lblWidth = label.getWidth();

        String newText = text;
        int middle = newText.length() / 2;
        int middle1 = middle;
        int middle2 = middle;
        while (textWidth > lblWidth && middle1 > 0 && middle2 < text.length() - 1) {
            newText = text.substring(0, --middle1) + "..." + text.substring(++middle2);
            textWidth = SwingUtilities.computeStringWidth(labelFontMetrics, newText);
        }
        return newText;
    }

}
