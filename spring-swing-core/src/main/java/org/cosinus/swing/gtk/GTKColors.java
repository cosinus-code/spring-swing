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

package org.cosinus.swing.gtk;

import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

import org.cosinus.swing.color.Colors;

/**
 *
 */
public class GTKColors {
    public static final int GTK_ICON_SIZE_INVALID = 0;
    public static final int GTK_ICON_SIZE_MENU = 1; // 16x16
    public static final int GTK_ICON_SIZE_SMALL_TOOLBAR = 2; // 18x18
    public static final int GTK_ICON_SIZE_LARGE_TOOLBAR = 3; // 24x24
    public static final int GTK_ICON_SIZE_BUTTON = 4; // 20x20
    public static final int GTK_ICON_SIZE_DND = 5; // 32x32
    public static final int GTK_ICON_SIZE_DIALOG = 6; // 48x48

    public static final Color VERTICAL2 = new Color(251, 251, 249);

    public static final Color POINT1 = new Color(215, 211, 206);
    public static final Color POINT2 = new Color(244, 241, 237);

    public static Color getLightBackground() {
        return Colors.getLighterColor(getBackgroundColor());
    }

    public static Color getBackgroundColor() {
        return getBackgroundColor(false);
    }

    public static Color getDisabledColor() {
        return getBackgroundColor(true);
    }

    public static Color getBackgroundColor(boolean pressed) {
        JPanel panel = new JPanel();
        SynthStyle style = SynthLookAndFeel.getStyle(panel, Region.PANEL);
        return style.getColor(new SynthContext(panel, Region.PANEL, style, SynthConstants.ENABLED), ColorType.BACKGROUND);
    }

    public static Color getForegroundColor(boolean pressed) {
        JButton button = new JButton();
        SynthStyle style = SynthLookAndFeel.getStyle(button, Region.BUTTON);
        return style.getColor(new SynthContext(button, Region.BUTTON, style, pressed ? SynthConstants.PRESSED : SynthConstants.ENABLED), ColorType.FOREGROUND);
    }

    public static Color getDarkColor() {
        return getInactiveForegroundColor();
    }

    public static Color getPoint1() {
        return POINT1;
    }

    public static Color getPoint2() {
        return POINT2;
    }

    public static Color getVertical1() {
        return getInactiveForegroundColor();
    }

    public static Color getVertical2() {
        return VERTICAL2;
    }

    public static Color getInactiveForegroundColor() {
        JLabel label = new JLabel();
        SynthStyle style = SynthLookAndFeel.getStyle(label, Region.LABEL);
        return style.getColor(new SynthContext(label, Region.LABEL, style, SynthConstants.DISABLED), ColorType.FOREGROUND);
    }

    public static Color getMenuActiveBackgroundColor() {
        JMenuItem menu = new JMenuItem();
        SynthStyle style = SynthLookAndFeel.getStyle(menu, Region.MENU_ITEM);
        return style.getColor(new SynthContext(menu, Region.MENU_ITEM, style, SynthConstants.SELECTED), ColorType.BACKGROUND);
    }

    public static Color getMenuActiveForegroundColor() {
        JMenuItem menu = new JMenuItem();
        SynthStyle style = SynthLookAndFeel.getStyle(menu, Region.MENU_ITEM);
        return style.getColor(new SynthContext(menu, Region.MENU_ITEM, style, SynthConstants.SELECTED), ColorType.FOREGROUND);
    }

    public static Color getButtonBackgroundColor(boolean pressed, boolean over) {
        JButton button = new JButton();
        SynthStyle style = SynthLookAndFeel.getStyle(button, Region.BUTTON);
        int state = pressed ?
                SynthConstants.PRESSED :
                over ?
                        SynthConstants.MOUSE_OVER :
                        SynthConstants.ENABLED;
        return style.getColor(new SynthContext(button, Region.BUTTON, style, state), ColorType.BACKGROUND);
    }

    public static Color getTableHeaderBottomBorderColor(int deep, boolean pressed, boolean over) {
        Color base = getTableHeaderBackgroundColor(pressed, over);

        int red = base.getRed();
        int green = base.getGreen();
        int blue = base.getBlue();
        switch (deep) {
            case 1:
                red -= 3;
                green -= 3;
                blue -= 3;
                break;
            case 2:
                red -= 8;
                green -= 8;
                blue -= 8;
                break;
            case 3:
                red -= 14;
                green -= 14;
                blue -= 13;
                break;
            case 4:
                red -= 20;
                green -= 19;
                blue -= 19;
                break;
            default:
                return null;
        }
        if (red < 0) red = 0;
        if (green < 0) green = 0;
        if (blue < 0) blue = 0;
        return new Color(red, green, blue);
    }

    public static Color getTableHeaderBackgroundColor(boolean pressed, boolean over) {
        return pressed ?
                getButtonBackgroundColor(pressed, over) :
                over ?
                        getLightBackground() :
                        getBackgroundColor();
    }
}
