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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

/**
 * Test the thumbnail read from the "THM" file saved by a camera next to a video.
 */
public class ImageHandlerSidecarThumbnailTest {

    private static final int SIZE = 100;

    // JPEG compression changes the colors a little
    private static final int COLOR_TOLERANCE = 16;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // the sidecar thumbnail doesn't use the file handler
    private final ImageHandler imageHandler = new ImageHandler(null);

    @Test
    public void testUpperCaseExtension() throws IOException {
        File video = video("GH014811.MP4");
        jpeg("GH014811.THM", 320, 240, Color.RED);

        BufferedImage thumbnail = sidecarThumbnail(video);

        assertSize(100, 75, thumbnail);
        assertColor(Color.RED, thumbnail);
    }

    @Test
    public void testLowerCaseExtension() throws IOException {
        File video = video("gopr0002.mp4");
        jpeg("gopr0002.thm", 320, 240, Color.BLUE);

        assertColor(Color.BLUE, sidecarThumbnail(video));
    }

    @Test
    public void testPreferUpperCaseExtension() throws IOException {
        File video = video("GH014812.MP4");
        File upperCase = jpeg("GH014812.THM", 320, 240, Color.RED);
        // on a case-insensitive file system both names are the same file
        assumeFalse(new File(upperCase.getParentFile(), "GH014812.thm").exists());
        jpeg("GH014812.thm", 320, 240, Color.BLUE);

        assertColor(Color.RED, sidecarThumbnail(video));
    }

    @Test
    public void testPortraitThumbnail() throws IOException {
        File video = video("MVI_0001.MOV");
        jpeg("MVI_0001.THM", 120, 160, Color.GREEN);

        assertSize(75, 100, sidecarThumbnail(video));
    }

    @Test
    public void testNameWithDots() throws IOException {
        File video = video("holiday.2024.mp4");
        jpeg("holiday.2024.THM", 160, 120, Color.RED);

        assertColor(Color.RED, sidecarThumbnail(video));
    }

    @Test
    public void testNoSidecarFile() throws IOException {
        File video = video("GH014813.MP4");
        jpeg("GH014814.THM", 320, 240, Color.RED);

        assertNoThumbnail(video);
    }

    @Test
    public void testSidecarFileIsNotAnImage() throws IOException {
        File video = video("GH014815.MP4");
        Files.writeString(temporaryFolder.getRoot().toPath().resolve("GH014815.THM"), "not a jpeg", UTF_8);

        assertNoThumbnail(video);
    }

    @Test
    public void testSidecarFileIsDirectory() throws IOException {
        File video = video("GH014816.MP4");
        temporaryFolder.newFolder("GH014816.THM");

        assertNoThumbnail(video);
    }

    @Test
    public void testThumbnailFileItself() throws IOException {
        File thumbnail = jpeg("GH014817.THM", 320, 240, Color.RED);

        assertNoThumbnail(thumbnail);
    }

    @Test
    public void testMissingVideo() throws IOException {
        jpeg("GH014818.THM", 320, 240, Color.RED);

        assertNoThumbnail(new File(temporaryFolder.getRoot(), "GH014818.MP4"));
    }

    @Test
    public void testDirectory() throws IOException {
        File directory = temporaryFolder.newFolder("GH014819");
        jpeg("GH014819.THM", 320, 240, Color.RED);

        assertNoThumbnail(directory);
    }

    private BufferedImage sidecarThumbnail(final File video) {
        Optional<Image> thumbnail = imageHandler.createSidecarThumbnail(video, SIZE);
        assertTrue(thumbnail.isPresent());
        return (BufferedImage) thumbnail.get();
    }

    private void assertNoThumbnail(final File file) {
        assertFalse(imageHandler.createSidecarThumbnail(file, SIZE).isPresent());
    }

    private void assertSize(final int width, final int height, final BufferedImage image) {
        assertEquals(width, image.getWidth());
        assertEquals(height, image.getHeight());
    }

    private void assertColor(final Color expected, final BufferedImage image) {
        Color actual = new Color(image.getRGB(image.getWidth() / 2, image.getHeight() / 2));
        assertTrue("expected " + expected + " but was " + actual,
            Math.abs(expected.getRed() - actual.getRed()) <= COLOR_TOLERANCE &&
                Math.abs(expected.getGreen() - actual.getGreen()) <= COLOR_TOLERANCE &&
                Math.abs(expected.getBlue() - actual.getBlue()) <= COLOR_TOLERANCE);
    }

    private File video(final String name) throws IOException {
        File video = temporaryFolder.newFile(name);
        Files.write(video.toPath(), new byte[]{0, 0, 0, 8, 'f', 'r', 'e', 'e'});
        return video;
    }

    private File jpeg(final String name, final int width, final int height, final Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }
        File file = new File(temporaryFolder.getRoot(), name);
        assertTrue(ImageIO.write(image, "jpg", file));
        return file;
    }
}
