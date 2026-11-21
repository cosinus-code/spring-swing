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

package org.cosinus.swing.image;

import org.junit.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.junit.Assert.assertEquals;

/**
 * Test the creation of color filled images.
 */
public class ImageHandlerColorFilledImageTest {

    private final ImageHandler imageHandler = new ImageHandler(null);

    @Test
    public void testOpaqueColor() {
        BufferedImage image = imageHandler.createColorFilledImage(30, 20, Color.ORANGE);

        assertEquals(30, image.getWidth());
        assertEquals(20, image.getHeight());
        assertEquals(TYPE_INT_RGB, image.getType());
        assertAllPixels(Color.ORANGE.getRGB(), image);
    }

    @Test
    public void testTranslucentColor() {
        Color translucent = new Color(10, 20, 30, 128);

        BufferedImage image = imageHandler.createColorFilledImage(5, 7, translucent);

        assertEquals(TYPE_INT_ARGB, image.getType());
        assertAllPixels(translucent.getRGB(), image);
    }

    @Test
    public void testTransparentColor() {
        BufferedImage image = imageHandler.createColorFilledImage(4, 4, new Color(255, 0, 0, 0));

        assertEquals(TYPE_INT_ARGB, image.getType());
        assertAllPixels(new Color(255, 0, 0, 0).getRGB(), image);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSize() {
        imageHandler.createColorFilledImage(0, 10, Color.RED);
    }

    private void assertAllPixels(final int expectedArgb, final BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                assertEquals("pixel " + x + "," + y, expectedArgb, image.getRGB(x, y));
            }
        }
    }
}
