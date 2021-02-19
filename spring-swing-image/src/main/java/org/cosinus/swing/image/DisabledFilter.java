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

package org.cosinus.swing.image;

import java.awt.image.RGBImageFilter;

import static java.awt.SystemColor.controlShadow;

/**
 * RGB filter for disabled like images
 */
public class DisabledFilter extends RGBImageFilter {
    public DisabledFilter() {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        int intensity = (rgb & 0xff000000);
        int systemColor = controlShadow.getRGB();

        int red = (rgb & 0x00ff0000) / 0x10000;
        int green = (rgb & 0x0000ff00) / 0x100;
        int blue = rgb & 0x000000ff;
        int systemRed = (systemColor & 0x00ff0000) / 0x10000;
        int systemGreen = (systemColor & 0x0000ff00) / 0x100;
        int systemBlue = systemColor & 0x000000ff;

        int level = 128;
        if (red < level) red = level;
        if (green < level) green = level;
        if (blue < level) blue = level;
        red = systemRed + (255 - systemRed) * (red - level) / level;
        green = systemGreen + (255 - systemGreen) * (green - level) / level;
        blue = systemBlue + (255 - systemBlue) * (blue - level) / level;

        return (intensity | (red * 0x10000) | (green * 0x100) | blue);
    }
}
